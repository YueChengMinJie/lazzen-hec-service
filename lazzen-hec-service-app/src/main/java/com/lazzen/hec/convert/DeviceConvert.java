package com.lazzen.hec.convert;

import com.lazzen.hec.dto.DeviceCurrentData;
import com.lazzen.hec.po.DeviceOnlineStatus;
import com.lazzen.hec.po.DevicePointData;

import java.util.List;

public class DeviceConvert extends Convert{

    public static List<DeviceCurrentData> convertDpa(List<DevicePointData> po) {
        return getMapperFacade().mapAsList(po, DeviceCurrentData.class);
    }
    public static Boolean convertOnline(DeviceOnlineStatus statusBySn) {
        if (statusBySn.getLink() != null && statusBySn.getLink().getValue() != null) {
            return statusBySn.getLink().getValue() == 1;
        }
        return false;
    }
}
