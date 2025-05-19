package com.lazzen.hec.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lazzen.hec.po.DevicePointData;

/**
 * @author guo
 * @description 针对表【device_point_data】的数据库操作Mapper
 * @createDate 2025-05-18 23:16:17
 * @Entity com.lazzen.hec.po.DevicePointData
 */
@Mapper
public interface DevicePointDataMapper extends BaseMapper<DevicePointData> {
    //
}
