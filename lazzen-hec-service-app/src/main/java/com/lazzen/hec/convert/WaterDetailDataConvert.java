package com.lazzen.hec.convert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.lazzen.hec.enumeration.DetailDataEnum;
import com.sipa.boot.java8.common.constants.SipaBootCommonConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WaterDetailDataConvert extends DetailDataConvert {
    @Value("${lazzen.hec.water-limit-time-seconds:300}")
    private long limitTime;

    private final String VALUE_TYPES = getDetailDataEnum().getForwardTotal() + "|"
        + getDetailDataEnum().getReverseTotal() + "|" + getDetailDataEnum().getMoment();

    private final Pattern pattern = Pattern.compile("^(" + VALUE_TYPES + ")(\\d+)$");

    @Override
    Pattern getPattern() {
        return pattern;
    }

    @Override
    DetailDataEnum getDetailDataEnum() {
        return DetailDataEnum.WATER;
    }

    @Override
    String getKeyFromGroup(Matcher matcher) {
        // 提取 <反向总量1>的数字
        return matcher.group(SipaBootCommonConstants.Number.INT_2);
    }

    @Override
    protected long getLimitTime() {
        return limitTime * 1000;
    }
}
