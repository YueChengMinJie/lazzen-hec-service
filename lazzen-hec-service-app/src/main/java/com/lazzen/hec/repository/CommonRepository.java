package com.lazzen.hec.repository;

import java.util.List;

import org.springframework.stereotype.Component;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lazzen.hec.mapper.IotPointConfMapper;
import com.lazzen.hec.po.IotPointConf;

import lombok.RequiredArgsConstructor;

/**
 * @author guo
 * @createDate 2025-05-18 21:59:34
 */
@Component
@DS("common")
@RequiredArgsConstructor
public class CommonRepository {
    private final IotPointConfMapper iotPointConfMapper;

    public List<IotPointConf> queryLegendData(String deviceType) {
        return iotPointConfMapper.selectList(Wrappers.<IotPointConf>lambdaQuery()
            .eq(IotPointConf::getProtocolCode, deviceType)
            .eq(IotPointConf::getCurveFlag, 1)
            .orderByAsc(IotPointConf::getPointCode));
    }
}
