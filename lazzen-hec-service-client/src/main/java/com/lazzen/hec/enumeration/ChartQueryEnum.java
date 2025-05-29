package com.lazzen.hec.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChartQueryEnum implements IEnum<Integer> {
    DAY(1), WEEK(2), MONTH(3), QUARTER(4), YEAR(5);

    private final Integer value;
}
