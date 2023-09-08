package com.supcon.ses.dataupload.model.setting;

import com.supcon.ses.dataupload.constant.DefaultSettingConstant;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class CompanyConfig {

    /**
     * adp平台企业cid
     */
    private String cid = DefaultSettingConstant.DEFAULT_CID;

    /**
     * 企业编号 必填
     */
    private String companyCode = StringUtils.EMPTY;

    /**
     * 企业名称 必填
     */
    private String companyName = StringUtils.EMPTY;

    /**
     * 接口认证appKey
     */
    private String appKey = StringUtils.EMPTY;

    /**
     * 接口认证appSecret
     */
    private String appSecret = StringUtils.EMPTY;

    /**
     * 上送服务器地址
     */
    private String serverAddress = StringUtils.EMPTY;

    /**
     * ADP平台的IP
     */
    private String adpServerIp = StringUtils.EMPTY;

    /**
     * ADP平台的端口
     */
    private String adpServerPort = StringUtils.EMPTY;

    /**
     * 上报一个批次的数量
     */
    private String bitchSize = StringUtils.EMPTY;

}
