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
public class DeviceCurrentData {
    private Long id;

    private String label;

    private BigDecimal val;

    private String unit;
}
