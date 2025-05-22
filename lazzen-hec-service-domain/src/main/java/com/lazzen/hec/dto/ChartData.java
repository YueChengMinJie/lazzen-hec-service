package com.lazzen.hec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChartData {
    @Schema(description = "x轴")
    private String xName;

    @Schema(description = "耗电量")
    private String value;

    @Schema(description = "环比 分子")
    private String qoq;

    @Schema(description = "同比 分子")
    private String yoy;
}
