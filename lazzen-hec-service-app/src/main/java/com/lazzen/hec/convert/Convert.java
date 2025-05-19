package com.lazzen.hec.convert;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.hutool.core.util.NumberUtil;
import com.lazzen.hec.constants.BusinessConstants;
import com.lazzen.hec.dto.CurrentWaterData;
import com.lazzen.hec.dto.DeviceCurrentData;
import com.lazzen.hec.po.DeviceOnlineStatus;
import com.lazzen.hec.po.DevicePointData;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;

/**
 * @author guo
 * @createDate 2025-05-18 21:59:34
 */
@Slf4j
public class Convert {


    private static MapperFacade getMapperFacade() {
        return SpringUtil.getBean(MapperFacade.class);
    }

    public static class DeviceCurrentDataConvert {
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

    public static class WaterConvert {
        private final static Pattern pattern = Pattern.compile(
                "^("+BusinessConstants.Water.SYB_FORWARD_TOTAL
                +"|"+BusinessConstants.Water.SYB_REVERSE_TOTAL
                +"|"+BusinessConstants.Water.SYB_MOMENT+")(\\d+)$");
        public static List<CurrentWaterData> convertSyb(List<DevicePointData> list) {
            //每三个数据合成一个数据
            Map<Integer, List<DevicePointData>> groupMap = new HashMap<>();
            for (DevicePointData obj : list) {
                Matcher matcher = pattern.matcher(obj.getName());
                if (matcher.matches()) {
                    int number = Integer.parseInt(matcher.group(2));  // 提取数字
                    groupMap.computeIfAbsent(number, k -> new ArrayList<>()).add(obj);
                }
            }
            List<CurrentWaterData> result = new ArrayList<>();
            for (Integer key : groupMap.keySet()) {
                CurrentWaterData data = new CurrentWaterData();
                data.setName(BusinessConstants.Water.SYB_NAME+key);
                setValue(data,groupMap.get(key));
                //todo 设置水仪表状态
                //data.setLink();
                result.add(data);
            }
            return result;
        }
        private static void  setValue(CurrentWaterData data,List<DevicePointData> threeInOne){
            //瞬时流量
            threeInOne.stream()
                    .filter(e->e!=null&&e.getName().contains(BusinessConstants.Water.SYB_MOMENT))
                    .findFirst()
                    .ifPresent(e->data.setValue(e.getValue()));
            //总流量
            Optional<DevicePointData> forward = threeInOne.stream()
                    .filter(e -> e != null && e.getName().contains(BusinessConstants.Water.SYB_FORWARD_TOTAL))
                    .findFirst();
            Optional<DevicePointData> reverse = threeInOne.stream()
                    .filter(e -> e != null && e.getName().contains(BusinessConstants.Water.SYB_REVERSE_TOTAL))
                    .findFirst();
            if(forward.isPresent()){
                if(reverse.isPresent()){
                    try {
                        BigDecimal num1 = NumberUtil.toBigDecimal(forward.get().getValue());
                        BigDecimal num2 = NumberUtil.toBigDecimal(reverse.get().getValue()).abs();
                        data.setTotalValue(NumberUtil.sub(num1,num2).toString());
                    }catch (Exception e) {
                        log.error(e.getMessage(),e);
                        log.error("forward:{}", forward.get().getValue());
                        log.error("reverse:{}", reverse.get().getValue());
                    }
                }else {
                    data.setTotalValue(forward.get().getValue());
                }
            }
        }

    }



}
