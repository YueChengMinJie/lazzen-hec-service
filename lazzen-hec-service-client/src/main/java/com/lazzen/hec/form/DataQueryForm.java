package com.lazzen.hec.form;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DataQueryForm extends PageForm {
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Schema(description = "点位code,可不传")
    private String pointCode;

    @NotBlank
    private String domainCode;
}
