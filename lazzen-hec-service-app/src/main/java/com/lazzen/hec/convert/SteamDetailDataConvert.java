package com.lazzen.hec.convert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.lazzen.hec.enumeration.DetailDataEnum;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SteamDetailDataConvert extends DetailDataConvert {
    private final Pattern pattern = Pattern.compile("^(" + getDetailDataEnum().getFORWARD_TOTAL() + "|"
        + getDetailDataEnum().getREVERSE_TOTAL() + "|" + getDetailDataEnum().getMOMENT() + ")(\\d+)$");

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
        // todo test
        // 从CH1 中提取出数字
        return pattern.matcher(matcher.group(1)).group();
    }
}
