create table API_BAS_INFO (
    API_MEATA_ID    VARCHAR2(500) not null,
    API_URL         VARCHAR2(1000) not null,
    API_SVC         VARCHAR2(500) not null,
    DATA_PV_GP      VARCHAR2(10) not null,
    DATA_FORMAT     VARCHAR2(10),
    API_EXPL        VARCHAR2(1000),
    IS_QUERY        VARCHAR2(50),
    IS_PATH         VARCHAR2(50),
    SRC_CONT_ID     VARCHAR2(500),
    PAGE_KEY        VARCHAR2(100),
    TOTAL_KEY       VARCHAR2(100),
    CNT_KEY         VARCHAR2(100),
    KEY_NAME        VARCHAR2(100),
    FRST_RGS_DTM    VARCHAR2(200),
    LAST_RGS_DTM    VARCHAR2(200),
    BSDT_KEY        VARCHAR2(100),
    BSDT            VARCHAR2(8),
    URL_REQ_TYPE    VARCHAR2(10),
    constraint PK_API_BASE_INFO
        primary key (API_META_ID, API_SVC, DATA_PV_GPs)
);

create table API_KEY_PARAM (
    API_MEATA_ID    VARCHAR2(500) not null,
    PARAM_TYPE      VARCHAR2(10),
    KEY_NM          VARCHAR2(500) not null,
    PAGE_COL        VARCHAR2(10),
    TC_COL          VARCHAR2(10),
    PARAM_ORDER     NUMBER,
    BSDT_KEY        VARCHAR2(100),
    constraint PK_API_KEY_PARAM
        primary key (API_META_ID, KEY_NM)
);

create table API_VALUE_PARAM (
    API_MEATA_ID    VARCHAR2(500) not null,
    KEY_NM          VARCHAR2(500),
    CD_ID           VARCHAR2(500),
    EACH_REQ_PARAMS VARCHAR2(1000),
    CD_GRP_ID       VARCHAR2(500),
);

create table API_CD_BAS (
    CD_GRP_ID   VARCHAR2(500) not null,
    CD_ID       VARCHAR2(1000) not null,
    CD_EXPL     VARCHAR2(500) not null,
    constraint PK_API_CD_BAS
        primary key (CD_GRP_ID, CD_ID, CD_EXPL)
)