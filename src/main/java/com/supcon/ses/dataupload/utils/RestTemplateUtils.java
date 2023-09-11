package com.supcon.ses.dataupload.utils;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class RestTemplateUtils {

    private final RestTemplate restTemplate;

    public RestTemplateUtils(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     *
     * @param baseUrl 平台ip和port
     * @param url 接口地址
     * @param request 请求参数
     * @param responseType 返回数据类型
     * @param uriVariables url参数
     * @return
     * @param <T>
     */
    public <T> ResponseEntity<T> post(String baseUrl, String url, Object request, Class<T> responseType, Map<String, Object> uriVariables) {
        HttpHeaders headers = new HttpHeaders();
        return post(baseUrl,url,request,responseType,uriVariables,headers);
    }

    /**
     *
     * @param baseUrl       平台ip和port
     * @param url           接口地址
     * @param request       请求参数
     * @param responseType  返回数据类型
     * @param uriVariables  url参数
     * @param headers       header参数
     * @return
     * @param <T>
     */
    public <T> ResponseEntity<T> post(String baseUrl, String url, Object request, Class<T> responseType, Map<String, Object> uriVariables,HttpHeaders headers) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(request, headers);
        String uri = baseUrl + url;
        return restTemplate.exchange(uri, HttpMethod.POST, entity, responseType, uriVariables);
    }


}