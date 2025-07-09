package com.lazzen.hec.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.lazzen.hec.constants.BusinessConstants;
import com.lazzen.hec.convert.DetailDataConvert;
import com.lazzen.hec.convert.DeviceDetailDataConvert;
import com.lazzen.hec.convert.SteamDetailDataConvert;
import com.lazzen.hec.convert.WaterDetailDataConvert;
import com.lazzen.hec.dto.*;
import com.lazzen.hec.enumeration.DetailDataEnum;
import com.lazzen.hec.form.*;
import com.lazzen.hec.po.DeviceOnlineStatus;
import com.lazzen.hec.po.DevicePointData;
import com.lazzen.hec.po.IotPointConf;
import com.lazzen.hec.repository.CommonRepository;
import com.lazzen.hec.repository.SmartManagementRepository;
import com.lazzen.hec.repository.StoreRepository;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.idev.excel.write.metadata.fill.FillConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ma.glasnost.orika.MapperFacade;

/**
 * @author guo
 */
@Service
@RequiredArgsConstructor
public class DeviceService {
    private final WaterDetailDataConvert waterConvert;

    private final SteamDetailDataConvert steamConvert;

    private final StoreRepository storeRepository;

    private final MapperFacade mapperFacade;

    private final SmartManagementRepository smartManagementRepository;

    private final CommonRepository commonRepository;

    /**
     * 获取该设备实时数据
     */
    public List<DeviceCurrentData> getImmediatelyBySn(String domainCode, String deviceType) {
        String sn = smartManagementRepository.assertSnByDomainCode(domainCode);
        List<DevicePointData> immediatelyBySn = storeRepository.getImmediatelyBySn(sn, deviceType);
        return DeviceDetailDataConvert.convertDpa(immediatelyBySn);
    }

    public Boolean getStatusByDomainCode(String domainCode) {
        String sn = smartManagementRepository.assertSnByDomainCode(domainCode);
        DeviceOnlineStatus statusBySn = storeRepository.getStatusBySn(sn);
        return DeviceDetailDataConvert.convertOnline(statusBySn);
    }

    /**
     * 水中控台数据集(水仪表)
     */
    public List<CurrentDetailData> currentDetailData(DetailForm form, DetailDataEnum dataEnum) {
        List<CurrentDetailData> currentDetailData = null;
        if (dataEnum == DetailDataEnum.WATER) {
            currentDetailData = currentDetailData(form, waterConvert, BusinessConstants.Water.SYB);
        }
        if (dataEnum == DetailDataEnum.STEAM) {
            currentDetailData = currentDetailData(form, steamConvert, BusinessConstants.Steam.QYB);
        }
        if (CollectionUtils.isNotEmpty(currentDetailData)) {
            currentDetailData
                .sort(Comparator.comparing(currentDetailData1 -> Long.parseLong(currentDetailData1.getId())));
        }
        return currentDetailData;
    }

    /**
     * 水中控台数据集(水仪表)
     */
    private List<CurrentDetailData> currentDetailData(DetailForm form, DetailDataConvert detailDataConvert,
        String deviceType) {
        String sn = smartManagementRepository.assertSnByDomainCode(form.getDomainCode());
        List<DevicePointData> immediatelyBySn = storeRepository.getImmediatelyBySn(sn, deviceType);
        if (immediatelyBySn.isEmpty()) {
            return null;
        }
        List<CurrentDetailData> currentDetailData = detailDataConvert.convertDetailData(immediatelyBySn);
        if (currentDetailData.isEmpty()) {
            return currentDetailData;
        }
        // 页面筛选
        if (StringUtils.isNotBlank(form.getDeviceName())) {
            Map<String, String> map = new HashMap<>(16);
            List<SqYbAliasDto> sqYbAliasDtos = new ArrayList<>();
            if (BusinessConstants.Water.SYB.equals(deviceType)) {
                sqYbAliasDtos = querySqAlias(1);
            } else if (BusinessConstants.Steam.QYB.equals(deviceType)) {
                sqYbAliasDtos = querySqAlias(2);
            }
            for (SqYbAliasDto sqYbAliasDto : sqYbAliasDtos) {
                map.put(sqYbAliasDto.getIdx() + StringUtils.EMPTY, sqYbAliasDto.getName());
            }
            for (CurrentDetailData currentDetailDatum : currentDetailData) {
                String id = currentDetailDatum.getId();
                currentDetailDatum.setCalcName(map.getOrDefault(id, currentDetailDatum.getName()));
            }
            currentDetailData.removeIf(e -> !e.getCalcName().contains(form.getDeviceName()));
        }
        if (form.getLink() != null) {
            currentDetailData.removeIf(e -> !Objects.equals(e.isLink(), form.getLink()));
        }
        return currentDetailData;
    }

    public List<EnergyData> historyCategoryEnergy(DataQueryForm form, String categoryType) {
        String sn = smartManagementRepository.assertSnByDomainCode(form.getDomainCode());
        Map<String, String> map = getAliasMap(form.getDataEnum());
        return storeRepository.listCategoryTotal(form, categoryType, sn, map);
    }

