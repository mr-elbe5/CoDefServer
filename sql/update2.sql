
alter table t_defect add comment VARCHAR(1000) NOT NULL DEFAULT '';
update t_defect set comment = position_comment;
update t_defect set position_comment = '';