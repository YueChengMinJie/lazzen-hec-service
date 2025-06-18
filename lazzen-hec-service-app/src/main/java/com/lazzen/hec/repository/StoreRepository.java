package com.lazzen.hec.repository;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lazzen.hec.constants.BusinessConstants;
import com.lazzen.hec.convert.CategoryConvert;
import com.lazzen.hec.dto.CategoryEnergyData;
import com.lazzen.hec.enumeration.ChartQueryEnum;
import com.lazzen.hec.enumeration.DetailDataEnum;
import com.lazzen.hec.form.DataQueryForm;
import com.lazzen.hec.mapper.CategoryEnergyMapper;
import com.lazzen.hec.mapper.CategoryEnergyMonthMapper;
import com.lazzen.hec.mapper.DeviceOnlineStatusMapper;
import com.lazzen.hec.mapper.DevicePointDataMapper;
import com.lazzen.hec.po.CategoryEnergy;
import com.lazzen.hec.po.CategoryEnergyMonth;
import com.lazzen.hec.po.DeviceOnlineStatus;
import com.lazzen.hec.po.DevicePointData;
import com.lazzen.hec.po.base.ActualityObject;
import com.sipa.boot.java8.common.constants.SipaBootCommonConstants;
import com.sipa.boot.java8.data.mysql.constants.SipaBootMysqlConstants;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.NumberUtil;
import lombok.RequiredArgsConstructor;

/**
 * @author guo
 * @createDate 2025-05-18 21:59:34
 */
@Component
@DS("lite-store")
@RequiredArgsConstructor
public class StoreRepository {
    private final CategoryEnergyMapper categoryEnergyMapper;

    private final CategoryEnergyMonthMapper monthMapper;

    private final DeviceOnlineStatusMapper deviceOnlineStatusMapper;

    private final DevicePointDataMapper devicePointDataMapper;

    private final DevicePointDataMapper immediatelyMapper;

    public List<DevicePointData> getImmediatelyBySn(String sn, String deviceType) {
        return immediatelyMapper.selectList(new LambdaQueryWrapper<DevicePointData>().eq(DevicePointData::getSn, sn)
            .eq(StringUtils.isNotBlank(deviceType), DevicePointData::getDeviceType, deviceType)
            .orderByAsc(DevicePointData::getCode));
    }

    public DeviceOnlineStatus getStatusBySn(String sn) {
        return deviceOnlineStatusMapper.selectOne(Wrappers.<DeviceOnlineStatus>lambdaQuery()
            .eq(DeviceOnlineStatus::getSn, sn)
            .orderByDesc(DeviceOnlineStatus::getId)
            .last(SipaBootMysqlConstants.LIMIT_ONE));
    }

    public Page<CategoryEnergy> pageCategoryTotal(DataQueryForm form, String categoryType, String sn) {
        Page<CategoryEnergy> forWard = pageCategory(form, form.getForwardPointCode(), categoryType, sn);
        if (form.getDataEnum() == DetailDataEnum.WATER && !StringUtils.isBlank(form.getReversePointCode())
            && CollectionUtils.isNotEmpty(forWard.getRecords())) {
            // 水 减去反向流量
            Page<CategoryEnergy> reverse = pageCategory(form, form.getReversePointCode(), categoryType, sn);
            if (CollectionUtils.isNotEmpty(reverse.getRecords())) {
                Map<String,
                    String> reverseMap = reverse.getRecords()
                        .stream()
                        .collect(Collectors.toMap(e -> e.getDateIndex() + e.getHourIndex(),
                            CategoryEnergy::getRelaTimeValue, (s, s2) -> s2));
                for (CategoryEnergy record : forWard.getRecords()) {
                    String reverseValue = reverseMap.get(record.getDateIndex() + record.getHourIndex());
                    if (StringUtils.isBlank(reverseValue)) {
                        continue;
                    }
                    BigDecimal num1 = NumberUtil.toBigDecimal(record.getRelaTimeValue());
                    BigDecimal num2 = NumberUtil.toBigDecimal(reverseValue).abs();
                    record.setRelaTimeValue(NumberUtil.sub(num1, num2).toString());
                }
            }
        }
        return forWard;
    }

