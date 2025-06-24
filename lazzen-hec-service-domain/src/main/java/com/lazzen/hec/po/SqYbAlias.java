package com.lazzen.hec.po;

import lombok.Data;

/**
 * 【lite_nader4_common】仪表别名表
 *
 * @author caszhou
 * @date 2025/6/24
 */
@Data
public class SqYbAlias {
    private Long id;

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
