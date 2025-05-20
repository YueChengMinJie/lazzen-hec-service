package com.lazzen.hec.web;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.lazzen.hec.constants.BusinessConstants;
import com.lazzen.hec.convert.SteamDetailDataConvert;
import com.lazzen.hec.convert.WaterDetailDataConvert;
import com.lazzen.hec.form.CurrentSteamForm;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lazzen.hec.dto.CurrentDetailData;
import com.lazzen.hec.dto.DeviceCurrentData;
import com.lazzen.hec.form.CurrentWaterForm;
import com.lazzen.hec.service.DeviceService;
import com.sipa.boot.java8.common.dtos.ResponseWrapper;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author caszhou
 * @date 2025/5/15
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/device")
public class DeviceController {
    private final DeviceService deviceService;
    private final WaterDetailDataConvert waterConvert;
    private final SteamDetailDataConvert steamConvert;

    @GetMapping("/status")
    @Operation(summary = "获取设备状态")
    public ResponseWrapper<Boolean> data(String domainCode) {
        return ResponseWrapper.successOf(deviceService.getStatusByDomainCode(domainCode));
    }

    @GetMapping("/current/data")
    @Operation(summary = "获取指定domain,deviceType(可空)实时数据")
    public ResponseWrapper<List<DeviceCurrentData>>
        immediatelyData(@Valid @NotNull(message = "缺少domainCode") String domainCode, String deviceType) {
        return ResponseWrapper.successOf(deviceService.getImmediatelyBySn(domainCode, deviceType));
    }

    @GetMapping("/current/water")
    @Operation(summary = "水系统中控屏")
    public ResponseWrapper<List<CurrentDetailData>> currentWater(@RequestBody CurrentWaterForm form) {
        return ResponseWrapper.successOf(deviceService.currentDetailData(form,waterConvert, BusinessConstants.Water.SYB));
    }
    @GetMapping("/current/steam")
    @Operation(summary = "蒸汽记录仪")
    public ResponseWrapper<List<CurrentDetailData>> currentSteam(@RequestBody CurrentSteamForm form) {
        return ResponseWrapper.successOf(deviceService.currentDetailData(form,steamConvert, BusinessConstants.Steam.QYB));
    }
}
