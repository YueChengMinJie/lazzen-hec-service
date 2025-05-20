package com.lazzen.hec.convert;

import cn.hutool.extra.spring.SpringUtil;
import ma.glasnost.orika.MapperFacade;

public class Convert {
    protected static MapperFacade getMapperFacade() {
        return SpringUtil.getBean(MapperFacade.class);
    }
}