    private Page<CategoryEnergy> pageCategory(DataQueryForm form, String pointCode, String categoryType, String sn) {
        LambdaQueryWrapper<CategoryEnergy> queryWrapper = Wrappers.<CategoryEnergy>lambdaQuery()
            .eq(CategoryEnergy::getCategory, categoryType)
            .eq(CategoryEnergy::getCode, pointCode)
            .eq(CategoryEnergy::getSn, sn)
            .orderByDesc(CategoryEnergy::getDateIndex)
            .orderByDesc(CategoryEnergy::getHourIndex);
        if (form.getStartDate() != null) {
            String formattedDate = form.getStartDate().format(CategoryConvert.dateFormatter);
            int dateNumber = Integer.parseInt(formattedDate);
            queryWrapper.and(wrapper -> wrapper.ge(CategoryEnergy::getDateIndex, dateNumber)
                .or()
                .gt(CategoryEnergy::getDateIndex, dateNumber)
                .and(i -> i.ge(CategoryEnergy::getHourIndex, form.getStartDate().getHour())));
        }
        if (form.getEndDate() != null) {
            String formattedDate = form.getEndDate().format(CategoryConvert.dateFormatter);
            int dateNumber = Integer.parseInt(formattedDate);
            queryWrapper.and(wrapper -> wrapper.le(CategoryEnergy::getDateIndex, dateNumber)
                .or()
                .lt(CategoryEnergy::getDateIndex, dateNumber)
                .and(i -> i.le(CategoryEnergy::getHourIndex, form.getEndDate().getHour())));
        }
        return categoryEnergyMapper.selectPage(Page.of(form.getPage(), form.getSize()), queryWrapper);
    }

    public List<DevicePointData> querySnFromPointData(DetailDataEnum dataType) {
        return this.devicePointDataMapper.selectList(
            Wrappers.<DevicePointData>lambdaQuery().eq(DevicePointData::getDeviceType, dataType.getDeviceType()));
    }

