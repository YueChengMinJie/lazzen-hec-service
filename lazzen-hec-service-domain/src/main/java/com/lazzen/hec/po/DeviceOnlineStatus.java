package com.lazzen.hec.po;

import java.util.Date;

import lombok.Data;

/**
 * 设备在线状态
 * 
 * @TableName device_online_status
 */
@Data
public class DeviceOnlineStatus {
    /**
     * 主键
     */
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 业务协议版本
     */
    private String version;

    /**
     * 上报时间戳
     */
    private String deviceTime;

    /**
     * 报文id
     */
    private String messageId;

    /**
     * —网关地址
     */
    private String gatewayAddr;

    /**
     * 设备SN
     */
    private String sn;

    /**
     * 网关地址
     */
    private String gatewayType;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 1在线 0离线
     */
    private Integer link;

    /**
     * 创建时间
     */
    private Date createTime;
}
