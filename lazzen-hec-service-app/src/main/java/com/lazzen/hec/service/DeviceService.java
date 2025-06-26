package com.lazzen.hec.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lazzen.hec.constants.BusinessConstants;
import com.lazzen.hec.convert.*;
import com.lazzen.hec.dto.*;
import com.lazzen.hec.enumeration.DetailDataEnum;
import com.lazzen.hec.form.*;
import com.lazzen.hec.po.CategoryEnergy;
import com.lazzen.hec.po.DeviceOnlineStatus;
import com.lazzen.hec.po.DevicePointData;
import com.lazzen.hec.repository.SmartManagementRepository;
import com.lazzen.hec.repository.StoreRepository;

import cn.hutool.core.util.NumberUtil;
import cn.idev.excel.FastExcel;
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

    public Page<CategoryEnergyData> historyCategoryEnergy(DataQueryForm form, String categoryType) {
        String sn = smartManagementRepository.assertSnByDomainCode(form.getDomainCode());
        Page<CategoryEnergy> categoryEnergyPage = storeRepository.pageCategoryTotal(form, categoryType, sn);
        List<CategoryEnergyData> convert = CategoryConvert.convert(categoryEnergyPage.getRecords());
        Page<CategoryEnergyData> map = mapperFacade.map(categoryEnergyPage, Page.class);
        map.setRecords(convert);
        return map;
    }

    public void historyCategoryEnergyExport(HttpServletResponse response, DataQueryForm form, String categoryType)
        throws IOException {
        form.setSize(Integer.MAX_VALUE);
        Page<CategoryEnergyData> categoryEnergyDataPage = historyCategoryEnergy(form, categoryType);

        response.setContentType(BusinessConstants.EXCEL_EXPORT_CONTENT_TYPE);
        response.setHeader(BusinessConstants.CONTENT_DISPOSITION, "attachment; filename=" + getFileName(form));

        if (form.getDataEnum() == DetailDataEnum.WATER) {
            List<CategoryEnergyWaterExport> list =
                mapperFacade.mapAsList(categoryEnergyDataPage.getRecords(), CategoryEnergyWaterExport.class);
            computeWaterSubValue(list);
            doWrite(list, CategoryEnergyWaterExport.class, response,
                BusinessConstants.Water.NAME_PREFIX + form.getId());
        } else {
            List<CategoryEnergySteamExport> list =
                mapperFacade.mapAsList(categoryEnergyDataPage.getRecords(), CategoryEnergySteamExport.class);
            computeSteamSubValue(list);
            doWrite(list, CategoryEnergySteamExport.class, response,
                BusinessConstants.Steam.NAME_PREFIX + form.getId());
        }
    }

    private <T> void doWrite(List<T> list, Class<T> clazz, HttpServletResponse response, String sheetName)
        throws IOException {
        FastExcel.write(response.getOutputStream(), clazz).sheet(sheetName).doWrite(list);
    }

    private String getFileName(DataQueryForm form) {
        String filename = "";
        if (form.getStartDate() != null) {
            filename += CategoryConvert.dateFormatter.format(form.getStartDate());
        }
        if (form.getEndDate() != null) {
            if (!filename.isEmpty()) {
                filename += "-";
            }
            filename += CategoryConvert.dateFormatter.format(form.getEndDate());
        }
        if (filename.isEmpty()) {
            filename += CategoryConvert.dateFormatter.format(LocalDateTime.now());
        }
        filename += ".xls";
        return filename;
    }

    private void computeWaterSubValue(List<CategoryEnergyWaterExport> list) {
        for (int i = 0; i < list.size(); i++) {
            CategoryEnergyWaterExport a = list.get(i);
            if (Objects.equals(i + 1, list.size())) {
                a.setSubValue("--");
            } else {
                BigDecimal num1 = NumberUtil.toBigDecimal(a.getValue());
                BigDecimal num2 = NumberUtil.toBigDecimal(list.get(i + 1).getValue());
                a.setSubValue(NumberUtil.sub(num1, num2).toString());
            }
        }
    }

    private void computeSteamSubValue(List<CategoryEnergySteamExport> list) {
        for (int i = 0; i < list.size(); i++) {
            CategoryEnergySteamExport a = list.get(i);
            if (Objects.equals(i + 1, list.size())) {
                a.setSubValue("--");
            } else {
                BigDecimal num1 = NumberUtil.toBigDecimal(a.getValue());
                BigDecimal num2 = NumberUtil.toBigDecimal(list.get(i + 1).getValue());
                a.setSubValue(NumberUtil.sub(num1, num2).toString());
            }
        }
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
            List<SqYbAliasDto> sqYbAliasDtos = querySqAlias(form.getDataType() == DetailDataEnum.WATER ? 1 : 2);
            Map<String, String> map = new HashMap<>(16);
            for (SqYbAliasDto sqYbAliasDto : sqYbAliasDtos) {
                map.put(sqYbAliasDto.getIdx() + StringUtils.EMPTY, sqYbAliasDto.getName());
            }
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
}
