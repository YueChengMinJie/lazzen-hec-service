package com.lazzen.hec.convert;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.lazzen.hec.dto.CurrentDetailData;
import com.lazzen.hec.enumeration.DetailDataEnum;
import com.lazzen.hec.po.DevicePointData;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author guo
 * @createDate 2025-05-18 21:59:34
 */
@Slf4j
public abstract class DetailDataConvert extends Convert{

    abstract Pattern getPattern();

    abstract DetailDataEnum getDetailDataEnum();

    public List<CurrentDetailData> convertDetailData(List<DevicePointData> list) {
        // 每三个数据合成一个数据
        Map<Integer, List<DevicePointData>> groupMap = new HashMap<>();
        for (DevicePointData obj : list) {
            Matcher matcher = getPattern().matcher(obj.getName());
            if (matcher.matches()) {
                int number = Integer.parseInt(matcher.group(2)); // 提取数字
                groupMap.computeIfAbsent(number, k -> new ArrayList<>()).add(obj);
            }
        }
        List<CurrentDetailData> result = new ArrayList<>();
        for (Integer key : groupMap.keySet()) {
            CurrentDetailData data = new CurrentDetailData();
            data.setName(getDetailDataEnum().getNAME_PREFIX() + key);
            setValue(data, groupMap.get(key));
            // todo 设置仪表状态
            // data.setLink();
            result.add(data);
        }
        return result;
    }

    private void setValue(CurrentDetailData data, List<DevicePointData> threeInOne) {
        // 瞬时流量
        threeInOne.stream()
                .filter(e -> e != null && e.getName().contains(getDetailDataEnum().getMOMENT()))
                .findFirst()
                .ifPresent(e -> data.setValue(e.getValue()));
        // 总流量
        Optional<DevicePointData> forward = threeInOne.stream()
                .filter(e -> e != null && e.getName().contains(getDetailDataEnum().getFORWARD_TOTAL()))
                .findFirst();
        Optional<DevicePointData> reverse = threeInOne.stream()
                .filter(e -> e != null && e.getName().contains(getDetailDataEnum().getREVERSE_TOTAL()))
                .findFirst();
        if (forward.isPresent()) {
            if (reverse.isPresent()) {
                try {
                    BigDecimal num1 = NumberUtil.toBigDecimal(forward.get().getValue());
                    BigDecimal num2 = NumberUtil.toBigDecimal(reverse.get().getValue()).abs();
                    data.setTotalValue(NumberUtil.sub(num1, num2).toString());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    log.error("forward:{}", forward.get().getValue());
                    log.error("reverse:{}", reverse.get().getValue());
                }
            } else {
                data.setTotalValue(forward.get().getValue());
            }
        }
    }
}
