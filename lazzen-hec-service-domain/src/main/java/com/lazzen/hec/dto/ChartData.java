package com.lazzen.hec.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartData {
    @Schema(description = "x轴")
    private String xName;

    @Schema(description = "耗电量")
    private BigDecimal value;

    @Schema(description = "环比 分子")
    private BigDecimal qoq;

    @Schema(description = "同比 分子")
    private BigDecimal yoy;
}
