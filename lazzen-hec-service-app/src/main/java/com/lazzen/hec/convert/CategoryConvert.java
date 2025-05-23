package com.lazzen.hec.convert;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    public static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static CategoryEnergyData convert(CategoryEnergy energy) {
        if (energy == null) {
            return null;
        }
        // 分析数据转换
        CategoryEnergyData data = new CategoryEnergyData();
        data.setId(energy.getId());
        try {
            LocalDate date = LocalDate.parse(String.valueOf(energy.getDateIndex()), dateFormatter);

            String formattedDateTime = String.format("%d-%02d-%02d %02d:00:00", date.getYear(), date.getMonthValue(),
                date.getDayOfMonth(), Integer.parseInt(energy.getHourIndex().trim()));
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
