package com.lazzen.hec.dto;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * @author caszhou
 * @date 2025/6/24
 */
@Data
public class WaveformExport {
    @ExcelProperty("点位")
    @ColumnWidth(20)
    private String name;

    @ExcelProperty("值")
    @ColumnWidth(20)
    private String value;
}
