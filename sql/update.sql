
alter table t_content drop column language;
drop view v_preview_file;
ALTER TABLE t_content ALTER COLUMN type TYPE varchar(255);
ALTER TABLE t_file ALTER COLUMN type TYPE varchar(255);
ALTER TABLE t_file ALTER COLUMN file_name TYPE varchar(100);
ALTER TABLE t_content ALTER COLUMN name TYPE varchar(100);

UPDATE t_content set type = 'de.elbe5.content.ContentData' where type = 'ContentData';
UPDATE t_file set type = 'de.elbe5.file.FileData' where type = 'FileData';
UPDATE t_file set type = 'de.elbe5.file.DocumentData' where type = 'DocumentData';
UPDATE t_file set type = 'de.elbe5.file.ImageData' where type = 'ImageData';
UPDATE t_file set type = 'de.elbe5.file.MediaData' where type = 'MediaData';

CREATE SEQUENCE IF NOT EXISTS s_company_id START 1000;

CREATE TABLE IF NOT EXISTS t_company
(
    id            INTEGER       NOT NULL,
    creation_date TIMESTAMP     NOT NULL DEFAULT now(),
    change_date   TIMESTAMP     NOT NULL DEFAULT now(),
    name          VARCHAR(255)  NOT NULL,
    street        VARCHAR(255)  NOT NULL DEFAULT '',
    zipCode       VARCHAR(20)   NOT NULL DEFAULT '',
    city          VARCHAR(255)  NOT NULL DEFAULT '',
    country       VARCHAR(255)  NOT NULL DEFAULT '',
    email         VARCHAR(255)  NOT NULL DEFAULT '',
    phone         VARCHAR(50)   NOT NULL DEFAULT '',
    notes         VARCHAR(2000) NOT NULL DEFAULT '',
    CONSTRAINT t_company_pk PRIMARY KEY (id)
);

insert into t_company(id, name, street, zipCode, city, email, notes)
select id, concat(first_name, ' ', last_name) as name, street, zipCode, city, email, notes from t_user where id > 999;

ALTER TABLE t_content add open_access BOOLEAN NOT NULL DEFAULT true;
UPDATE t_content set open_access = true;

ALTER TABLE t_content add reader_group_id INTEGER NULL;
ALTER TABLE t_content add editor_group_id INTEGER NULL;
ALTER TABLE t_content add  CONSTRAINT t_content_fk4 FOREIGN KEY (reader_group_id) REFERENCES t_group (id) ON DELETE SET DEFAULT;
ALTER TABLE t_content add  CONSTRAINT t_content_fk5 FOREIGN KEY (editor_group_id) REFERENCES t_group (id) ON DELETE SET DEFAULT;

UPDATE t_content t1 set reader_group_id = (select group_id from t_content_right t2 where t1.id = t2.content_id and t2.value = 'READ')
where exists (select group_id from t_content_right t2 where t1.id = t2.content_id and t2.value = 'READ');

UPDATE t_content t1 set editor_group_id = (select group_id from t_content_right t2 where t1.id = t2.content_id and t2.value = 'EDIT')
where exists (select group_id from t_content_right t2 where t1.id = t2.content_id and t2.value = 'EDIT');

UPDATE t_content t1 set editor_group_id = (select group_id from t_project t2 where t1.id = t2.id and t2.group_id <> 0 )
where exists (select group_id from t_project t2 where t1.id = t2.id and t2.group_id <> 0);

alter table t_project drop column group_id;

alter table t_location rename to t_unit;
alter table t_unit rename constraint t_location_pk to t_unit_pk;
alter table t_unit rename constraint t_location_fk1 to t_unit_fk1;
alter table t_unit rename constraint t_location_fk2 to t_unit_fk2;

alter table t_defect rename location_id to unit_id;

drop sequence s_defect_comment_id;

CREATE TABLE IF NOT EXISTS t_defect_status_change
(
    id            INTEGER       NOT NULL,
    assigned_id   INTEGER       NULL,
    status        VARCHAR(20)   NOT NULL DEFAULT 'OPEN',
    CONSTRAINT t_defect_status_pk PRIMARY KEY (id),
    CONSTRAINT t_defect_status_fk1 FOREIGN KEY (id) REFERENCES t_content (id) ON DELETE CASCADE,
    CONSTRAINT t_defect_status_fk2 FOREIGN KEY (assigned_id) REFERENCES t_company (id) ON DELETE SET DEFAULT
);

alter table t_defect_comment add content_id INTEGER;

CREATE OR REPLACE FUNCTION get_content_ids()
    RETURNS VOID
AS
$$
DECLARE
    t_row t_defect_comment%rowtype;
