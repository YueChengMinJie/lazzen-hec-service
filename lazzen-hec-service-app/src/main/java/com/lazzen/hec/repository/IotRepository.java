package com.lazzen.hec.repository;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lazzen.hec.convert.GwmpcwgDataConvert;
import com.lazzen.hec.dto.GwmpcwgData;
import com.lazzen.hec.mapper.CategoryEnergyMapper;
import com.lazzen.hec.mapper.CategoryEnergyMonthMapper;
import com.lazzen.hec.mapper.DeviceBusinessPointSurveyMapper;
import com.lazzen.hec.po.DeviceBusinessPointSurvey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@DS("144iot")
public class IotRepository {
    private final CategoryEnergyMapper categoryEnergyMapper;
    private final CategoryEnergyMonthMapper monthMapper;
    private final DeviceBusinessPointSurveyMapper immediatelyMapper;


    public List<DeviceBusinessPointSurvey> getImmediatelyBySn(String sn) {
        return immediatelyMapper.selectList(
                new LambdaQueryWrapper<DeviceBusinessPointSurvey>()
                        .eq(DeviceBusinessPointSurvey::getSn, sn)
        );
    }
}
