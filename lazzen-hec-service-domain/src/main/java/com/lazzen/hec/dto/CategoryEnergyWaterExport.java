package com.lazzen.hec.dto;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
public class CategoryEnergyWaterExport {
    @ExcelProperty("日期")
    @ColumnWidth(20)
    private String date;

    @ExcelProperty("用水量")
    @ColumnWidth(20)
    private String value;

    @ExcelProperty("差值")
    @ColumnWidth(20)
    private String subValue;
}
