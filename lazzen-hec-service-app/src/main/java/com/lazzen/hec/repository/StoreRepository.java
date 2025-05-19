package com.lazzen.hec.repository;

import java.util.List;

import org.springframework.stereotype.Component;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lazzen.hec.mapper.*;
import com.lazzen.hec.po.DeviceOnlineStatus;
import com.lazzen.hec.po.DevicePointData;
import com.sipa.boot.java8.common.utils.StringUtils;
import com.sipa.boot.java8.data.mysql.constants.SipaBootMysqlConstants;

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

    private final DeviceBusinessPointSurveyMapper deviceBusinessPointSurveyMapper;

    private final DeviceOnlineStatusMapper deviceOnlineStatusMapper;

    private final DevicePointDataMapper immediatelyMapper;

    public List<DevicePointData> getImmediatelyBySn(String sn, String deviceType) {
        return immediatelyMapper.selectList(new LambdaQueryWrapper<DevicePointData>().eq(DevicePointData::getSn, sn)
            .eq(!StringUtils.isNullOrEmpty(deviceType), DevicePointData::getDeviceType, deviceType)
            .orderByAsc(DevicePointData::getCode));
    }

    public DeviceOnlineStatus getStatusBySn(String sn) {
        return deviceOnlineStatusMapper.selectOne(Wrappers.<DeviceOnlineStatus>lambdaQuery()
            .eq(DeviceOnlineStatus::getSn, sn)
            .orderByDesc(DeviceOnlineStatus::getId)
            .last(SipaBootMysqlConstants.LIMIT_ONE));
    }
}