    public List<CategoryEnergyData> queryDataByDevicePointData(List<DevicePointData> devicePointData,
        ChartQueryEnum dateType, boolean previous, DetailDataEnum dataType) {
        LocalDateTime startDate;
        LocalDateTime endDate;
        LocalDateTime now = LocalDateTime.now();
        if (previous) {
            now = now.minusYears(1);
        }
        if (dateType == ChartQueryEnum.DAY || dateType == ChartQueryEnum.WEEK || dateType == ChartQueryEnum.MONTH) {
            if (dateType == ChartQueryEnum.DAY) {
                // 今天开始到今天结束
                startDate = now.with(LocalTime.MIN);
                endDate = now.with(LocalTime.MAX);
            } else if (dateType == ChartQueryEnum.WEEK) {
                // 本周开始到本周结束
                startDate = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
                endDate = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).with(LocalTime.MAX);
            } else {
                // 本月开始到本月结束
                startDate = now.toLocalDate().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
                endDate = now.toLocalDate().with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
            }
            List<CategoryEnergy> categoryEnergies = this.categoryEnergyMapper.selectList(Wrappers
                .<CategoryEnergy>lambdaQuery()
                .in(CategoryEnergy::getSn, getSnList(devicePointData))
                .in(CategoryEnergy::getCode, getCodeList(dataType, devicePointData))
                .ge(CategoryEnergy::getDateIndex,
                    Integer.parseInt(LocalDateTimeUtil.format(startDate, DateTimeFormatter.BASIC_ISO_DATE)))
                .le(CategoryEnergy::getDateIndex,
                    Integer.parseInt(LocalDateTimeUtil.format(endDate, DateTimeFormatter.BASIC_ISO_DATE)))
                .ge(CategoryEnergy::getHourIndex,
                    LocalDateTimeUtil.format(startDate, BusinessConstants.DateFormat.HOUR))
                .le(CategoryEnergy::getHourIndex, LocalDateTimeUtil.format(endDate, BusinessConstants.DateFormat.HOUR))
                .orderByAsc(CategoryEnergy::getId));
            if (CollectionUtils.isNotEmpty(categoryEnergies)) {
                Map<String, List<CategoryEnergy>> collect;
                if (dateType == ChartQueryEnum.DAY) {
                    Map<String, List<CategoryEnergy>> dbCollect = categoryEnergies.stream()
                        .collect(Collectors.groupingBy(e -> getFormattedHour(e.getHourIndex())));
                    waterDayProcess(devicePointData, dataType, dbCollect);
                    collect = new LinkedHashMap<>();
                    for (int i = 0; i < 25; i++) {
                        String key = getFormattedHour(i + SipaBootCommonConstants.BLANK);
                        collect.put(key, dbCollect.get(key));
                    }
                } else if (dateType == ChartQueryEnum.WEEK) {
                    Map<String, List<CategoryEnergy>> dbCollect = categoryEnergies.stream()
                        .collect(Collectors.groupingBy(e -> getFormattedWeekDay(e.getDateIndex(), startDate, endDate)));
                    waterDayProcess(devicePointData, dataType, dbCollect);
                    collect = new LinkedHashMap<>();
                    for (int i = 0; i < 7; i++) {
                        String key = getFormattedWeekDayForIdx(i);
                        collect.put(key, dbCollect.get(key));
                    }
                } else {
                    Map<String, List<CategoryEnergy>> dbCollect = categoryEnergies.stream()
                        .collect(Collectors.groupingBy(e -> getFormattedDay(e.getDateIndex())));
                    waterDayProcess(devicePointData, dataType, dbCollect);
                    collect = new LinkedHashMap<>();
                    LocalDate lastDay = now.toLocalDate().with(TemporalAdjusters.lastDayOfMonth());
                    IntStream.rangeClosed(1, lastDay.getDayOfMonth()).forEach(day -> {
                        String key = getFormattedDay(day);
                        collect.put(key, dbCollect.get(key));
                    });
                }
                return collect.entrySet()
                    .stream()
                    .map(entry -> CategoryEnergyData.builder()
                        .date(entry.getKey())
                        .value(getSumValue(getValue(entry, dataType, devicePointData)))
                        .build())
                    .collect(Collectors.toList());
            }
        } else {
            if (dateType == ChartQueryEnum.QUARTER) {
                startDate = now.withMonth(now.toLocalDate().getMonth().firstMonthOfQuarter().getValue())
                    .with(TemporalAdjusters.firstDayOfMonth())
                    .with(LocalTime.MIN);
                endDate = now.withMonth(now.toLocalDate().getMonth().firstMonthOfQuarter().getValue() + 2)
                    .with(TemporalAdjusters.lastDayOfMonth())
                    .with(LocalTime.MAX);
            } else {
                startDate = now.withDayOfYear(1).with(LocalTime.MIN);
                endDate = now.with(TemporalAdjusters.lastDayOfYear()).with(LocalTime.MAX);
            }
            List<CategoryEnergyMonth> categoryEnergyMonths =
                this.monthMapper.selectList(Wrappers.<CategoryEnergyMonth>lambdaQuery()
                    .in(CategoryEnergyMonth::getSn, getSnList(devicePointData))
                    .in(CategoryEnergyMonth::getCode, getCodeList(dataType, devicePointData))
                    .ge(CategoryEnergyMonth::getYearIndex,
                        startDate.format(DateTimeFormatter.ofPattern(BusinessConstants.DateFormat.YEAR)))
                    .le(CategoryEnergyMonth::getYearIndex,
                        endDate.format(DateTimeFormatter.ofPattern(BusinessConstants.DateFormat.YEAR)))
                    .ge(CategoryEnergyMonth::getDateIndex,
                        Integer.valueOf(
                            startDate.format(DateTimeFormatter.ofPattern(BusinessConstants.DateFormat.YEAR_MONTH))))
                    .le(CategoryEnergyMonth::getDateIndex,
                        Integer.valueOf(
                            endDate.format(DateTimeFormatter.ofPattern(BusinessConstants.DateFormat.YEAR_MONTH))))
                    .orderByAsc(CategoryEnergyMonth::getId));
            if (CollectionUtils.isNotEmpty(categoryEnergyMonths)) {
                Map<String, List<CategoryEnergyMonth>> collect;
                if (dateType == ChartQueryEnum.QUARTER) {
                    Map<String, List<CategoryEnergyMonth>> dbCollect = categoryEnergyMonths.stream()
                        .collect(Collectors.groupingBy(e -> getFormattedMonth(e.getDateIndex(), startDate, endDate)));
                    waterMonthProcess(devicePointData, dataType, dbCollect);
                    collect = new LinkedHashMap<>();
                    for (int i = 0; i < 3; i++) {
                        String key = getFormattedMonthByIdx(i);
                        collect.put(key, dbCollect.get(key));
                    }
                } else {
                    Map<String, List<CategoryEnergyMonth>> dbCollect = categoryEnergyMonths.stream()
                        .collect(Collectors.groupingBy(e -> getFormattedMonthYear(e.getDateIndex())));
                    waterMonthProcess(devicePointData, dataType, dbCollect);
                    collect = new LinkedHashMap<>();
                    for (int i = 0; i < 12; i++) {
                        String key = (i + 1) + BusinessConstants.MONTH;
                        collect.put(key, dbCollect.get(key));
                    }
                }
                return collect.entrySet()
                    .stream()
                    .map(entry -> CategoryEnergyData.builder()
                        .date(entry.getKey())
                        .value(getSumValue(getMonthValue(entry, dataType, devicePointData)))
                        .build())
                    .collect(Collectors.toList());
            }
        }
        return null;
    }

    private List<? extends ActualityObject> getValue(Map.Entry<String, List<CategoryEnergy>> entry,
        DetailDataEnum dataType, List<DevicePointData> devicePointData) {
        List<CategoryEnergy> value = entry.getValue();
        if (dataType == DetailDataEnum.WATER && CollectionUtils.isNotEmpty(value)) {
            List<String> codeList = devicePointData.stream()
                .filter(dpd -> dpd.getName().startsWith(dataType.getForwardTotal()))
                .map(DevicePointData::getCode)
                .collect(Collectors.toList());
            return value.stream()
                .filter(categoryEnergy -> codeList.contains(categoryEnergy.getCode()))
                .collect(Collectors.toList());
        }
        return value;
    }

    private List<? extends ActualityObject> getMonthValue(Map.Entry<String, List<CategoryEnergyMonth>> entry,
        DetailDataEnum dataType, List<DevicePointData> devicePointData) {
        List<CategoryEnergyMonth> value = entry.getValue();
        if (dataType == DetailDataEnum.WATER && CollectionUtils.isNotEmpty(value)) {
            List<String> codeList = devicePointData.stream()
                .filter(dpd -> dpd.getName().startsWith(dataType.getForwardTotal()))
                .map(DevicePointData::getCode)
                .collect(Collectors.toList());
            return value.stream()
                .filter(categoryEnergy -> codeList.contains(categoryEnergy.getCode()))
                .collect(Collectors.toList());
        }
        return value;
    }

    private void waterMonthProcess(List<DevicePointData> devicePointData, DetailDataEnum dataType,
        Map<String, List<CategoryEnergyMonth>> dbCollect) {
        if (dataType == DetailDataEnum.WATER) {
            Map<String, String> codeNameMap = devicePointData.stream()
                .collect(Collectors.toMap(DevicePointData::getCode, DevicePointData::getName, (o, o2) -> o2));
            dbCollect.forEach((k, v) -> {
                for (CategoryEnergyMonth categoryEnergyMonth : v) {
                    String code = categoryEnergyMonth.getCode();
                    String name = codeNameMap.get(code);
                    if (StringUtils.isNotBlank(name) && name.startsWith(dataType.getForwardTotal())) {
                        v.stream().filter(e -> {
                            String c = e.getCode();
                            String n = codeNameMap.get(c);
                            if (StringUtils.isNotBlank(n) && StringUtils.equals(n,
                                name.replace(dataType.getForwardTotal(), dataType.getReverseTotal()))) {
                                return Objects.equals(e.getDateIndex(), categoryEnergyMonth.getDateIndex())
                                    && Objects.equals(e.getYearIndex(), categoryEnergyMonth.getYearIndex());
                            }
                            return false;
                        })
                            .findFirst()
                            .ifPresent(e -> categoryEnergyMonth.setActuality(getRealValue(categoryEnergyMonth, e)));
                    }
                }
            });
        }
    }

    private void waterDayProcess(List<DevicePointData> devicePointData, DetailDataEnum dataType,
        Map<String, List<CategoryEnergy>> dbCollect) {
        if (dataType == DetailDataEnum.WATER) {
            Map<String, String> codeNameMap = devicePointData.stream()
                .collect(Collectors.toMap(DevicePointData::getCode, DevicePointData::getName, (o, o2) -> o2));
            dbCollect.forEach((k, v) -> {
                for (CategoryEnergy categoryEnergy : v) {
                    String code = categoryEnergy.getCode();
                    String name = codeNameMap.get(code);
                    if (StringUtils.isNotBlank(name) && name.startsWith(dataType.getForwardTotal())) {
                        v.stream().filter(e -> {
                            String c = e.getCode();
                            String n = codeNameMap.get(c);
                            if (StringUtils.isNotBlank(n) && StringUtils.equals(n,
                                name.replace(dataType.getForwardTotal(), dataType.getReverseTotal()))) {
                                return Objects.equals(e.getDateIndex(), categoryEnergy.getDateIndex())
                                    && Objects.equals(e.getHourIndex(), categoryEnergy.getHourIndex());
                            }
                            return false;
                        }).findFirst().ifPresent(e -> categoryEnergy.setActuality(getRealValue(categoryEnergy, e)));
                    }
                }
            });
        }
    }

    private String getRealValue(ActualityObject a, ActualityObject b) {
        String aActuality = a.getActuality();
        String bActuality = b.getActuality();
        if (StringUtils.isNotBlank(aActuality) && StringUtils.isNotBlank(bActuality)) {
            BigDecimal aBigDecimal = new BigDecimal(aActuality);
            BigDecimal bBigDecimal = new BigDecimal(bActuality).abs();
            return aBigDecimal.subtract(bBigDecimal).toString();
        }
        return null;
    }

    private List<String> getCodeList(DetailDataEnum dataType, List<DevicePointData> devicePointData) {
        return devicePointData.stream().filter(dpd -> {
            String name = dpd.getName();
            if (dataType == DetailDataEnum.WATER) {
                return name.startsWith(dataType.getForwardTotal()) || name.startsWith(dataType.getReverseTotal());
            } else if (dataType == DetailDataEnum.STEAM) {
                return name.endsWith(dataType.getForwardTotal());
            } else {
                return false;
            }
        }).map(DevicePointData::getCode).distinct().collect(Collectors.toList());
    }

    private static List<String> getSnList(List<DevicePointData> devicePointData) {
        return devicePointData.stream().map(DevicePointData::getSn).distinct().collect(Collectors.toList());
    }

    private String getFormattedMonthYear(Integer dateIndex) {
        String month = dateIndex.toString().substring(4);
        return Integer.valueOf(month) + BusinessConstants.MONTH;
    }

    private String getFormattedMonthByIdx(int i) {
        switch (i) {
            case 0:
                return "孟月";
            case 1:
                return "仲月";
            case 2:
                return "季月";
            default:
                return null;
        }
    }

    private String getFormattedMonth(Integer dateIndex, LocalDateTime startDate, LocalDateTime endDate) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(BusinessConstants.DateFormat.YEAR_MONTH);
        Integer start = Integer.valueOf(startDate.format(dtf));
        Integer end = Integer.valueOf(endDate.format(dtf));
        if (Objects.equals(dateIndex, end)) {
            return "季月";
        } else if (Objects.equals(dateIndex, start)) {
            return "孟月";
        } else if (Objects.equals(dateIndex, Integer.valueOf(startDate.plusMonths(1).format(dtf)))) {
            return "仲月";
        }
        return StringUtils.EMPTY;
    }

    private String getFormattedWeekDayForIdx(int i) {
        switch (i) {
            case 0:
                return "周一";
            case 1:
                return "周二";
            case 2:
                return "周三";
            case 3:
                return "周四";
            case 4:
                return "周五";
            case 5:
                return "周六";
            case 6:
                return "周天";
            default:
                return null;
        }
    }

    private String getSumValue(List<? extends ActualityObject> value) {
        if (CollectionUtils.isEmpty(value)) {
            return null;
        }
        return value.stream()
            .map(ActualityObject::getActuality)
            .filter(StringUtils::isNotBlank)
            .map(BigDecimal::new)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .toString();
    }

    private String getFormattedDay(Integer dateIndex) {
        return dateIndex.toString().substring(6);
    }

    private String getFormattedWeekDay(Integer dateIndex, LocalDateTime startDate, LocalDateTime endDate) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
        Integer start = Integer.valueOf(startDate.format(dtf));
        Integer end = Integer.valueOf(endDate.format(dtf));
        if (Objects.equals(dateIndex, end)) {
            return "周天";
        } else if (Objects.equals(dateIndex, start)) {
            return "周一";
        } else if (Objects.equals(dateIndex, Integer.valueOf(startDate.plusDays(1).format(dtf)))) {
            return "周二";
        } else if (Objects.equals(dateIndex, Integer.valueOf(startDate.plusDays(2).format(dtf)))) {
            return "周三";
        } else if (Objects.equals(dateIndex, Integer.valueOf(startDate.plusDays(3).format(dtf)))) {
            return "周四";
        } else if (Objects.equals(dateIndex, Integer.valueOf(startDate.plusDays(4).format(dtf)))) {
            return "周五";
        } else if (Objects.equals(dateIndex, Integer.valueOf(startDate.plusDays(5).format(dtf)))) {
            return "周六";
        }
        return StringUtils.EMPTY;
    }

    public String getFormattedHour(String hourIndex) {
        int hour = Integer.parseInt(hourIndex);
        return String.format("%02d:00", hour);
    }

    public String getFormattedDay(int day) {
        return String.format("%02d", day);
    }

    public List<CategoryEnergyData> queryTopDataByDevicePointData(List<DevicePointData> devicePointData,
        ChartQueryEnum dateType, DetailDataEnum dataType) {
        LocalDateTime startDate;
        LocalDateTime endDate;
        LocalDateTime now = LocalDateTime.now();
        if (dateType == ChartQueryEnum.DAY || dateType == ChartQueryEnum.WEEK || dateType == ChartQueryEnum.MONTH) {
            if (dateType == ChartQueryEnum.DAY) {
                // 今天开始到今天结束
                startDate = now.with(LocalTime.MIN);
                endDate = now.with(LocalTime.MAX);
            } else if (dateType == ChartQueryEnum.WEEK) {
                // 本周开始到本周结束
                startDate = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
                endDate = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).with(LocalTime.MAX);
            } else {
                // 本月开始到本月结束
                startDate = now.toLocalDate().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
                endDate = now.toLocalDate().with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
            }
            List<CategoryEnergy> categoryEnergies = this.categoryEnergyMapper.selectList(Wrappers
                .<CategoryEnergy>lambdaQuery()
                .in(CategoryEnergy::getSn, getSnList(devicePointData))
                .in(CategoryEnergy::getCode, getCodeList(dataType, devicePointData))
                .ge(CategoryEnergy::getDateIndex,
                    Integer.parseInt(LocalDateTimeUtil.format(startDate, DateTimeFormatter.BASIC_ISO_DATE)))
                .le(CategoryEnergy::getDateIndex,
                    Integer.parseInt(LocalDateTimeUtil.format(endDate, DateTimeFormatter.BASIC_ISO_DATE)))
                .ge(CategoryEnergy::getHourIndex,
                    LocalDateTimeUtil.format(startDate, BusinessConstants.DateFormat.HOUR))
                .le(CategoryEnergy::getHourIndex, LocalDateTimeUtil.format(endDate, BusinessConstants.DateFormat.HOUR))
                .orderByAsc(CategoryEnergy::getId));
            if (CollectionUtils.isNotEmpty(categoryEnergies)) {
                Map<String, String> codeNameMap = getCodeNameMap(devicePointData, dataType);
                Map<String, List<CategoryEnergy>> group = new HashMap<>();
                group.put(StringUtils.EMPTY, categoryEnergies);
                waterDayProcess(devicePointData, dataType, group);

                Map<String, List<CategoryEnergy>> collect = categoryEnergies.stream()
                    .collect(Collectors.groupingBy(categoryEnergy -> codeNameMap.get(categoryEnergy.getCode())));
                collect.remove(StringUtils.EMPTY);
                return collect.entrySet()
                    .stream()
                    .map(entry -> CategoryEnergyData.builder()
                        .date(entry.getKey())
                        .value(getSumValue(entry.getValue()))
                        .build())
                    .collect(Collectors.toList());
            }
        } else {
            if (dateType == ChartQueryEnum.QUARTER) {
                startDate = now.withMonth(now.toLocalDate().getMonth().firstMonthOfQuarter().getValue())
                    .with(TemporalAdjusters.firstDayOfMonth())
                    .with(LocalTime.MIN);
                endDate = now.withMonth(now.toLocalDate().getMonth().firstMonthOfQuarter().getValue() + 2)
                    .with(TemporalAdjusters.lastDayOfMonth())
                    .with(LocalTime.MAX);
            } else {
                startDate = now.withDayOfYear(1).with(LocalTime.MIN);
                endDate = now.with(TemporalAdjusters.lastDayOfYear()).with(LocalTime.MAX);
            }
            List<CategoryEnergyMonth> categoryEnergyMonths =
                this.monthMapper.selectList(Wrappers.<CategoryEnergyMonth>lambdaQuery()
                    .in(CategoryEnergyMonth::getSn, getSnList(devicePointData))
                    .in(CategoryEnergyMonth::getCode, getCodeList(dataType, devicePointData))
                    .ge(CategoryEnergyMonth::getYearIndex,
                        startDate.format(DateTimeFormatter.ofPattern(BusinessConstants.DateFormat.YEAR)))
                    .le(CategoryEnergyMonth::getYearIndex,
                        endDate.format(DateTimeFormatter.ofPattern(BusinessConstants.DateFormat.YEAR)))
                    .ge(CategoryEnergyMonth::getDateIndex,
                        Integer.valueOf(
                            startDate.format(DateTimeFormatter.ofPattern(BusinessConstants.DateFormat.YEAR_MONTH))))
                    .le(CategoryEnergyMonth::getDateIndex,
                        Integer.valueOf(
                            endDate.format(DateTimeFormatter.ofPattern(BusinessConstants.DateFormat.YEAR_MONTH))))
                    .orderByAsc(CategoryEnergyMonth::getId));
            if (CollectionUtils.isNotEmpty(categoryEnergyMonths)) {
                Map<String, String> codeNameMap = getCodeNameMap(devicePointData, dataType);
                Map<String, List<CategoryEnergyMonth>> group = new HashMap<>();
                group.put(StringUtils.EMPTY, categoryEnergyMonths);
                waterMonthProcess(devicePointData, dataType, group);

                Map<String, List<CategoryEnergyMonth>> collect = categoryEnergyMonths.stream()
                    .collect(
                        Collectors.groupingBy(categoryEnergyMonth -> codeNameMap.get(categoryEnergyMonth.getCode())));
                collect.remove(StringUtils.EMPTY);
                return collect.entrySet()
                    .stream()
                    .map(entry -> CategoryEnergyData.builder()
                        .date(entry.getKey())
                        .value(getSumValue(entry.getValue()))
                        .build())
                    .collect(Collectors.toList());
            }
        }
        return null;
    }

    private Map<String, String> getCodeNameMap(List<DevicePointData> devicePointData, DetailDataEnum dataType) {
        Pattern numberPattern = Pattern.compile("\\d+");
        return devicePointData.stream().collect(Collectors.toMap(DevicePointData::getCode, dpd -> {
            String dpdName = dpd.getName();
            String key = getKey(dpdName, numberPattern);
            if (dataType == DetailDataEnum.WATER && dpdName.equals(dataType.getForwardTotal() + key)) {
                return dataType.getNamePrefix() + key;
            } else if (dataType == DetailDataEnum.STEAM
                && dpdName.endsWith(key + SipaBootCommonConstants.EMPTY + dataType.getForwardTotal())) {
                return dataType.getNamePrefix() + key;
            }
            return StringUtils.EMPTY;
        }, (s, s2) -> s2));
    }

    private String getKey(String dpdName, Pattern numberPattern) {
        Matcher numMatcher = numberPattern.matcher(dpdName);
        if (numMatcher.find()) {
            return numMatcher.group().trim();
        }
        return StringUtils.EMPTY;
    }
}
