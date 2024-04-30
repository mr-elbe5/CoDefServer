
alter table t_defect add location VARCHAR(1000) NOT NULL DEFAULT '';

alter table t_project add idx INTEGER NOT NULL DEFAULT 0;
alter table t_project add zip_code VARCHAR(20) NOT NULL DEFAULT '';
alter table t_project add city VARCHAR(255) NOT NULL DEFAULT '';
alter table t_project add street VARCHAR(255) NOT NULL DEFAULT '';
alter table t_project add weather_station VARCHAR(20) NOT NULL DEFAULT '';

alter table t_configuration add country_code VARCHAR(20) NOT NULL DEFAULT 'de';
alter table t_configuration add timezone_name VARCHAR(40) NOT NULL DEFAULT 'Europe/Berlin';
alter table t_configuration add meteostat_key VARCHAR(80) NOT NULL DEFAULT '';

CREATE TABLE IF NOT EXISTS t_project_daily_report
(
    id              INTEGER NOT NULL,
    idx             INTEGER NOT NULL DEFAULT 0,
    weather_coco    VARCHAR(100) NOT NULL DEFAULT '',
    weather_wspd    VARCHAR(40) NOT NULL DEFAULT '',
    weather_wdir    VARCHAR(40) NOT NULL DEFAULT '',
    weather_temp    VARCHAR(40) NOT NULL DEFAULT '',
    weather_rhum    VARCHAR(40) NOT NULL DEFAULT '',
    CONSTRAINT t_project_daily_report_pk PRIMARY KEY (id),
    CONSTRAINT t_project_daily_report_fk1 FOREIGN KEY (id) REFERENCES t_content (id) ON DELETE CASCADE,
    CONSTRAINT t_project_daily_report_uq1 UNIQUE (idx)
);

CREATE TABLE IF NOT EXISTS t_company_briefing
(
    company_id INTEGER     NOT NULL,
    project_daily_report_id  INTEGER     NOT NULL,
    activity        VARCHAR(1000) NOT NULL DEFAULT '',
    briefing        VARCHAR(1000) NOT NULL DEFAULT '',
    CONSTRAINT t_company_briefing_pk PRIMARY KEY (company_id, project_daily_report_id),
    CONSTRAINT t_company_briefing_fk1 FOREIGN KEY (company_id) REFERENCES t_company (id) ON DELETE CASCADE,
    CONSTRAINT t_company_briefing_fk2 FOREIGN KEY (project_daily_report_id) REFERENCES t_project_daily_report (id) ON DELETE CASCADE
);