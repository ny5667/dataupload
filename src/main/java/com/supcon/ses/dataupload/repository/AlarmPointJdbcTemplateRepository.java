package com.supcon.ses.dataupload.repository;

import com.supcon.ses.dataupload.model.pojo.AlarmPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class AlarmPointJdbcTemplateRepository {

    private final JdbcTemplate jdbcTemplate;

    public AlarmPointJdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> findAllMap(Long cid) {
        Assert.notNull(cid,"cid can not be null.");
        String sql = "SELECT * FROM SES_ENV_MONITOR_CONFIG WHERE VALID = 1 AND CID = ?";
        return jdbcTemplate.queryForList(sql, cid);
    }


    public void batchUpdate(List<AlarmPoint> dataList) {
        String sql = "UPDATE SES_ENV_MONITOR_CONFIG SET TAG_VALUE=? WHERE BIZ_ID=?";
        int[] updatedRows = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                AlarmPoint data = dataList.get(i);
                ps.setFloat(1, data.getTagValue());
                ps.setString(2, data.getBizId());
            }

            @Override
            public int getBatchSize() {
                return dataList.size();
            }
        });
        log.error("Batch updated " + updatedRows.length + " rows.");
    }

}
