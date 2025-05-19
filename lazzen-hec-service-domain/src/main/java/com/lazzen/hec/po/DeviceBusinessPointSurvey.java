package com.lazzen.hec.po;

import java.util.Date;

import lombok.Data;

/**
 * 测量数据(曲线) 15分钟存一次 (只存7天的)
 * 
 * @TableName device_business_point_survey
 */
@Data
public class DeviceBusinessPointSurvey {
    /**
     * 
     */
    private Long id;

    /**
     * 分割时间
     */
    private Long intervalStart;

    /**
     * 上报时间戳
     */
    private Long deviceTime;

    /**
     * 设备SN
     */
    private String sn;

    /**
     * 
     */
    private String unCode;

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
     * 单位
     */
    private String unit;

    /**
     * 
     */
    private Date createTime;
}
