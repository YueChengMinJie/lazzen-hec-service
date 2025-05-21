package com.lazzen.hec.convert;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import com.lazzen.hec.dto.CategoryEnergyData;
import com.lazzen.hec.po.CategoryEnergy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CategoryConvert extends Convert {
    public static CategoryEnergyData convert(CategoryEnergy energy) {
        if (energy == null) {
            return null;
        }
        CategoryEnergyData data = new CategoryEnergyData();
        try {
            // todo gzp 确认如何转换时间
            String formattedDateTime = String.format("%d-%02d-%02d %02d:00:00", LocalDateTime.now().getYear(),
                LocalDateTime.now().getMonthValue(), energy.getDateIndex(),
                Integer.parseInt(energy.getHourIndex().trim()));
            data.setDate(formattedDateTime);
        } catch (Exception e) {
            data.setDate(energy.getDateIndex() + "-" + energy.getHourIndex());
            log.error(e.getMessage(), e);
        }

        data.setValue(energy.getRelaTimeValue());
        return data;
    }

    public static List<CategoryEnergyData> convert(List<CategoryEnergy> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(CategoryConvert::convert).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
