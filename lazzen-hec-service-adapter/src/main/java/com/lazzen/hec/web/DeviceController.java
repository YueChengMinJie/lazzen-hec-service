package com.lazzen.hec.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sipa.boot.java8.common.dtos.ResponseWrapper;

import io.swagger.v3.oas.annotations.Operation;

/**
 * @author caszhou
 * @date 2025/5/15
 */
@RestController
@RequestMapping("/device")
public class DeviceController {
    @GetMapping("/status")
    @Operation(summary = "获取设备状态")
    public ResponseWrapper<Boolean> data(String domainCode) {
        return ResponseWrapper.successOf(true);
    }
}
