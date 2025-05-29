package com.lazzen.hec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartTopData {
    @Schema(description = "设备名称")
    private String name;

    @Schema(description = "能耗")
    private String value;
}
