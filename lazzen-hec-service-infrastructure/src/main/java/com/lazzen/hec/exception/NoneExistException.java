package com.lazzen.hec.exception;

public class NoneExistException extends Exception{
    public NoneExistException(String msg) {
        super(msg);
    }
    public static void assertExist(Object object,String param)throws NoneExistException{
        if(object==null){
            throw new NoneExistException(param+"不存在对应的数据");
        }
        if(object instanceof String){
            if(object.toString().trim().isEmpty()){
                throw new NoneExistException(param+"不存在对应的数据");
            }
        }
    }
}
