package com.supcon.ses.dataupload.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supcon.ses.dataupload.exceptions.JsonSerializationException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class JsonHelper {

    private JsonHelper(){

    }

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String writeValue(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new JsonSerializationException("序列化报错",e);
        }
    }

    public static  <T>  T  parseJson(String json,Class<T> cls){
        if (StringUtils.isEmpty(json)){
            return null;
        }
        try {
            return mapper.readValue(json,cls);
        } catch (IOException e) {
            throw new JsonSerializationException("序列化报错",e);
        }
    }

}
