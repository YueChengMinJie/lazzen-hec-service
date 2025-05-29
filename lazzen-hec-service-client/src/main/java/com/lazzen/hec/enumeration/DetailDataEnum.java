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

    private final String deviceType;

    private final String namePrefix;

    private final String forwardTotal;

    private final String reverseTotal;

    private final String moment;
}
