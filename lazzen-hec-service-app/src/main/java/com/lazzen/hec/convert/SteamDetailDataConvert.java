package com.lazzen.hec.convert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.lazzen.hec.enumeration.DetailDataEnum;
import com.sipa.boot.java8.common.constants.SipaBootCommonConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SteamDetailDataConvert extends DetailDataConvert {
    private final String VALUE_TYPES = getDetailDataEnum().getForwardTotal() + "|"
        + getDetailDataEnum().getReverseTotal() + "|" + getDetailDataEnum().getMoment();

    private final Pattern pattern = Pattern.compile("^(.+?)\\s+(" + VALUE_TYPES + ")+$");

    private final Pattern numberPattern = Pattern.compile("-?\\d+");

    @Override
    Pattern getPattern() {
        return pattern;
    }

    @Override
    DetailDataEnum getDetailDataEnum() {
        return DetailDataEnum.STEAM;
    }

    @Override
    String getKeyFromGroup(Matcher matcher) {
        // 从CH1 中提取出数字
        Matcher numMatcher = numberPattern.matcher(matcher.group(SipaBootCommonConstants.Number.INT_1));
        if (numMatcher.find()) {
            return numMatcher.group();
        }
        return null;
    }
}