    public void historyCategoryEnergyExport(HttpServletResponse response, DataQueryForm form, String categoryType) {
        List<EnergyData> energyData = historyCategoryEnergy(form, categoryType);

        response.setContentType(BusinessConstants.EXCEL_EXPORT_CONTENT_TYPE);
        response.setHeader(BusinessConstants.CONTENT_DISPOSITION, "attachment; filename=" + getFileName());

        if (form.getDataEnum() == DetailDataEnum.WATER) {
            doWrite(energyData, response, form);
        } else {
            doWrite(energyData, response, form);
        }
    }

    @SneakyThrows
    private void doWrite(List<EnergyData> energyData, HttpServletResponse response, DataQueryForm form) {
        try (ExcelWriter excelWriter = FastExcel.write(response.getOutputStream())
            .withTemplate(DeviceService.class.getClassLoader().getResourceAsStream(getResourceName(form.getDataEnum())))
            .build()) {
            WriteSheet writeSheet = FastExcel.writerSheet().build();
            FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
            excelWriter.fill(energyData, fillConfig, writeSheet);
            Map<String, Object> map = new HashMap<>();
            map.put("startTime", LocalDateTimeUtil.format(form.getStartDate(), BusinessConstants.DateFormat.EXPORT));
            map.put("endTime", LocalDateTimeUtil.format(form.getEndDate(), BusinessConstants.DateFormat.EXPORT));
            excelWriter.fill(map, writeSheet);
        }
    }

    private String getResourceName(DetailDataEnum dataType) {
        return dataType == DetailDataEnum.WATER ? "template/water.xlsx" : "template/gas.xlsx";
    }

    private String getFileName() {
        return System.currentTimeMillis() + ".xls";
    }

