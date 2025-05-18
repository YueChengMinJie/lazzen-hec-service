package com.lazzen.hec.service;

import com.lazzen.hec.convert.GwmpcwgDataConvert;
import com.lazzen.hec.dto.GwmpcwgData;
import com.lazzen.hec.po.DeviceBusinessPointSurvey;
import com.lazzen.hec.repository.BreakerRepository;
import com.lazzen.hec.repository.IotRepository;
import com.sipa.boot.java8.common.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author guo
* @description 针对表【category_energy】的数据库操作Service实现
* @createDate 2025-05-18 21:59:34
*/
@Service
@RequiredArgsConstructor
public class CategoryEnergyService  {
    private final IotRepository iotRepository;
    private final BreakerRepository breakerRepository;

    /**
     * 获取该设备实时数据
     * @param domainCode
     * @return
     * @throws Exception
     */
    public List<GwmpcwgData> getImmediatelyBySn(String domainCode) throws Exception{
        String sn = breakerRepository.assertSnByDomainCode(domainCode);
        List<DeviceBusinessPointSurvey> immediatelyBySn = iotRepository.getImmediatelyBySn(sn);
        return GwmpcwgDataConvert.convert(immediatelyBySn);
    }

}