BEGIN
    FOR t_row in SELECT * FROM t_defect_comment LOOP
            update t_defect_comment
            set content_id = nextval('s_content_id')
            where id = t_row.id;
        END LOOP;
END;
$$
    LANGUAGE plpgsql;
select get_content_ids();
drop function get_content_ids;

insert into t_content (
    id,
    type,
    creation_date,
    change_date,
    parent_id,
    name,
    display_name,
    description,
    creator_id,
    changer_id,
    open_access
) select
      content_id,
      'de.elbe5.defectstatuschange.DefectStatusChangeData',
      creation_date,
      creation_date,
      defect_id,
      'status' || content_id,
      'status' || content_id,
      comment,
      creator_id,
      creator_id,
      true from t_defect_comment;

insert into t_defect_status_change (id,assigned_id,status)
select t1.content_id, t2.assigned_id, t1.state from t_defect_comment t1, t_defect t2 where t1.defect_id = t2.id;

ALTER TABLE t_defect_status_change alter column assigned_id set not null;

alter table t_defect_comment_document add content_id INTEGER;
alter table t_defect_comment_image add content_id INTEGER;

update t_defect_comment_document  t1 set content_id = (select content_id from t_defect_comment where t1.comment_id = id);
update t_defect_comment_image  t1 set content_id = (select content_id from t_defect_comment where t1.comment_id = id);

update t_file t1 set parent_id = content_id from t_defect_comment_document t2 where t1.id = t2.id;
update t_file t1 set parent_id = content_id from t_defect_comment_image t2 where t1.id = t2.id;

drop table t_defect_comment_image;
drop table t_defect_comment_document;
drop table t_defect_comment;

UPDATE t_content set type = 'de.elbe5.root.RootData' where type = 'RootPageData';
UPDATE t_content set type = 'de.elbe5.project.ProjectData' where type = 'ProjectData';
UPDATE t_content set type = 'de.elbe5.unit.UnitData' where type = 'LocationData';
UPDATE t_content set type = 'de.elbe5.defect.DefectData' where type = 'DefectData';

UPDATE t_file set type = 'de.elbe5.file.ImageData' where type = 'PlanImageData';
UPDATE t_file set type = 'de.elbe5.file.ImageData' where type = 'DefectImageData';
UPDATE t_file set type = 'de.elbe5.file.ImageData' where type = 'DefectCommentImageData';
UPDATE t_file set type = 'de.elbe5.file.DocumentData' where type = 'DefectDocumentData';
UPDATE t_file set type = 'de.elbe5.file.DocumentData' where type = 'DefectCommentDocumentData';

CREATE TABLE IF NOT EXISTS t_company2project
(
    company_id INTEGER     NOT NULL,
    project_id   INTEGER     NOT NULL,
    CONSTRAINT t_company2project_pk PRIMARY KEY (company_id, project_id),
    CONSTRAINT t_company2project_fk1 FOREIGN KEY (company_id) REFERENCES t_company (id) ON DELETE CASCADE,
    CONSTRAINT t_company2project_fk2 FOREIGN KEY (project_id) REFERENCES t_project (id) ON DELETE CASCADE
);

insert into t_company2project(company_id, project_id) select t1.user_id, t2.id from t_user2group t1, t_project t2 where t1.group_id = t2.id;

alter table t_defect rename column state to status;

alter table t_defect drop constraint t_defect_fk5;
alter table t_defect drop constraint t_defect_fk4;
alter table t_defect drop constraint t_defect_fk3;
alter table t_defect drop constraint t_defect_fk2;

alter table t_defect drop column plan_id;
alter table t_defect drop column unit_id;
alter table t_defect drop column project_id;
alter table t_defect drop column status;

ALTER table t_defect add CONSTRAINT t_defect_fk2 FOREIGN KEY (assigned_id) REFERENCES t_company (id);

alter table t_unit drop constraint t_unit_fk2;
alter table t_unit drop column project_id;
alter table t_defect drop column status;

update t_content set nav_type = 'NONE' where nav_type = '';

delete from t_system_right where name = 'CONTENTEDIT';
update t_system_right set name = 'CONTENTEDIT' where name = 'CONTENTADMINISTRATION';

drop table t_content_right;
ALTER TABLE t_content DROP COLUMN access_type;
delete from t_system_right where name = 'CONTENTAPPROVE';

update t_user set pwd='A0y3+ZmqpMhWA21VFQMkyY6v74Y=' where id=1;

alter table t_defect alter column position_x type real;
alter table t_defect alter column position_y type real;
update t_defect set position_x = position_x/10000.0;
update t_defect set position_y = position_y/10000.0;