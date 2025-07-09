package com.lazzen.hec.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyData {
    private String id;

    private String name;

    private String start;

    private String end;

    private String gap;
}
