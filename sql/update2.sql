
alter table t_defect add comment VARCHAR(1000) NOT NULL DEFAULT '';
update t_defect set comment = position_comment;
update t_defect set position_comment = '';

alter table t_project add idx INTEGER NOT NULL DEFAULT 0;
alter table t_project add zip_code VARCHAR(20) NOT NULL DEFAULT '';
alter table t_project add city VARCHAR(255) NOT NULL DEFAULT '';
alter table t_project add street VARCHAR(255) NOT NULL DEFAULT '';
alter table t_project add weather_station VARCHAR(20) NOT NULL DEFAULT '';

alter table t_configuration add country_code VARCHAR(20) NOT NULL DEFAULT 'de';
alter table t_configuration add meteostat_key VARCHAR(80) NOT NULL DEFAULT '';

CREATE TABLE IF NOT EXISTS t_project_diary
(
    id              INTEGER NOT NULL,
    idx             INTEGER NOT NULL DEFAULT 0,
    weather_coco    INTEGER NOT NULL DEFAULT 0,
    weather_wspd    INTEGER NOT NULL DEFAULT 0,
    weather_wdir    INTEGER NOT NULL DEFAULT 0,
    weather_temp    INTEGER NOT NULL DEFAULT 0,
    weather_rhum    INTEGER NOT NULL DEFAULT 0,
    weather_prcp    INTEGER NOT NULL DEFAULT 0,
    activity        VARCHAR(1000) NOT NULL DEFAULT '',
    briefing        VARCHAR(1000) NOT NULL DEFAULT '',
    CONSTRAINT t_project_diary_pk PRIMARY KEY (id),
    CONSTRAINT t_project_diary_fk1 FOREIGN KEY (id) REFERENCES t_content (id) ON DELETE CASCADE,
    CONSTRAINT t_project_diary_uq1 UNIQUE (idx)
);

CREATE TABLE IF NOT EXISTS t_company2project_diary
(
    company_id INTEGER     NOT NULL,
    project_diary_id  INTEGER     NOT NULL,
    CONSTRAINT t_company2project_diary_pk PRIMARY KEY (company_id, project_diary_id),
    CONSTRAINT t_company2project_diary_fk1 FOREIGN KEY (company_id) REFERENCES t_company (id) ON DELETE CASCADE,
    CONSTRAINT t_company2project_diary_fk2 FOREIGN KEY (project_diary_id) REFERENCES t_project_diary (id) ON DELETE CASCADE
);