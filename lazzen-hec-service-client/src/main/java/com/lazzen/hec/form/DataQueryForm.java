package com.lazzen.hec.form;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.lazzen.hec.enumeration.DetailDataEnum;

import io.swagger.v3.oas.annotations.media.Schema;
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
    private String id;

    @NotBlank
    private String domainCode;

    @NotNull
    private DetailDataEnum dataEnum;

    @NotBlank
    @Schema(description = "正向总量/累积值的点位code 分析数据需要  这个参数必须,作为分页的基点")
    private String forwardPointCode;

    @Schema(description = "瞬时流量/实时值的点位code 分析数据需要")
    private String momentPointCode;

    @Schema(description = "反向总量的点位code 分析数据需要")
    private String reversePointCode;
}
