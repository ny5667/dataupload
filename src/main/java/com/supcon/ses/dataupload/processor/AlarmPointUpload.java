package com.supcon.ses.dataupload.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.supcon.ses.dataupload.cache.DataUploadSettingCache;
import com.supcon.ses.dataupload.constant.DefaultSettingConstant;
import com.supcon.ses.dataupload.model.env.IdDataDTO;
import com.supcon.ses.dataupload.model.pojo.AlarmPoint;
import com.supcon.ses.dataupload.model.setting.CompanyConfig;
import com.supcon.ses.dataupload.model.vo.TagVo;
import com.supcon.ses.dataupload.repository.AlarmPointJdbcTemplateRepository;
import com.supcon.ses.dataupload.repository.TagRestTemplateRepository;
import com.supcon.ses.dataupload.utils.JsonHelper;
import com.supcon.ses.dataupload.utils.RestTemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@EnableScheduling
public class AlarmPointUpload {

    private static final Logger log = LoggerFactory.getLogger(AlarmPointUpload.class);

    private final List<String> ignoreColumnList = Arrays.asList("BIZ_ID", DefaultSettingConstant.TAG_NAME, DefaultSettingConstant.TAG_VALUE, "VALID", "CID", "ALARM_NAME"); // 忽略的列名列表

    private static final String ENV_MONITOR = "/service/env/envmonitor";

    private final AlarmPointJdbcTemplateRepository repository;

    private final TagRestTemplateRepository tagRestTemplateRepository;

    private final RestTemplateUtils restTemplateUtils;

    private static final Map<String, Float> value_map = new HashMap<>();

    static {
        value_map.put("false", 0f);
        value_map.put("true", 1f);
    }

    public AlarmPointUpload(AlarmPointJdbcTemplateRepository repository, TagRestTemplateRepository tagRestTemplateRepository, RestTemplateUtils restTemplateUtils) {
        this.repository = repository;
        this.tagRestTemplateRepository = tagRestTemplateRepository;
        this.restTemplateUtils = restTemplateUtils;
    }

    @Scheduled(fixedRate = 5 * 1000)
    public void run() throws JsonProcessingException {

        log.error("实时数据上报开始.");

        if (DataUploadSettingCache.getCompanyConfigs() == null) {
            log.error("配置信息为空，跳过.");
            return;
        }

        for (CompanyConfig company :
                DataUploadSettingCache.getCompanyConfigs()) {
            uploadDataByCompany(company);
        }

    }

    /*-----------------------------------------公共方法---------------------------------------------------*/

    /**
     * 根据公司上报实时数据
     *
     * @param company 公司信息
     */
    private void uploadDataByCompany(CompanyConfig company) throws JsonProcessingException {

        //查询实时数据
        List<Map<String, Object>> allAlarmPoint = repository.findAllMap(Long.parseLong(company.getCid()));

        //查询位号数据
        String ip = company.getAdpServerIp();
        String port = company.getAdpServerPort();
        List<String> tagNames = allAlarmPoint.stream().filter(v -> v.get(DefaultSettingConstant.TAG_NAME) != null).map(v -> v.get(DefaultSettingConstant.TAG_NAME).toString()).collect(Collectors.toList());
        if (tagNames.isEmpty()) {
            log.error("无实时上报数据.");
            return;
        }
        List<TagVo> tagVoList = tagRestTemplateRepository.findAll(ip, port, tagNames);

        //设置实时数据位号值
        setTagValueAndDefaultValue(allAlarmPoint, tagVoList);

        // 更新位号数据到数据库中
        // 如果下次测试数据取不到则用上一次实时数据库的值
        List<AlarmPoint> dataList = new ArrayList<>();
        allAlarmPoint.forEach(c -> {
            String bizId = (String) c.get("BIZ_ID");
            Float value = (Float) c.get(DefaultSettingConstant.UPLOAD_TAG_VALUE);
            AlarmPoint po = new AlarmPoint(bizId, value);
            dataList.add(po);
        });
        repository.batchUpdate(dataList);
        log.error("数据更新结束.");

        //去掉不需要上报的字段
        ignoreColumnList.forEach(ignoreColumn -> allAlarmPoint.forEach(map -> map.remove(ignoreColumn)));

        //处理上报数据/加密
        int batchSize = Integer.parseInt(company.getBitchSize());  // 每批次的大小
        for (int i = 0; i < allAlarmPoint.size(); i += batchSize) {
            List<Map<String, Object>> batch = allAlarmPoint.subList(i, Math.min(i + batchSize, allAlarmPoint.size()));
            uploadData(batch, company);
        }

        log.error("实时数据上报结束.");

    }


