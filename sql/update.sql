
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
