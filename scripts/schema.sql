CREATE TABLE SES_ENV_MONITOR_CONFIG (
    BIZ_ID VARCHAR(255) PRIMARY KEY,
    NAME VARCHAR(255),
    TAG_NAME VARCHAR(255),
    TAG_VALUE FLOAT,
    VALID INT DEFAULT 1,
    CID BIGINT,
    UNIT VARCHAR(255)
);