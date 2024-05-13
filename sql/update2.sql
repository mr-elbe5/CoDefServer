
alter table t_defect add location VARCHAR(1000) NOT NULL DEFAULT '';

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
    CONSTRAINT t_project_daily_report_fk1 FOREIGN KEY (id) REFERENCES t_content (id) ON DELETE CASCADE
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

--

ALTER TABLE t_project_daily_report ADD report_date TIMESTAMP NOT NULL DEFAULT now();
UPDATE t_project_daily_report t1 SET report_date = (SELECT t2.creation_date FROM t_content t2 WHERE t2.id = t1.id);

--

ALTER TABLE t_codef_user ADD show_open BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE t_codef_user ADD show_disputed BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE t_codef_user ADD show_rejected BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE t_codef_user ADD show_done BOOLEAN NOT NULL DEFAULT true;