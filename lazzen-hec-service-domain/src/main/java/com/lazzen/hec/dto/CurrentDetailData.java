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
    @Schema(description = "水仪表名字 ->水仪表1  蒸汽仪表名字->气仪表1")
    private String name;

    @Schema(description = "仪表的在线状态")
    private boolean link;

    @Schema(description = "仪表的瞬时值")
    private String value;

    @Schema(description = "仪表的总流量")
    private String totalValue;
}
