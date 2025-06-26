package com.lazzen.hec.po;

import java.util.Date;

import lombok.Data;

/**
 * 实时值
 * 
 * @TableName device_point_data
 */
@Data
public class DevicePointData {
    private Long id;

    /**
     * 业务协议版本
     */
    private String version;

    /**
     * 服务器时间戳
     */
    private Long serverTime;

    /**
     * 上报时间戳
     */
    private Long deviceTime;

    /**
     * 报文id
     */
    private String messageId;

    /**
     * 网关地址
     */
    private String gatewayAddr;

    /**
     * 设备SN
     */
    private String sn;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 原始点位
     */
    private String code;

    /**
     * 点位名称
     */
    private String name;

    /**
     * 值
     */
    private String value;

    /**
     * 原始值
     */
    private String originalValue;

    /**
     * 
     */
    private String unit;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Long customerId;

    /**
     * 归一化编码
     */
    private String unCode;

    /**
     * 数据类型
     */
    private String dataType;
}
