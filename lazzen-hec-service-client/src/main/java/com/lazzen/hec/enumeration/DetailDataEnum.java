package com.lazzen.hec.enumeration;

import com.lazzen.hec.constants.BusinessConstants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DetailDataEnum {
    WATER(BusinessConstants.Water.SYB, BusinessConstants.Water.NAME_PREFIX, BusinessConstants.Water.FORWARD_TOTAL, BusinessConstants.Water.REVERSE_TOTAL, BusinessConstants.Water.MOMENT),

    STEAM(BusinessConstants.Steam.QYB, BusinessConstants.Steam.NAME_PREFIX, BusinessConstants.Steam.FORWARD_TOTAL, BusinessConstants.Steam.REVERSE_TOTAL, BusinessConstants.Steam.MOMENT),

    ;

    private final String DEVICE_TYPE;

    private final String NAME_PREFIX;

    private final String FORWARD_TOTAL;

    private final String REVERSE_TOTAL;

    private final String MOMENT;
}
