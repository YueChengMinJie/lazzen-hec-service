package com.lazzen.hec.repository;

import java.util.List;

import org.springframework.stereotype.Component;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lazzen.hec.form.DataQueryForm;
import com.lazzen.hec.mapper.*;
import com.lazzen.hec.po.CategoryEnergy;
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

    public Page<CategoryEnergy> pageCategory(DataQueryForm form, String categoryType, String sn) {
        LambdaQueryWrapper<CategoryEnergy> queryWrapper = Wrappers.<CategoryEnergy>lambdaQuery()
            .eq(CategoryEnergy::getCategory, categoryType)
            .eq(CategoryEnergy::getSn, sn)
            .eq(!StringUtils.isNullOrEmpty(form.getPointCode()), CategoryEnergy::getCode, form.getPointCode());
        // todo getDateIndex 真实是什么样的 到底是时间戳还是ymd
        if (form.getStartDate() != null) {
            queryWrapper.and(wrapper -> wrapper.ge(CategoryEnergy::getDateIndex, form.getStartDate().getDayOfMonth())
                .or()
                .gt(CategoryEnergy::getDateIndex, form.getStartDate().getDayOfMonth())
                .and(i -> i.ge(CategoryEnergy::getHourIndex, form.getStartDate().getHour())));
        }
        if (form.getEndDate() != null) {
            queryWrapper.and(wrapper -> wrapper.le(CategoryEnergy::getDateIndex, form.getEndDate().getDayOfMonth())
                .or()
                .lt(CategoryEnergy::getDateIndex, form.getEndDate().getDayOfMonth())
                .and(i -> i.le(CategoryEnergy::getHourIndex, form.getEndDate().getHour())));
        }
        return categoryEnergyMapper.selectPage(Page.of(form.getPage(), form.getSize()), queryWrapper);
    }
}
