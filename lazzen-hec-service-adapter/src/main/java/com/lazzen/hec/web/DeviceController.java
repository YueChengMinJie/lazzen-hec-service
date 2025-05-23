package com.lazzen.hec.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lazzen.hec.constants.BusinessConstants;
import com.lazzen.hec.convert.SteamDetailDataConvert;
import com.lazzen.hec.convert.WaterDetailDataConvert;
import com.lazzen.hec.dto.CategoryEnergyData;
import com.lazzen.hec.dto.ChartData;
import com.lazzen.hec.dto.CurrentDetailData;
import com.lazzen.hec.dto.DeviceCurrentData;
import com.lazzen.hec.enumeration.ChartQueryEnum;
import com.lazzen.hec.form.CurrentSteamForm;
import com.lazzen.hec.form.CurrentWaterForm;
import com.lazzen.hec.form.DataQueryForm;
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
    @Operation(summary = "获取指定domain, deviceType实时数据")
    public ResponseWrapper<List<DeviceCurrentData>> immediatelyData(
        @Valid @NotNull(message = "缺少domainCode") String domainCode,
        @Valid @NotNull(message = "缺少deviceType") String deviceType) {
        return ResponseWrapper.successOf(deviceService.getImmediatelyBySn(domainCode, deviceType));
    }

    @PostMapping("/current/water")
    @Operation(summary = "水系统中控屏")
    public ResponseWrapper<List<CurrentDetailData>> currentWater(@Valid @RequestBody CurrentWaterForm form) {
        return ResponseWrapper
            .successOf(deviceService.currentDetailData(form, waterConvert, BusinessConstants.Water.SYB));
    }

    @PostMapping("/current/steam")
    @Operation(summary = "蒸汽记录仪")
    public ResponseWrapper<List<CurrentDetailData>> currentSteam(@Valid @RequestBody CurrentSteamForm form) {
        return ResponseWrapper
            .successOf(deviceService.currentDetailData(form, steamConvert, BusinessConstants.Steam.QYB));
    }

    @PostMapping("/history/analysis")
    @Operation(summary = "分析数据")
    public ResponseWrapper<Page<CategoryEnergyData>> historySteam(@Valid @RequestBody DataQueryForm form) {
        return ResponseWrapper
            .successOf(deviceService.historyCategoryEnergy(form, BusinessConstants.Electronic.CATEGORY));
    }

    @PostMapping("/history/analysis/export")
    @Operation(summary = "导出分析数据")
    public void historySteamExport(@Valid @RequestBody DataQueryForm form, HttpServletResponse response)
        throws IOException {
        deviceService.historyCategoryEnergyExport(response, form, BusinessConstants.Electronic.CATEGORY);
    }

    @PostMapping("/chart/water")
    @Operation(summary = "水能耗图表")
    public ResponseWrapper<List<ChartData>> chartWater(ChartQueryEnum form) {
        return ResponseWrapper.successOf(deviceService.chart(form, BusinessConstants.Water.CATEGORY));
    }

    @PostMapping("/chart/steam")
    @Operation(summary = "气能耗图表")
    public ResponseWrapper<List<ChartData>> chartSteam(ChartQueryEnum form) {
        return ResponseWrapper.successOf(deviceService.chart(form, BusinessConstants.Steam.CATEGORY));
    }
}
