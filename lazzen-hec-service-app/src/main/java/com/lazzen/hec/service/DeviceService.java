package com.lazzen.hec.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lazzen.hec.convert.CategoryConvert;
import com.lazzen.hec.convert.DetailDataConvert;
import com.lazzen.hec.convert.DeviceDetailDataConvert;
import com.lazzen.hec.dto.CategoryEnergyData;
import com.lazzen.hec.dto.ChartData;
import com.lazzen.hec.dto.CurrentDetailData;
import com.lazzen.hec.dto.DeviceCurrentData;
import com.lazzen.hec.enumeration.ChartQueryEnum;
import com.lazzen.hec.form.DataQueryForm;
import com.lazzen.hec.form.DetailForm;
import com.lazzen.hec.po.CategoryEnergy;
import com.lazzen.hec.po.DeviceOnlineStatus;
import com.lazzen.hec.po.DevicePointData;
import com.lazzen.hec.repository.SmartManagementRepository;
import com.lazzen.hec.repository.StoreRepository;
import com.sipa.boot.java8.common.utils.StringUtils;

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
    public List<CurrentDetailData> currentDetailData(DetailForm form, DetailDataConvert detailDataConvert,
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
        Page<CategoryEnergy> categoryEnergyPage = storeRepository.pageCategory(form, categoryType, sn);
        List<CategoryEnergyData> convert = CategoryConvert.convert(categoryEnergyPage.getRecords());
        Page<CategoryEnergyData> map = mapperFacade.map(categoryEnergyPage, Page.class);
        map.setRecords(convert);
        return map;
    }

    public List<ChartData> chart(ChartQueryEnum form, String category) {
        switch (form) {
            case DAY:
                break;
            case WEEK:
                // 本周
                break;
            case MONTH:
                break;
            case YEAR:
                break;
        }
        return null;
    }
}
