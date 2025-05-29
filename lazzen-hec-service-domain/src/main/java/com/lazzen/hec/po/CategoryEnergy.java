package com.lazzen.hec.po;

import java.util.Date;

import com.lazzen.hec.po.base.ActualityObject;

import lombok.Data;

/**
 * 能耗日表(小时)
 * 
 * @TableName category_energy
 */
@Data
public class CategoryEnergy implements ActualityObject {
    /**
     * 
     */
    private Long id;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 能源类型：水water 电electric 气
     */
    private String category;

    /**
     * 设备sn
     */
    private String sn;

    /**
     * 点位code
     */
    private String code;

    /**
     * 归一化点位
     */
    private String groupCode;

    /**
     * 日期yyyy-mm-dd
     */
    private Integer dateIndex;

    /**
     * 小时
     */
    private String hourIndex;

    /**
     * 原始值
     */
    private String original;

    /**
     * 修改值
     */
    private String modify;

    /**
     * 实际值
     */
    private String actuality;

    /**
     * 设备上报能耗实时值
     */
    private String relaTimeValue;

    /**
     * 单位
     */
    private String unit;

    /**
     * 设备上报时间
     */
    private Long deviceTime;

    /**
     * 设备状态 0绑定 1解绑
     */
    private String state;

    /**
     * 修改人
     */
    private String updateUser;

    /**
     * 补录类型 0网关同步 1手动补录
     */
    private String updateType;

    /**
     * 修改时间
     */
    private Date updateTime;
}
