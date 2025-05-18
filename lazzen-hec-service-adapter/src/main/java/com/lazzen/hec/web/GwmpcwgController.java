package com.lazzen.hec.web;

import java.util.List;

import com.lazzen.hec.service.CategoryEnergyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lazzen.hec.dto.GwmpcwgData;
import com.sipa.boot.java8.common.dtos.ResponseWrapper;

import io.swagger.v3.oas.annotations.Operation;


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
    @Operation(summary = "获取实时数据")
    public ResponseWrapper<List<GwmpcwgData>> data(String domainCode) {
        try {
            return ResponseWrapper.successOf(categoryEnergyService.getImmediatelyBySn(domainCode));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseWrapper.errorOf(e.getMessage());
        }
    }
}
