package com.lazzen.hec.web;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.lazzen.hec.dto.GwmpcwgData;
import com.sipa.boot.java8.common.dtos.ResponseWrapper;

import io.swagger.v3.oas.annotations.Operation;

/**
 * @author caszhou
 * @date 2025/5/15
 */
@RestController
@RequestMapping("/gwmpcwg")
public class GwmpcwgController {
    @GetMapping
    @Operation(summary = "获取实时数据")
    public ResponseWrapper<List<GwmpcwgData>> data(String domainCode) {
        return ResponseWrapper.successOf(
            Lists.newArrayList(GwmpcwgData.builder().id(1).label("温度").val(new BigDecimal("1.1")).unit("°C").build(),
                GwmpcwgData.builder().id(2).label("温度").val(new BigDecimal("1.2")).unit("°C").build(),
                GwmpcwgData.builder().id(3).label("温度").val(new BigDecimal("1.3")).unit("°C").build(),
                GwmpcwgData.builder().id(4).label("温度").val(new BigDecimal("1.4")).unit("°C").build(),
                GwmpcwgData.builder().id(5).label("温度").val(new BigDecimal("1.5")).unit("°C").build(),
                GwmpcwgData.builder().id(6).label("温度").val(new BigDecimal("1.6")).unit("°C").build()));
    }
}
