package com.lazzen.hec.web;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lazzen.hec.dto.GwmpcwgData;
import com.lazzen.hec.service.CategoryEnergyService;
import com.sipa.boot.java8.common.dtos.ResponseWrapper;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author caszhou
 * @date 2025/5/15
 */
@RestController
@RequestMapping("/gwmpcwg")
@RequiredArgsConstructor
@Slf4j
public class GwmpcwgController {
    private final CategoryEnergyService categoryEnergyService;

    @GetMapping
    @Operation(summary = "获取指定domain,deviceType(可空)实时数据")
    public ResponseWrapper<List<GwmpcwgData>>
        immediatelyData(@Valid @NotNull(message = "缺少domainCode") String domainCode, String deviceType) {
        try {
            return ResponseWrapper.successOf(categoryEnergyService.getImmediatelyBySn(domainCode, deviceType));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseWrapper.errorOf(e.getMessage());
        }
    }
}
