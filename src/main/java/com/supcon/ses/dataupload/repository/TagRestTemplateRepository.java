package com.supcon.ses.dataupload.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supcon.ses.dataupload.model.vo.ResultVO;
import com.supcon.ses.dataupload.model.vo.TagVo;
import com.supcon.ses.dataupload.utils.RestTemplateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class TagRestTemplateRepository {

    private static final String BASE_URL = "http://%s:%s";

    private static final String READ_TAG_VALUE_URL = "/msService/public/TagManagement/readTagsSync";

    private final ObjectMapper objectMapper;

    private final RestTemplateUtils restTemplateUtils;

    public TagRestTemplateRepository(ObjectMapper objectMapper, RestTemplateUtils restTemplateUtils) {
        this.objectMapper = objectMapper;
        this.restTemplateUtils = restTemplateUtils;
    }

    public List<TagVo> findAll(String ip, String port, List<String> tagNames) throws JsonProcessingException {
        String baseUrlF = String.format(BASE_URL, ip, port);
        Map<String, Object> uriVariables = new HashMap<>();
        ResponseEntity<String> response = null;
        log.error("调用测试接口参数");
        String s = objectMapper.writeValueAsString(tagNames);
        log.error(s);

        Map<String, Object> request = new HashMap<>();
        request.put("tagNames", tagNames);

        try {
            response = restTemplateUtils.post(baseUrlF, READ_TAG_VALUE_URL, request, String.class, uriVariables);
            log.error("调用测点接口返回");
            String body = response.getBody();
            log.error(body);
        } catch (Exception ex) {
            log.error("调用测试接口报错", ex);
            return Collections.emptyList();
        }

        ResultVO resultVO = objectMapper.readValue(response.getBody(), ResultVO.class);
        return objectMapper.readValue(objectMapper.writeValueAsString(resultVO.getData()), new TypeReference<List<TagVo>>() {
        });

    }

}