package com.lazzen.hec.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * 【lite_nader4_common】仪表别名表
 *
 * @author caszhou
 * @date 2025/6/24
 */
@Data
public class SqYbAliasForm {
    /**
     * 1:水 2:汽
     */
    @NotNull
    private Integer type;

    /**
     * 水和汽的下标，从1开始
     */
    @NotNull
    private Integer idx;

    /**
     * 别名
     */
    @NotBlank
    private String name;
}
