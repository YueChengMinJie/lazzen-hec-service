package com.lazzen.hec.po;

import java.util.Date;

import lombok.Data;

/**
 * 【基础服务】设备表
 * 
 * @TableName core_breaker
 */
@Data
public class CoreBreaker {
    /**
     * 设备ID
     */
    private Long breakerId;

    /**
     * 设备编码(设备序列号)
     */
    private String breakerCode;

    /**
     * 设备名称
     */
    private String breakerName;

    /**
     * 客户ID
     */
    private Long customId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 区域编码
     */
    private String domainCode;

    /**
     * 多级区域编码
     */
    private String cascadeCode;

    /**
     * 设备类别
     */
    private String breakerLargeGroup;

    /**
     * 设备大类
     */
    private String breakerLargeType;

    /**
     * 设备协议【设备大类对应协议】
     */
    private String protocol;

    /**
     * 能源类型 code
     */
    private String energyType;

    /**
     * 能源ID
     */
    private Long energyId;

    /**
     * 优先级排序
     */
    private Integer sort;

    /**
     * 生产日期
     */
    private Date productDate;

    /**
     * 合闸图片
     */
    private String closePic;

    /**
     * 分闸图片
     */
    private String openPic;

    /**
     * 资产编号
     */
    private String assetCode;

    /**
     * 图片
     */
    private String pic;

    /**
     * 系统ID
     */
    private Long systemId;

    /**
     * 网关地址
     */
    private String gatewayAddress;

    /**
     * 设备地址
     */
    private String breakerAddress;

    /**
     * 台账编号
     */
    private String propertyNumber;

    /**
     * 数据来源
     */
    private String dataSources;

    /**
     * 设备维护状态(0：使用中, 1：维护中)
     */
    private Integer maintenanceStatus;

    /**
     * 是否删除
     */
    private Integer deleteFlag;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private Date updatedTime;
}
