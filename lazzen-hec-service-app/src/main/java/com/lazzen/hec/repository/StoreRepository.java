package com.lazzen.hec.repository;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lazzen.hec.constants.BusinessConstants;
import com.lazzen.hec.convert.CategoryConvert;
import com.lazzen.hec.dto.CategoryEnergyData;
import com.lazzen.hec.dto.EnergyData;
import com.lazzen.hec.enumeration.ChartQueryEnum;
import com.lazzen.hec.enumeration.DetailDataEnum;
import com.lazzen.hec.form.AnalyseForm;
import com.lazzen.hec.form.DataQueryForm;
import com.lazzen.hec.mapper.*;
import com.lazzen.hec.po.*;
import com.lazzen.hec.po.base.ActualityObject;
import com.sipa.boot.java8.common.archs.threadpool.pojo.Tuple2;
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

    private final DeviceBusinessPointSurveyMapper deviceBusinessPointSurveyMapper;

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

    public List<EnergyData> listCategoryTotal(DataQueryForm form, String categoryType, String sn,
        Map<String, String> aliasMap) {
        List<EnergyData> list = new ArrayList<>();
        for (int i = 0; i < form.getIds().size(); i++) {
            String id = form.getIds().get(i);
            String forwardPointCode = form.getForwardPointCodes().get(i);
            List<CategoryEnergy> forward = listCategory(form, forwardPointCode, categoryType, sn);
            String reversePointCode = null;
            if (CollectionUtils.isNotEmpty(form.getReversePointCodes())) {
                reversePointCode = form.getReversePointCodes().get(i);
            }
            if (form.getDataEnum() == DetailDataEnum.WATER && StringUtils.isNotBlank(reversePointCode)
                && CollectionUtils.isNotEmpty(forward)) {
                // 水 减去反向流量
                List<CategoryEnergy> reverse = listCategory(form, reversePointCode, categoryType, sn);
                if (CollectionUtils.isNotEmpty(reverse)) {
                    Map<String,
                        String> reverseMap = reverse.stream()
                            .collect(Collectors.toMap(e -> e.getDateIndex() + e.getHourIndex(),
                                CategoryEnergy::getRelaTimeValue, (s, s2) -> s2));
                    for (CategoryEnergy record : forward) {
                        String reverseValue = reverseMap.get(record.getDateIndex() + record.getHourIndex());
                        if (StringUtils.isBlank(reverseValue)) {
                            continue;
                        }
                        String gap = getGap(record.getRelaTimeValue(), reverseValue);
                        record.setRelaTimeValue(gap);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(forward)) {
                EnergyData categoryEnergyData = new EnergyData();
                categoryEnergyData.setId(id);
                categoryEnergyData.setName(getNameById(id, aliasMap, form.getDataEnum()));
                String start = forward.get(0).getRelaTimeValue();
                categoryEnergyData.setStart(start);
                String end = forward.get(forward.size() - 1).getRelaTimeValue();
                categoryEnergyData.setEnd(end);
                categoryEnergyData.setGap(getGap(end, start));
                list.add(categoryEnergyData);
            }
        }
        return list;
    }

    private String getNameById(String id, Map<String, String> aliasMap, @NotNull DetailDataEnum dataEnum) {
        return aliasMap.getOrDefault(id, dataEnum.getNamePrefix() + id);
    }

    private static String getGap(String start, String end) {
        BigDecimal num1 = NumberUtil.toBigDecimal(start);
        BigDecimal num2 = NumberUtil.toBigDecimal(end).abs();
        String gap = NumberUtil.sub(num1, num2).toString();
        return gap;
    }

    private List<CategoryEnergy> listCategory(DataQueryForm form, String pointCode, String categoryType, String sn) {
        LambdaQueryWrapper<CategoryEnergy> minQueryWrapper =
            getListCategoryMapper(form, pointCode, categoryType, sn, true);
        LambdaQueryWrapper<CategoryEnergy> maxQueryWrapper =
            getListCategoryMapper(form, pointCode, categoryType, sn, false);
        CategoryEnergy min = categoryEnergyMapper.selectOne(minQueryWrapper);
        CategoryEnergy max = categoryEnergyMapper.selectOne(maxQueryWrapper);
        if (min != null && max != null) {
            return Arrays.asList(min, max);
        }
        return Collections.emptyList();
    }

    private static LambdaQueryWrapper<CategoryEnergy> getListCategoryMapper(DataQueryForm form, String pointCode,
        String categoryType, String sn, boolean min) {
        LambdaQueryWrapper<CategoryEnergy> queryWrapper = Wrappers.<CategoryEnergy>lambdaQuery()
            .eq(CategoryEnergy::getCategory, categoryType)
            .eq(CategoryEnergy::getCode, pointCode)
            .eq(CategoryEnergy::getSn, sn);
        if (form.getStartDate() != null) {
            String formattedDate = form.getStartDate().format(CategoryConvert.dateFormatter);
            int dateNumber = Integer.parseInt(formattedDate);
            queryWrapper.and(wrapper -> wrapper.gt(CategoryEnergy::getDateIndex, dateNumber)
                .or(wrapper2 -> wrapper2.eq(CategoryEnergy::getDateIndex, dateNumber)
                    .ge(CategoryEnergy::getHourIndex, String.format("%02d", form.getStartDate().getHour()))));
        }
        if (form.getEndDate() != null) {
            String formattedDate = form.getEndDate().format(CategoryConvert.dateFormatter);
            int dateNumber = Integer.parseInt(formattedDate);
            queryWrapper.and(wrapper -> wrapper.lt(CategoryEnergy::getDateIndex, dateNumber)
                .or(wrapper2 -> wrapper2.eq(CategoryEnergy::getDateIndex, dateNumber)
                    .le(CategoryEnergy::getHourIndex, String.format("%02d", form.getStartDate().getHour()))));
        }
        if (min) {
            queryWrapper.orderByAsc(CategoryEnergy::getDateIndex);
            queryWrapper.orderByAsc(CategoryEnergy::getHourIndex);
            queryWrapper.last(SipaBootMysqlConstants.LIMIT_ONE);
        } else {
            queryWrapper.orderByDesc(CategoryEnergy::getDateIndex);
            queryWrapper.orderByDesc(CategoryEnergy::getHourIndex);
            queryWrapper.last(SipaBootMysqlConstants.LIMIT_ONE);
        }
        return queryWrapper;
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
                .in(CategoryEnergy::getGroupCode, getGroupCodeList(dataType, devicePointData))
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
                    .in(CategoryEnergyMonth::getGroupCode, getGroupCodeList(dataType, devicePointData))
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

    private List<String> getGroupCodeList(DetailDataEnum dataType, List<DevicePointData> devicePointData) {
        return devicePointData.stream().filter(dpd -> {
            String name = dpd.getName();
            if (dataType == DetailDataEnum.WATER) {
                return name.startsWith(dataType.getForwardTotal()) || name.startsWith(dataType.getReverseTotal());
            } else if (dataType == DetailDataEnum.STEAM) {
                return name.endsWith(dataType.getForwardTotal());
            } else {
                return false;
            }
        }).map(DevicePointData::getUnCode).distinct().collect(Collectors.toList());
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
                .in(CategoryEnergy::getGroupCode, getGroupCodeList(dataType, devicePointData))
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
                    .in(CategoryEnergyMonth::getGroupCode, getGroupCodeList(dataType, devicePointData))
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

    public List<List<String>> queryCurve(List<LocalDateTime> localDateTimes, String sn, List<IotPointConf> points) {
        LocalDateTime start = localDateTimes.get(0);
        LocalDateTime end = localDateTimes.get(localDateTimes.size() - 1);
        List<DeviceBusinessPointSurvey> deviceBusinessPointSurveys =
            deviceBusinessPointSurveyMapper.selectList(Wrappers.<DeviceBusinessPointSurvey>lambdaQuery()
                .eq(DeviceBusinessPointSurvey::getSn, sn)
                .ge(DeviceBusinessPointSurvey::getIntervalStart, LocalDateTimeUtil.toEpochMilli(start))
                .le(DeviceBusinessPointSurvey::getIntervalStart, LocalDateTimeUtil.toEpochMilli(end)));
        List<List<String>> result = new ArrayList<>();
        for (IotPointConf point : points) {
            result.add(new ArrayList<>());
        }
        if (CollectionUtils.isNotEmpty(deviceBusinessPointSurveys)) {
            Map<Long, List<DeviceBusinessPointSurvey>> map = deviceBusinessPointSurveys.stream()
                .collect(Collectors.groupingBy(DeviceBusinessPointSurvey::getIntervalStart));
            for (LocalDateTime localDateTime : localDateTimes) {
                long ts = LocalDateTimeUtil.toEpochMilli(localDateTime);
                List<DeviceBusinessPointSurvey> deviceBusinessPointSurveyList = map.get(ts);
                if (CollectionUtils.isNotEmpty(deviceBusinessPointSurveyList)) {
                    Map<String, DeviceBusinessPointSurvey> collect = deviceBusinessPointSurveyList.stream()
                        .collect(Collectors.toMap(DeviceBusinessPointSurvey::getCode, e -> e, (t, t2) -> t2));
                    for (int i = 0; i < points.size(); i++) {
                        IotPointConf point = points.get(i);
                        String pointCode = point.getPointCode();
                        DeviceBusinessPointSurvey deviceBusinessPointSurvey = collect.get(pointCode);
                        if (deviceBusinessPointSurvey == null) {
                            result.get(i).add(null);
                        } else {
                            result.get(i).add(deviceBusinessPointSurvey.getValue());
                        }
                    }
                } else {
                    for (int i = 0; i < points.size(); i++) {
                        result.get(i).add(null);
                    }
                }
            }
        }
        return result;
    }

    public Tuple2<List<String>, List<String>> analyseData(AnalyseForm form, boolean previous, String sn) {
        LocalDateTime startDate;
        LocalDateTime endDate;
        LocalDateTime now = LocalDateTime.now();
        DetailDataEnum dataType = form.getDataType();
        ChartQueryEnum dateType = form.getDateType();

        List<String> codes = new ArrayList<>();
        codes.add(form.getForwardPointCode());
        if (dataType == DetailDataEnum.WATER) {
            codes.add(form.getReversePointCode());
        }

        if (previous && dateType == ChartQueryEnum.DAY) {
            now = now.minusDays(1);
        } else if (previous && dateType == ChartQueryEnum.MONTH) {
            now = now.minusMonths(1);
        } else if (previous && dateType == ChartQueryEnum.YEAR) {
            now = now.minusYears(1);
        }

        if (dateType == ChartQueryEnum.DAY || dateType == ChartQueryEnum.MONTH) {
            if (dateType == ChartQueryEnum.DAY) {
                startDate = now.with(LocalTime.MIN);
                endDate = now.plusHours(1).withMinute(0).withSecond(0).withNano(0);
            } else {
                startDate = now.toLocalDate().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
                endDate = now.toLocalDate().with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
            }
            List<CategoryEnergy> categoryEnergies = this.categoryEnergyMapper.selectList(Wrappers
                .<CategoryEnergy>lambdaQuery()
                .eq(CategoryEnergy::getSn, sn)
                .in(CategoryEnergy::getCode, codes)
                .ge(CategoryEnergy::getDateIndex,
                    Integer.parseInt(LocalDateTimeUtil.format(startDate, DateTimeFormatter.BASIC_ISO_DATE)))
                .le(CategoryEnergy::getDateIndex,
                    Integer.parseInt(LocalDateTimeUtil.format(endDate, DateTimeFormatter.BASIC_ISO_DATE)))
                .ge(CategoryEnergy::getHourIndex,
                    LocalDateTimeUtil.format(startDate, BusinessConstants.DateFormat.HOUR))
                .le(CategoryEnergy::getHourIndex, LocalDateTimeUtil.format(endDate, BusinessConstants.DateFormat.HOUR))
                .orderByAsc(CategoryEnergy::getId));
            if (dateType == ChartQueryEnum.DAY) {
                List<LocalDateTime> times = new ArrayList<>();
                LocalDateTime current = startDate;
                while (!current.isAfter(endDate)) {
                    times.add(current);
                    current = current.plusHours(1);
                }

                Map<String, List<CategoryEnergy>> map =
                    categoryEnergies.stream().collect(Collectors.groupingBy(categoryEnergy -> {
                        Long deviceTime = categoryEnergy.getDeviceTime();
                        LocalDateTime localDateTime =
                            Instant.ofEpochMilli(deviceTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
                        return LocalDateTimeUtil.format(localDateTime, BusinessConstants.DateFormat.HOUR) + ":00";
                    }));

                List<String> xList = new ArrayList<>();
                List<String> yList = new ArrayList<>();
                for (LocalDateTime ldt : times) {
                    String key = LocalDateTimeUtil.format(ldt, BusinessConstants.DateFormat.HOUR) + ":00";
                    xList.add(key);
                    if (map.containsKey(key)) {
                        List<CategoryEnergy> energies = map.get(key);
                        yList.add(getSumValue(energies));
                    } else {
                        yList.add(null);
                    }
                }
                return new Tuple2<>(xList, yList);
            } else {
                List<LocalDateTime> times = new ArrayList<>();
                LocalDateTime current = startDate;
                while (!current.isAfter(endDate)) {
                    times.add(current);
                    current = current.plusDays(1);
                }

                Map<String, List<CategoryEnergy>> map =
                    categoryEnergies.stream().collect(Collectors.groupingBy(categoryEnergy -> {
                        Long deviceTime = categoryEnergy.getDeviceTime();
                        LocalDateTime localDateTime =
                            Instant.ofEpochMilli(deviceTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
                        return LocalDateTimeUtil.format(localDateTime, BusinessConstants.DateFormat.YEAR_MONTH_DAY);
                    }));

                List<String> xList = new ArrayList<>();
                List<String> yList = new ArrayList<>();
                for (LocalDateTime ldt : times) {
                    String key = LocalDateTimeUtil.format(ldt, BusinessConstants.DateFormat.YEAR_MONTH_DAY);
                    xList.add(key);
                    if (map.containsKey(key)) {
                        List<CategoryEnergy> energies = map.get(key);
                        yList.add(getSumValue(energies));
                    } else {
                        yList.add(null);
                    }
                }
                return new Tuple2<>(xList, yList);
            }
        } else {
            startDate = now.withDayOfYear(1).with(LocalTime.MIN);
            endDate = now.with(LocalTime.MAX);
            List<CategoryEnergyMonth> categoryEnergyMonths =
                this.monthMapper.selectList(Wrappers.<CategoryEnergyMonth>lambdaQuery()
                    .eq(CategoryEnergyMonth::getSn, sn)
                    .in(CategoryEnergyMonth::getCode, codes)
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
            List<LocalDateTime> times = new ArrayList<>();
            LocalDateTime current = startDate;
            while (!current.isAfter(endDate)) {
                times.add(current);
                current = current.plusMonths(1);
            }

            Map<String, List<CategoryEnergyMonth>> map =
                categoryEnergyMonths.stream().collect(Collectors.groupingBy(categoryEnergyMonth -> {
                    Long deviceTime = categoryEnergyMonth.getDeviceTime();
                    LocalDateTime localDateTime =
                        Instant.ofEpochMilli(deviceTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
                    return LocalDateTimeUtil.format(localDateTime, BusinessConstants.DateFormat.YEAR_MONTH_WITH_ACROSS);
                }));

            List<String> xList = new ArrayList<>();
            List<String> yList = new ArrayList<>();
            for (LocalDateTime ldt : times) {
                String key = LocalDateTimeUtil.format(ldt, BusinessConstants.DateFormat.YEAR_MONTH_WITH_ACROSS);
                xList.add(key);
                if (map.containsKey(key)) {
                    List<CategoryEnergyMonth> energies = map.get(key);
                    yList.add(getSumValue(energies));
                } else {
                    yList.add(null);
                }
            }
            return new Tuple2<>(xList, yList);
        }
    }
}
