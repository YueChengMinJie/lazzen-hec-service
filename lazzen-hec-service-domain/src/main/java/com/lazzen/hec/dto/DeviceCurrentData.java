package com.lazzen.hec.dto;

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

    private String name;

    private String value;

    private String unit;

    private String code;
}
