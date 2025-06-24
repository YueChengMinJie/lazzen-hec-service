package com.lazzen.hec.form;

import javax.validation.constraints.NotBlank;

import lombok.Data;

/**
 * @author caszhou
 * @date 2025/6/24
 */
@Data
public class ParamExportForm {
    @NotBlank
    private String domainCode;

    @NotBlank
    private String param;
}
