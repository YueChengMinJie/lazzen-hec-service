package com.lazzen.hec.convert;

import cn.hutool.extra.spring.SpringUtil;
import ma.glasnost.orika.MapperFacade;

/**
 * @author guo
 * @createDate 2025-05-18 21:59:34
 */
public class Convert {
    protected static MapperFacade getMapperFacade() {
        return SpringUtil.getBean(MapperFacade.class);
    }
}
