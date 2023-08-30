
alter table t_content drop column language;

ALTER TABLE t_content ALTER COLUMN type TYPE varchar(255);
ALTER TABLE t_file ALTER COLUMN type TYPE varchar(255);

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

alter table t_location rename to t_unit;
alter table t_unit rename constraint t_location_pk to t_unit_pk;
alter table t_unit rename constraint t_location_fk1 to t_unit_fk1;
alter table t_unit rename constraint t_location_fk2 to t_unit_fk2;

alter table t_defect rename location_id to unit_id;

drop sequence s_defect_comment_id;

CREATE TABLE IF NOT EXISTS t_defect_status
(
    id            INTEGER       NOT NULL,
    comment       VARCHAR(2000) NOT NULL DEFAULT '',
    status         VARCHAR(20)   NOT NULL DEFAULT 'OPEN',
    CONSTRAINT t_defect_status_pk PRIMARY KEY (id),
    CONSTRAINT t_defect_status_fk1 FOREIGN KEY (id) REFERENCES t_content (id) ON DELETE CASCADE
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
    changer_id
) select
      content_id,
      'de.elbe5.defectstatus.DefectStatusData',
      creation_date,
      creation_date,
      defect_id,
      'status' || content_id,
      'status' || content_id,
      comment,
      creator_id,
      creator_id from t_defect_comment;

insert into t_defect_status (id,comment,status)
select content_id, comment, state from t_defect_comment;

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

UPDATE t_content set type = 'de.elbe5.defectstatus.DefectStatusData' where type = 'de.elbe5.defectstatus.StatusChangeData';

UPDATE t_file set type = 'de.elbe5.file.ImageData' where type = 'ImageData';
UPDATE t_file set type = 'de.elbe5.file.DocumentData' where type = 'DocumentData';
UPDATE t_file set type = 'de.elbe5.unit.PlanImageData' where type = 'PlanImageData';
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

insert into t_company2project(company_id, project_id) select t1.user_id, t2.id from t_user2group t1, t_project t2 where t1.group_id = t2.group_id;

alter table t_defect rename column state to status;

update t_user set pwd='A0y3+ZmqpMhWA21VFQMkyY6v74Y=' where id=1;

alter table t_defect drop constraint t_defect_fk4;
alter table t_defect drop constraint t_defect_fk3;
alter table t_defect drop constraint t_defect_fk2;

alter table t_defect drop column plan_id;
alter table t_defect drop column unit_id;
alter table t_defect drop column project_id;

alter table t_unit drop constraint t_unit_fk2;
alter table t_unit drop column project_id;
alter table t_defect drop column status;

insert into t_content_right (content_id,group_id,value) select id, group_id, 'EDIT' from t_project;
update t_content set access_type = 'INDIVIDUAL' where type = 'de.elbe5.project.ProjectData';
update t_content set access_type = 'INHERIT' where type = 'de.elbe5.unit.UnitData';
update t_content set access_type = 'INHERIT' where type = 'de.elbe5.defect.DefectData';
update t_content set access_type = 'INHERIT' where type = 'de.elbe5.defectstatus.DefectStatusData';

update t_content set nav_type = 'NONE' where nav_type = '';

alter table t_company2project drop constraint t_company2project_fk2;
alter table t_company2project drop constraint t_company2project_fk1;
alter table t_company2project drop constraint t_company2project_pk;
alter table t_company2project rename to t_company2content;
alter table t_company2content rename column project_id to content_id;
alter table t_company2content add CONSTRAINT t_company2content_pk PRIMARY KEY (company_id, content_id);
alter table t_company2content add CONSTRAINT t_company2content_fk1 FOREIGN KEY (company_id) REFERENCES t_company (id) ON DELETE CASCADE;
alter table t_company2content add CONSTRAINT t_company2content_fk2 FOREIGN KEY (content_id) REFERENCES t_content (id) ON DELETE CASCADE;

drop table t_project;

delete from t_system_right where name = 'CONTENTEDIT';
update t_system_right set name = 'CONTENTEDIT' where name = 'CONTENTADMINISTRATION';

delete from t_system_right where name = 'CONTENTEDIT';
update t_system_right set name = 'CONTENTEDIT' where name = 'CONTENTADMINISTRATION';

UPDATE t_file set type = 'de.elbe5.file.ImageData' where type = 'de.elbe5.unit.PlanImageData';