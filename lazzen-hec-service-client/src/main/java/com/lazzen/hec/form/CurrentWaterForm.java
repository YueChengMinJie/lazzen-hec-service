package com.lazzen.hec.form;

import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CurrentWaterForm implements DetailForm {
    @Schema(description = "水仪表设备状态")
    private Boolean link;

    @Schema(description = "水仪表设备名称")
    private String waterDeviceName;

    @Schema(description = "水系统中控屏domainCode")
    @NotBlank
    private String domainCode;

    @Override
    public String getDeviceName() {
        return waterDeviceName;
    }
}
