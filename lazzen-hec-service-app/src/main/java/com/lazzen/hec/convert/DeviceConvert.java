package com.lazzen.hec.convert;

import java.util.List;

import com.lazzen.hec.dto.DeviceCurrentData;
import com.lazzen.hec.po.DeviceOnlineStatus;
import com.lazzen.hec.po.DevicePointData;

import cn.hutool.extra.spring.SpringUtil;
import ma.glasnost.orika.MapperFacade;

/**
 * @author guo
 * @createDate 2025-05-18 21:59:34
 */
public class DeviceConvert {
    public static List<DeviceCurrentData> convertDpa(List<DevicePointData> po) {
        return getMapperFacade().mapAsList(po, DeviceCurrentData.class);
    }

    private static MapperFacade getMapperFacade() {
        return SpringUtil.getBean(MapperFacade.class);
    }

    public static Boolean convertOnline(DeviceOnlineStatus statusBySn) {
        if (statusBySn.getLink() != null && statusBySn.getLink().getValue() != null) {
            return statusBySn.getLink().getValue() == 1;
        }
        return false;
    }
}
