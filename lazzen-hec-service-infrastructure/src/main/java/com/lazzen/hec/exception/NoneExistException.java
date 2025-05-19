package com.lazzen.hec.exception;

import java.util.List;

/**
 * @author guo
 * @createDate 2025-05-18 21:59:34
 */
public class NoneExistException extends RuntimeException {
    private static final String MSG = "不存在对应的数据";

    public NoneExistException(String msg) {
        super(msg);
    }

    public static void assertExist(Object object, String param) throws NoneExistException {
        if (object == null) {
            throw new NoneExistException(param + MSG);
        }
        if (object instanceof String) {
            if (object.toString().trim().isEmpty()) {
                throw new NoneExistException(param + MSG);
            }
        }
        if (object instanceof List) {
            if (((List<?>)object).isEmpty()) {
                throw new NoneExistException(param + MSG);
            }
        }
    }
}