    public List<ChartData> chart(ChartForm form) {
        List<DevicePointData> devicePointData = storeRepository.querySnFromPointData(form.getDataType());
        List<ChartData> chartDataList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(devicePointData)) {
            List<CategoryEnergyData> data = storeRepository.queryDataByDevicePointData(devicePointData,
                form.getDateType(), false, form.getDataType());
            List<CategoryEnergyData> previousData = storeRepository.queryDataByDevicePointData(devicePointData,
                form.getDateType(), true, form.getDataType());
            if (CollectionUtils.isNotEmpty(data)) {
                chartDataList.addAll(agg(data, previousData));
            }
        }
        return chartDataList;
    }

    private List<ChartData> agg(List<CategoryEnergyData> data, List<CategoryEnergyData> previousData) {
        List<ChartData> chartData = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(data)) {
            for (int i = 0; i < data.size(); i++) {
                CategoryEnergyData current = data.get(i);
                BigDecimal currentValue = getBd(current.getValue());
                BigDecimal qoq = calculateQoq(data, i);
                BigDecimal yoy = calculateYoy(data, i, previousData);
                chartData
                    .add(ChartData.builder().xName(current.getDate()).value(currentValue).qoq(qoq).yoy(yoy).build());
            }
        }
        return chartData;
    }

    private BigDecimal calculateQoq(List<CategoryEnergyData> categoryEnergyDataList, int currentIndex) {
        if (currentIndex == 0) {
            return null;
        }
        CategoryEnergyData previous = categoryEnergyDataList.get(currentIndex - 1);
        CategoryEnergyData current = categoryEnergyDataList.get(currentIndex);
        return calc(previous, current);
    }

    private BigDecimal calc(CategoryEnergyData previous, CategoryEnergyData current) {
        if (Objects.nonNull(previous) && Objects.nonNull(current)) {
            String preVal = previous.getValue();
            String curVal = current.getValue();
            if (StringUtils.isNotBlank(preVal) && StringUtils.isNotBlank(curVal)) {
                BigDecimal preValue = new BigDecimal(preVal);
                BigDecimal curValue = new BigDecimal(curVal);
                return calculateRingRatio(preValue, curValue);
            }
        }
        return null;
    }

    public BigDecimal calculateRingRatio(BigDecimal previous, BigDecimal current) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        BigDecimal difference = current.subtract(previous);
        return difference.multiply(BigDecimal.valueOf(100)).divide(previous, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateYoy(List<CategoryEnergyData> data, int currentIndex,
        List<CategoryEnergyData> previousData) {
        CategoryEnergyData previous = null;
        if (CollectionUtils.isNotEmpty(previousData)) {
            previous = previousData.get(currentIndex);
        }
        CategoryEnergyData current = data.get(currentIndex);
        return calc(previous, current);
    }

    private BigDecimal getBd(String val) {
        return StringUtils.isNotBlank(val) ? new BigDecimal(val) : null;
    }

    public List<ChartTopData> chartTop(ChartTopForm form) {
        List<DevicePointData> devicePointData = storeRepository.querySnFromPointData(form.getDataType());
        List<ChartTopData> chartDataList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(devicePointData)) {
            List<CategoryEnergyData> data =
                storeRepository.queryTopDataByDevicePointData(devicePointData, form.getDateType(), form.getDataType());
            if (CollectionUtils.isNotEmpty(data)) {
                chartDataList.addAll(top(data));
            }
        }
        if (CollectionUtils.isNotEmpty(chartDataList)) {
            String prefix = form.getDataType() == DetailDataEnum.WATER ? BusinessConstants.Water.NAME_PREFIX
                : BusinessConstants.Steam.NAME_PREFIX;
            Map<String, String> map = getAliasMap(form.getDataType());
            for (ChartTopData chartTopData : chartDataList) {
                String name = chartTopData.getName();
                String nameIdx = name.substring(prefix.length());
                String s = map.get(nameIdx);
                if (StringUtils.isNotBlank(s)) {
                    chartTopData.setName(s);
                }
            }
        }
        return chartDataList;
    }

    private Map<String, String> getAliasMap(DetailDataEnum dataType) {
        List<SqYbAliasDto> sqYbAliasDtos = querySqAlias(dataType == DetailDataEnum.WATER ? 1 : 2);
        Map<String, String> map = new HashMap<>(16);
        for (SqYbAliasDto sqYbAliasDto : sqYbAliasDtos) {
            map.put(sqYbAliasDto.getIdx() + StringUtils.EMPTY, sqYbAliasDto.getName());
        }
        return map;
    }

    private List<ChartTopData> top(List<CategoryEnergyData> data) {
        List<ChartTopData> chartTopData = new ArrayList<>();
        for (CategoryEnergyData datum : data) {
            chartTopData.add(ChartTopData.builder().name(datum.getDate()).value(datum.getValue()).build());
        }
        chartTopData.sort(Comparator.comparing(ChartTopData::getValue).reversed());
        return chartTopData;
    }

    @SneakyThrows
    public void paramExport(HttpServletResponse response, ParamExportForm form) {
        List<DeviceCurrentData> immediatelyBySn = getImmediatelyBySn(form.getDomainCode(), null);

        response.setContentType(BusinessConstants.EXCEL_EXPORT_CONTENT_TYPE);
        String param = form.getParam();
        String fileName = URLEncoder.encode(param + ".xlsx", StandardCharsets.UTF_8.name());
        response.setHeader(BusinessConstants.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

        if (CollectionUtils.isNotEmpty(immediatelyBySn)) {
            List<WaveformExport> list = new ArrayList<>();
            for (DeviceCurrentData immediatelyBySnData : immediatelyBySn.stream()
                .filter(deviceCurrentData -> deviceCurrentData.getName().contains(param))
                .sorted(Comparator.comparing(
                    deviceCurrentData -> Integer.parseInt(deviceCurrentData.getName().substring(param.length()))))
                .collect(Collectors.toList())) {
                WaveformExport waveformExport = new WaveformExport();
                waveformExport.setName(immediatelyBySnData.getName());
                waveformExport.setValue(immediatelyBySnData.getValue());
                list.add(waveformExport);
            }
            FastExcel.write(response.getOutputStream(), WaveformExport.class).sheet(param).doWrite(list);
        }
    }

    public List<SqYbAliasDto> querySqAlias(int type) {
        return smartManagementRepository.sqAlias(type);
    }

    public Boolean saveSqAlias(SqYbAliasForm form) {
        return smartManagementRepository.saveSqAlias(form);
    }

    public CurveDto curve(CurveForm form) {
        String domainCode = form.getDomainCode();
        String deviceType = form.getDeviceType();
        String sn = smartManagementRepository.assertSnByDomainCode(domainCode);
        List<IotPointConf> legendData = commonRepository.queryLegendData(deviceType);
        CurveDto.CurveDtoBuilder builder = CurveDto.builder();
        if (CollectionUtils.isNotEmpty(legendData)) {
            builder.legendData(legendData.stream().map(IotPointConf::getPointName).collect(Collectors.toList()));
            List<LocalDateTime> localDateTimes = generateOneDayLocalDateTimes();
            builder.xAxisData(localDateTimes.stream()
                .map(localDateTime -> LocalDateTimeUtil.format(localDateTime,
                    DateTimeFormatter.ofPattern(BusinessConstants.DateFormat.DT)))
                .collect(Collectors.toList()));
            builder.seriesData(storeRepository.queryCurve(localDateTimes, sn, legendData));
        }
        return builder.build();
    }

    private List<LocalDateTime> generateOneDayLocalDateTimes() {
        List<LocalDateTime> localDateTimes = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        int adjustedMinute = (now.getMinute() / 15) * 15;
        LocalDateTime start = now.withMinute(adjustedMinute).withSecond(0).withNano(0).minusDays(1);
        for (int i = 0; i < 97; i++) {
            localDateTimes.add(start.plusMinutes(15 * i));
        }
        return localDateTimes;
    }
}
