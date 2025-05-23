package com.lazzen.hec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author caszhou
 * @date 2025/5/15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentDetailData {
    private String id;

    @Schema(description = "水仪表名字 ->水仪表1  蒸汽仪表名字->气仪表1")
    private String name;

    @Schema(description = "仪表的在线状态")
    private boolean link;

    @Schema(description = "仪表的瞬时值")
    private String value;

    @Schema(description = "仪表的总流量")
    private String totalValue;

    // 下面这三个code 分析哪类数据就传哪个?

    @Schema(description = "正向总量/累积值的点位code 分析数据需要")
    private String forwardPointCode;

    @Schema(description = "瞬时流量/实时值的点位code 分析数据需要")
    private String momentPointCode;

    @Schema(description = "反向总量的点位code 分析数据需要")
    private String reversePointCode;
}
