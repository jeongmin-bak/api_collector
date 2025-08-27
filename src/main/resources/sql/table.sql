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
);

create table SWCDITBFAD001 (
    FRG_BUR_C   varchar(3) not null, -- 대외기관코드
    SV_NM       varchar(150) not null, --  서비스명
    OTSD_LINK_URL varchar(1000) not null, -- 외부링크URL
    LNK_DTA_DRM_ID varchar(150) not null, -- 연동데이터식별ID 서비스명+UUID
    FRG_IF_BUR_NM   varchar(100) not null, -- 대외인터페이스기관명
    API_DFNTN_CN    varchar(1000) not null, -- API정의내용
    API_RSP_CN      CLOB    not null, -- API응답내용
    COL_DTA_TP_NM   varchar(15) not null, -- 컬럼데이터유형명(데이터포맷)
    BSDT    varchar(8) not null, -- 기준일자
    LDNG_TS timestamp -- 적재일자
    constraint PK_SWCDITBFAD002
        primary key (FRG_BUR_C, SV_NM, OTSD_LINK_URL, LNK_DTA_DRM_ID)
);

create table SWCDITBFAD002 (
    FRG_BUR_C   varchar(3) not null, -- 대외기관코드
    SV_NM       varchar(150) not null, --  서비스명
    OTSD_LINK_URL varchar(1000) not null, -- 외부링크URL
    LNK_DTA_DRM_ID varchar(150) not null, -- 연동데이터식별ID
    FRG_IF_BUR_NM   varchar(100) not null, -- 대외인터페이스기관명
    API_DFNTN_CN    varchar(1000) not null, -- API정의내용
    API_RSP_CN      CLOB    not null, -- API응답내용
    COL_DTA_TP_NM   varchar(15) not null, -- 컬럼데이터유형명
    BSDT    varchar(8) not null, -- 기준일자
    LDNG_TS timestamp -- 적재일자
    constraint PK_SWCDITBFAD002
        primary key (FRG_BUR_C, SV_NM, OTSD_LINK_URL, LNK_DTA_DRM_ID)
)