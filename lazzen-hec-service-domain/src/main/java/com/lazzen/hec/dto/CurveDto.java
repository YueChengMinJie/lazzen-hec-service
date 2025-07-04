package com.lazzen.hec.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author caszhou
 * @date 2025/6/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurveDto {
    private List<String> legendData;

    private List<String> xAxisData;

    private List<List<String>> seriesData;
}
