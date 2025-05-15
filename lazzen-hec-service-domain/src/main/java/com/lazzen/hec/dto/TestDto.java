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
public class TestDto {
    @Schema(description = "用户id")
    private Integer id;
}
