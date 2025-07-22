package com.lazzen.hec.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.lazzen.hec.enumeration.ChartQueryEnum;
import com.lazzen.hec.enumeration.DetailDataEnum;

import lombok.Data;

/**
 * @author caszhou
 * @date 2025/6/24
 */
@Data
public class AnalyseForm {
    @NotNull
    private ChartQueryEnum dateType;

    @NotBlank
    private String domainCode;

    @NotNull
    private DetailDataEnum dataType;

    private String forwardPointCode;

    private String reversePointCode;
}
