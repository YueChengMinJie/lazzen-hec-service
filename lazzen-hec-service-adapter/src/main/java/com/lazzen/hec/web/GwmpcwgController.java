package com.lazzen.hec.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lazzen.hec.dto.GwmpcwgData;
import com.sipa.boot.java8.common.dtos.ResponseWrapper;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

/**
 * @author caszhou
 * @date 2025/5/15
 */
@Slf4j
@RestController
@RequestMapping("/gwmpcwg")
public class GwmpcwgController {
    @GetMapping
    @Operation(summary = "获取 高压柜母排测温 实时数据")
    public ResponseWrapper<GwmpcwgData> data(String sn) {
        log.info(sn);
        return ResponseWrapper.successOf(GwmpcwgData.builder().build());
    }
}
