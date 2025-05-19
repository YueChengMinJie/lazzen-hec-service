package com.lazzen.hec.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lazzen.hec.po.CategoryEnergyMonth;

/**
 * @author guo
 * @description 针对表【category_energy_month】的数据库操作Mapper
 * @createDate 2025-05-18 22:04:33
 * @Entity com.lazzen.hec.po.CategoryEnergyMonth
 */
@Mapper
public interface CategoryEnergyMonthMapper extends BaseMapper<CategoryEnergyMonth> {

}
