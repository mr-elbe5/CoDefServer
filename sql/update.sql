CREATE TABLE IF NOT EXISTS t_content_log
(
    content_id INTEGER     NOT NULL,
    day        DATE        NOT NULL,
    count      INTEGER 	   NOT NULL,
    CONSTRAINT t_content_log_pk PRIMARY KEY (content_id, day),
    CONSTRAINT t_content_log_fk1 FOREIGN KEY (content_id) REFERENCES t_content (id) ON DELETE CASCADE
);

alter table t_content drop column language;

CREATE TABLE IF NOT EXISTS t_link
(
    id            INTEGER       NOT NULL,
    link_url      VARCHAR(500)  NOT NULL DEFAULT '',
    link_icon     VARCHAR(255)  NOT NULL DEFAULT '',
    CONSTRAINT t_link_pk PRIMARY KEY (id),
    CONSTRAINT t_link_fk1 FOREIGN KEY (id) REFERENCES t_content (id) ON DELETE CASCADE
);

ALTER TABLE t_content ALTER COLUMN type TYPE varchar(255);
ALTER TABLE t_file ALTER COLUMN type TYPE varchar(255);

UPDATE t_content set type = 'de.elbe5.content.ContentData' where type = 'ContentData';
UPDATE t_content set type = 'de.elbe5.link.LinkData' where type = 'LinkData';

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
    street        VARCHAR(255)  NOT NULL,
    zipCode       VARCHAR(20)   NOT NULL,
    city          VARCHAR(255)  NOT NULL,
    country       VARCHAR(255)  NOT NULL DEFAULT '',
    email         VARCHAR(255)  NOT NULL,
    phone         VARCHAR(50)   NOT NULL DEFAULT '',
    fax           VARCHAR(50)   NOT NULL DEFAULT '',
    description   VARCHAR(2000) NOT NULL DEFAULT '',
    CONSTRAINT t_company_pk PRIMARY KEY (id)
);

alter table t_user add company_id INTEGER NULL;
alter table t_user add CONSTRAINT t_user_fk1 FOREIGN KEY (company_id) REFERENCES t_company (id) ON DELETE SET NULL;

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
      'de.elbe5.codef.defectstatus.DefectStatusData',
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
