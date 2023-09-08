package com.supcon.ses.dataupload.model.env;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class IdDataDTO {

    private String id;

    private List<Map<String, Object>> data;

}
