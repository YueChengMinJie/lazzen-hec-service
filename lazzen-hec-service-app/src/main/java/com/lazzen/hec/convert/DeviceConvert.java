package com.lazzen.hec.convert;

import java.util.List;

import com.lazzen.hec.dto.DeviceCurrentData;
import com.lazzen.hec.po.DeviceOnlineStatus;
import com.lazzen.hec.po.DevicePointData;

public class DeviceConvert extends Convert {
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
