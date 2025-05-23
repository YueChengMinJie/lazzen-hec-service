package com.lazzen.hec.form;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DataQueryForm extends PageForm {
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    /**
     * 设备下标
     */
    @NotBlank
    private String id;

    @NotBlank
    private String domainCode;
}
