
alter table t_user add type VARCHAR(60);
update t_user set type='de.elbe5.user.CodefUserData';
alter table t_user alter column type set NOT NULL;

alter table t_user add name VARCHAR(255);
update t_user set name = last_name where first_name = '';
update t_user set name = first_name || ' ' || last_name where first_name <> '';

CREATE TABLE IF NOT EXISTS t_codef_user
(
    id                 INTEGER      NOT NULL,
    project_id         INTEGER      NULL,
    company_ids        VARCHAR(60)  NOT NULL DEFAULT '',
    show_closed        BOOLEAN      NOT NULL DEFAULT true,
    view_restriction   VARCHAR(30)  NOT NULL DEFAULT '',
    CONSTRAINT t_codef_user_pk PRIMARY KEY (id),
    CONSTRAINT t_codef_user_fk1 FOREIGN KEY (id) REFERENCES t_user(id) ON DELETE CASCADE
);

insert into t_codef_user select id from t_user;

alter table t_user drop column first_name;
alter table t_user drop column last_name;
alter table t_user drop column street;
alter table t_user drop column zipcode;
alter table t_user drop column city;
alter table t_user drop column country;
alter table t_user drop column phone;
alter table t_user drop column mobile;
alter table t_user drop column notes;

alter table t_user add active BOOLEAN NOT NULL DEFAULT TRUE;
--
alter table t_user drop column locked;
alter table t_user drop column deleted;

update t_content set type = 'de.elbe5.defectstatus.StatusChangeData'
                 where type = 'de.elbe5.defectstatuschange.DefectStatusChangeData';

CREATE TABLE IF NOT EXISTS t_configuration
(
    title            VARCHAR(100) NOT NULL DEFAULT '',
    salt             VARCHAR(100) NOT NULL DEFAULT '',
    locale           VARCHAR(30) NOT NULL DEFAULT 'GERMAN',
    show_date_time   BOOLEAN NOT NULL DEFAULT false,
    use_read_rights  BOOLEAN NOT NULL DEFAULT false,
    use_read_group   BOOLEAN NOT NULL DEFAULT false,
    use_editor_group BOOLEAN NOT NULL DEFAULT false,
    show_inactive_content BOOLEAN NOT NULL DEFAULT false,
    use_notified BOOLEAN NOT NULL DEFAULT false
);
insert into t_configuration (title, salt) values ('Codef', '4FGOXcMTbG0=');

alter table t_user add creator_id    INTEGER       NOT NULL DEFAULT 1;
alter table t_user add changer_id    INTEGER       NOT NULL DEFAULT 1;
alter table t_user add creation_date TIMESTAMP     NOT NULL DEFAULT now();
alter table t_user add CONSTRAINT t_user_fk1 FOREIGN KEY (creator_id) REFERENCES t_user (id) ON DELETE SET DEFAULT;
alter table t_user add CONSTRAINT t_user_fk2 FOREIGN KEY (changer_id) REFERENCES t_user (id) ON DELETE SET DEFAULT;

alter table t_group add creator_id    INTEGER       NOT NULL DEFAULT 1;
alter table t_group add changer_id    INTEGER       NOT NULL DEFAULT 1;
alter table t_group add creation_date TIMESTAMP     NOT NULL DEFAULT now();
alter table t_group add CONSTRAINT t_group_fk1 FOREIGN KEY (creator_id) REFERENCES t_user (id) ON DELETE SET DEFAULT;
alter table t_group add CONSTRAINT t_group_fk2 FOREIGN KEY (changer_id) REFERENCES t_user (id) ON DELETE SET DEFAULT;

alter table t_company add creator_id    INTEGER       NOT NULL DEFAULT 1;
alter table t_company add changer_id    INTEGER       NOT NULL DEFAULT 1;
alter table t_company add CONSTRAINT t_company_fk1 FOREIGN KEY (creator_id) REFERENCES t_user (id) ON DELETE SET DEFAULT;
alter table t_company add CONSTRAINT t_company_fk2 FOREIGN KEY (changer_id) REFERENCES t_user (id) ON DELETE SET DEFAULT;

---

alter table t_defect_status_change rename to t_defect_status;
alter table t_defect_status rename constraint t_defect_status_change_pk to t_defect_status_pk;
alter table t_defect_status rename constraint t_defect_status_change_fk1 to t_defect_status_fk1;
alter table t_defect_status rename constraint t_defect_status_change_fk2 to t_defect_status_fk2;
update t_content set type = 'de.elbe5.defectstatus.DefectStatusData' where type = 'de.elbe5.defectstatus.DefectStatusChangeData';

--

alter table t_defect alter column position_comment type VARCHAR(1000);
alter table t_defect drop column display_id;
drop sequence s_defect_id;

--

alter table t_configuration add sync_project_companies BOOLEAN NOT NULL DEFAULT true;