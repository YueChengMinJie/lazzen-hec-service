package com.lazzen.hec.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lazzen.hec.constants.BusinessConstants;
import com.lazzen.hec.convert.*;
import com.lazzen.hec.dto.*;
import com.lazzen.hec.enumeration.DetailDataEnum;
import com.lazzen.hec.form.ChartForm;
import com.lazzen.hec.form.DataQueryForm;
import com.lazzen.hec.form.DetailForm;
import com.lazzen.hec.po.CategoryEnergy;
import com.lazzen.hec.po.DeviceOnlineStatus;
import com.lazzen.hec.po.DevicePointData;
import com.lazzen.hec.repository.SmartManagementRepository;
import com.lazzen.hec.repository.StoreRepository;
import com.sipa.boot.java8.common.utils.StringUtils;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.idev.excel.FastExcel;
import lombok.RequiredArgsConstructor;
import ma.glasnost.orika.MapperFacade;

/**
 * @author guo
 * @description 针对表【category_energy】的数据库操作Service实现
 * @createDate 2025-05-18 21:59:34
 */
@Service
@RequiredArgsConstructor
public class DeviceService {
    private final WaterDetailDataConvert waterConvert;

    private final SteamDetailDataConvert steamConvert;

    private final StoreRepository storeRepository;

    private final MapperFacade mapperFacade;

    private final SmartManagementRepository smartManagementRepository;

    private final SpringUtil springUtil;

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
        if (dataEnum == DetailDataEnum.WATER) {
            return currentDetailData(form, waterConvert, BusinessConstants.Water.SYB);
        }
        if (dataEnum == DetailDataEnum.STEAM) {
            return currentDetailData(form, steamConvert, BusinessConstants.Steam.QYB);
        }
        return null;
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
        if (!StringUtils.isNullOrEmpty(form.getDeviceName())) {
            currentDetailData.removeIf(e -> !e.getName().contains(form.getDeviceName()));
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
        return null;
    }
}
