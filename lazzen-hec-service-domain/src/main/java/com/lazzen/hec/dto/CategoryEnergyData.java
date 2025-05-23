package com.lazzen.hec.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryEnergyData {
    private Long id;

    private String date;

    private String value;
}
