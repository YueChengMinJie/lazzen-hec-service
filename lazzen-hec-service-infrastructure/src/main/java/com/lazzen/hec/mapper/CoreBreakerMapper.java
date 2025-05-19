package com.lazzen.hec.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lazzen.hec.po.CoreBreaker;

/**
 * @author guo
 * @description 针对表【core_breaker(【基础服务】设备表)】的数据库操作Mapper
 * @createDate 2025-05-18 22:40:22
 * @Entity com.lazzen.hec.po.CoreBreaker
 */
@Mapper
public interface CoreBreakerMapper extends BaseMapper<CoreBreaker> {
    //
}
