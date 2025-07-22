package com.lazzen.hec.dto;

import java.util.ArrayList;
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
public class AnalyseDto {
    private List<String> legendData;

    private List<String> xAxisData = new ArrayList<>();

    private List<List<String>> seriesData = new ArrayList<>();

    private String value1 = "--";

    private String text1;

    private String value2 = "--";

    private String text2;

    private String value3 = "--";

    private String text3 = "--";
}
