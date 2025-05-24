package com.lazzen.hec.form;

import javax.validation.constraints.NotNull;

import com.lazzen.hec.enumeration.ChartQueryEnum;
import com.lazzen.hec.enumeration.DetailDataEnum;

import lombok.Data;

@Data
public class ChartForm {
    @NotNull
    private ChartQueryEnum queryEnum;

    @NotNull
    private String domainCode;

    @NotNull
    private DetailDataEnum dataEnum;
}
