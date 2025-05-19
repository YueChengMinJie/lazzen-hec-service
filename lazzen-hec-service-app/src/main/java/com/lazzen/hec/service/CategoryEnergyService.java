package com.lazzen.hec.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lazzen.hec.convert.GwmpcwgDataConvert;
import com.lazzen.hec.dto.GwmpcwgData;
import com.lazzen.hec.po.DevicePointData;
import com.lazzen.hec.repository.SmartManagementRepository;
import com.lazzen.hec.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

/**
 * @author guo
 * @description 针对表【category_energy】的数据库操作Service实现
 * @createDate 2025-05-18 21:59:34
 */
@Service
@RequiredArgsConstructor
public class CategoryEnergyService {
    private final StoreRepository storeRepository;

    private final SmartManagementRepository smartManagementRepository;

    /**
     * 获取该设备实时数据
     * 
     * @param domainCode
     * @return
     * @throws Exception
     */
    public List<GwmpcwgData> getImmediatelyBySn(String domainCode, String deviceType) throws Exception {
        String sn = smartManagementRepository.assertSnByDomainCode(domainCode);
        List<DevicePointData> immediatelyBySn = storeRepository.getImmediatelyBySn(sn, deviceType);
        return GwmpcwgDataConvert.convert(immediatelyBySn);
    }
}
