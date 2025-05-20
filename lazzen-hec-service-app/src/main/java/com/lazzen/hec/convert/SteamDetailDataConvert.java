package com.lazzen.hec.convert;

import java.util.regex.Pattern;

import com.lazzen.hec.enumeration.DetailDataEnum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SteamDetailDataConvert extends DetailDataConvert {
    private final Pattern pattern = Pattern.compile("^(" + getDetailDataEnum().getFORWARD_TOTAL() + "|"
            + getDetailDataEnum().getREVERSE_TOTAL() + "|" + getDetailDataEnum().getMOMENT() + ")(\\d+)$");

    @Override
    Pattern getPattern() {
        return pattern;
    }

    @Override
    DetailDataEnum getDetailDataEnum() {
        return DetailDataEnum.STEAM;
    }
}
