package com.lazzen.hec.form;

import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CurrentSteamForm implements DetailForm {
    @Schema(description = "设备状态")
    private Boolean link;

    @Schema(description = "设备名称")
    private String steamDeviceName;

    @Schema(description = "蒸汽记录仪domainCode")
    @NotBlank
    private String domainCode;

    @Override
    public String getDeviceName() {
        return steamDeviceName;
    }
}
