package com.lazzen.hec.constants;

/**
 * 先叫这个名字,多了在分
 */
public interface BusinessConstants {
    String EXCEL_EXPORT_CONTENT_TYPE = "application/vnd.ms-excel";

    String CONTENT_DISPOSITION = "Content-Disposition";

    String MONTH = "月";

    /**
     * 水中控
     */
    interface Water {
        String CATEGORY = "water";

        String SYB = "SYB";

        String NAME_PREFIX = "水仪表";

        String FORWARD_TOTAL = "正向总量";

        String REVERSE_TOTAL = "反向总量";

        String MOMENT = "瞬时流量";
    }

    /**
     * 蒸汽记录仪
     */
    interface Steam {
        String CATEGORY = "steam";

        String QYB = "QYB";

        String NAME_PREFIX = "气仪表";

        String FORWARD_TOTAL = "累积值";

        String REVERSE_TOTAL = "没有反向";

        String MOMENT = "实时值";
    }

    interface Electronic {
        String CATEGORY = "electric";
    }

    interface DateFormat {
        String YEAR = "yyyy";

        String YEAR_MONTH = "yyyyMM";

        String HOUR = "HH";

        String DT = "yyyy-MM-dd HH:mm:ss";

        String EXPORT = "yyyy/MM/dd HH:mm:ss";
    }
}
