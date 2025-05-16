package com.lazzen.hec.dto;

import java.math.BigDecimal;

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
public class GwmpcwgData {
    private BigDecimal val1;

    private String unit1;

    private BigDecimal val2;

    private String unit2;

    private BigDecimal val3;

    private String unit3;

    private BigDecimal val4;

    private String unit4;

    private BigDecimal val5;

    private String unit5;

    private BigDecimal val6;

    private String unit6;
}
