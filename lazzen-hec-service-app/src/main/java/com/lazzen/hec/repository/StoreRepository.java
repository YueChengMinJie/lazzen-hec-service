package com.lazzen.hec.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lazzen.hec.convert.CategoryConvert;
import com.lazzen.hec.enumeration.DetailDataEnum;
import com.lazzen.hec.form.DataQueryForm;
import com.lazzen.hec.mapper.*;
import com.lazzen.hec.po.CategoryEnergy;
import com.lazzen.hec.po.DeviceOnlineStatus;
import com.lazzen.hec.po.DevicePointData;
import com.sipa.boot.java8.common.utils.StringUtils;
import com.sipa.boot.java8.data.mysql.constants.SipaBootMysqlConstants;

import cn.hutool.core.util.NumberUtil;
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

    public List<CategoryEnergy> categoryEnergyList(Integer startDateIndex, Integer endDateIndex, String sn,
        Set<String> codeSet) {
        LambdaQueryWrapper<CategoryEnergy> queryWrapper = Wrappers.<CategoryEnergy>lambdaQuery()
            .ge(CategoryEnergy::getDateIndex, startDateIndex)
            .le(CategoryEnergy::getDateIndex, endDateIndex)
            .eq(CategoryEnergy::getSn, sn)
            .eq(CollectionUtils.isNotEmpty(codeSet), CategoryEnergy::getCode, codeSet)
            .orderByDesc(CategoryEnergy::getDateIndex)
            .orderByDesc(CategoryEnergy::getHourIndex);
        return categoryEnergyMapper.selectList(queryWrapper);
    }

    public Page<CategoryEnergy> pageCategoryTotal(DataQueryForm form, String categoryType, String sn) {
        Page<CategoryEnergy> forWard = pageCategory(form, form.getForwardPointCode(), categoryType, sn);
        if (form.getDataEnum() == DetailDataEnum.WATER && !StringUtils.isNullOrEmpty(form.getReversePointCode())
            && CollectionUtils.isNotEmpty(forWard.getRecords())) {
            // 水 减去反向流量
            Page<CategoryEnergy> reverse = pageCategory(form, form.getReversePointCode(), categoryType, sn);
            if (CollectionUtils.isNotEmpty(reverse.getRecords())) {
                Map<String, String> reverseMap = reverse.getRecords()
                    .stream()
                    .collect(
                        Collectors.toMap(e -> e.getDateIndex() + e.getHourIndex(), CategoryEnergy::getRelaTimeValue));
                for (CategoryEnergy record : forWard.getRecords()) {
                    String reverseValue = reverseMap.get(record.getDateIndex() + record.getHourIndex());
                    if (StringUtils.isNullOrEmpty(reverseValue)) {
                        continue;
                    }
                    BigDecimal num1 = NumberUtil.toBigDecimal(record.getRelaTimeValue());
                    BigDecimal num2 = NumberUtil.toBigDecimal(reverseValue).abs();
                    record.setRelaTimeValue(NumberUtil.sub(num1, num2).toString());
                }
            }
        }
        return forWard;
    }

    private Page<CategoryEnergy> pageCategory(DataQueryForm form, String pointCode, String categoryType, String sn) {
        LambdaQueryWrapper<CategoryEnergy> queryWrapper = Wrappers.<CategoryEnergy>lambdaQuery()
            .eq(CategoryEnergy::getCategory, categoryType)
            .eq(CategoryEnergy::getCode, pointCode)
            .eq(CategoryEnergy::getSn, sn)
            .orderByDesc(CategoryEnergy::getDateIndex)
            .orderByDesc(CategoryEnergy::getHourIndex);
        if (form.getStartDate() != null) {
            String formattedDate = form.getStartDate().format(CategoryConvert.dateFormatter); // "20250523"
            int dateNumber = Integer.parseInt(formattedDate);
            queryWrapper.and(wrapper -> wrapper.ge(CategoryEnergy::getDateIndex, dateNumber)
                .or()
                .gt(CategoryEnergy::getDateIndex, dateNumber)
                .and(i -> i.ge(CategoryEnergy::getHourIndex, form.getStartDate().getHour())));
        }
        if (form.getEndDate() != null) {
            String formattedDate = form.getEndDate().format(CategoryConvert.dateFormatter); // "20250523"
            int dateNumber = Integer.parseInt(formattedDate);
            queryWrapper.and(wrapper -> wrapper.le(CategoryEnergy::getDateIndex, dateNumber)
                .or()
                .lt(CategoryEnergy::getDateIndex, dateNumber)
                .and(i -> i.le(CategoryEnergy::getHourIndex, form.getEndDate().getHour())));
        }
        return categoryEnergyMapper.selectPage(Page.of(form.getPage(), form.getSize()), queryWrapper);
    }
}
