package com.lazzen.hec.po;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

/**
 * @author caszhou
 * @date 2025/6/24
 */
@Data
public class IotPointConf {
    @TableId
    private Long pointId;

    private Long parentPointId;

    private String protocolCode;

    private String pointCode;

    private String pointName;

    private Integer symbol;

    private String parseRule;

    private Long dictId;

    private Integer recordIndex;

    private Integer pointIndex;

    private Integer pointLength;

    private Integer switchFlag;

    private Integer energyFlag;

    private Integer curveFlag;

    private String businessRemark;

    private Long groupId;

    private Integer addressSize;

    private Double calcRatio;

    private String pointUnit;

    private String pointType;

    private String eventType;

    private Integer accessPermission;

    private String businessGroup;

    private String businessCategory;

    private String pointOwner;

    private Integer businessSort;

    private Integer status;

    private String createBy;

    private String updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