    /**
     * 上报数据
     *
     * @param batch   上报数据信息
     * @param company 公司信息
     */
    private void uploadData(List<Map<String, Object>> batch, CompanyConfig company) {

        UUID uuid = UUID.randomUUID();
        String guid = uuid.toString();

        IdDataDTO idDataDTO = new IdDataDTO(guid, batch);

        //请求参数
        List<IdDataDTO> list = new ArrayList<>();
        list.add(idDataDTO);

        String sendJson = JsonHelper.writeValue(list);
        log.error("发送报警消息.");
        log.error(sendJson);

        //平台ip和port
        String serverAddress = company.getServerAddress();

        //url参数
        Map<String, Object> uriVariables = new HashMap<>();

        //返回数据
        ResponseEntity<String> response = null;

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("appkey", company.getAppKey());
        headers.set("appsecret", company.getAppSecret());

        try {
            response = restTemplateUtils.post(serverAddress, ENV_MONITOR, list, String.class, uriVariables, headers);
            log.error("调用测点接口返回");
            String body = response.getBody();
            log.error(body);
        } catch (Exception ex) {
            log.error("报警消息发送失败", ex);
        }

    }

    /**
     * 设置报警点的位号值和其他默认值
     *
     * @param allAlarmPoint 实时数据
     * @param tagVoList     位号列表
     */
    private void setTagValueAndDefaultValue(List<Map<String, Object>> allAlarmPoint, List<TagVo> tagVoList) {
        Map<String, TagVo> tagVoMap = tagVoList.stream()
                .collect(Collectors.toMap(TagVo::getName, Function.identity(), (v1, v2) -> v1));

        allAlarmPoint.forEach(c -> {
            if (c.get(DefaultSettingConstant.TAG_NAME) == null) {
                return;
            }
            String tagName = c.get(DefaultSettingConstant.TAG_NAME).toString();
            TagVo tagVo = tagVoMap.get(tagName);
            if (tagVo == null || tagVo.getValue() == null || tagVo.getValue().isEmpty()) {
                if (c.get(DefaultSettingConstant.TAG_VALUE) == null) {
                    c.put(DefaultSettingConstant.UPLOAD_TAG_VALUE, 0f);//默认设置为0
                } else {
                    String tagValue = c.get(DefaultSettingConstant.TAG_VALUE).toString();
                    float v = Float.parseFloat(tagValue);
                    c.put(DefaultSettingConstant.UPLOAD_TAG_VALUE, v);//设置为上一次查询出的位号值
                }
                return;
            }
            log.error("测点取到值为：");
            log.error(tagVo.getValue());
            //如果是bool类型，则转成0/1类型
            Float boolString = value_map.get(tagVo.getValue());
            log.error("map值为：{}", boolString);
            if (boolString != null) {
                c.put(DefaultSettingConstant.UPLOAD_TAG_VALUE, boolString);//位号实时值
                return;
            }
            c.put(DefaultSettingConstant.UPLOAD_TAG_VALUE, Float.parseFloat(tagVo.getValue()));//位号实时值
            String name = c.get("NAME").toString();
            c.put("name", name);
            String unit = c.get("UNIT").toString();
            c.put("unit", unit);
        });


    }

}
