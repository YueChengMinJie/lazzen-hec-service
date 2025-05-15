package com.lazzen.hec.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sipa.boot.java8.common.dtos.ResponseWrapper;

import io.swagger.v3.oas.annotations.Operation;

/**
 * @author caszhou
 * @date 2025/5/15
 */
@RestController
@RequestMapping("/test")
public class TestController {
    @PostMapping("/1")
    @Operation(summary = "测试1", description = "测试1")
    public ResponseWrapper<?> test1() {
        return ResponseWrapper.success();
    }
}
