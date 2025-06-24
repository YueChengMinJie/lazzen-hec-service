package com.lazzen.hec.dto;

import lombok.Data;

/**
 * @author caszhou
 * @date 2025/6/24
 */
@Data
public class SqYbAliasDto {
    /**
     * 1:水 2:汽
     */
    private int type;

    /**
     * 水和汽的下标，从1开始
     */
    private int idx;

    /**
     * 别名
     */
    private String name;
}
