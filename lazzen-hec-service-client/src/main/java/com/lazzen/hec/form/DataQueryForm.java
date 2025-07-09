package com.lazzen.hec.form;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.lazzen.hec.enumeration.DetailDataEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DataQueryForm {
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    /**
     * 设备下标
     */
    @NotNull
    private List<String> ids;

    @NotBlank
    private String domainCode;

    @NotNull
    private DetailDataEnum dataEnum;

    @NotNull
    @Schema(description = "正向总量/累积值的点位code 分析数据需要  这个参数必须,作为分页的基点")
    private List<String> forwardPointCodes;

    @Schema(description = "瞬时流量/实时值的点位code 分析数据需要")
    private List<String> momentPointCodes;

    @Schema(description = "反向总量的点位code 分析数据需要")
    private List<String> reversePointCodes;
}
