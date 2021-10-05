drop table course cascade constraints
drop table student cascade constraints
drop table student_course_register cascade constraints
drop table user_details cascade constraints
create table course (id number(10,0) generated as identity, description varchar2(255 char), name varchar2(255 char), primary key (id))
create table student (id number(10,0) generated as identity, name varchar2(255 char), user_details_id number(10,0), primary key (id))
create table student_course_register (student_id number(10,0) not null, course_id number(10,0) not null)
create table user_details (id number(10,0) generated as identity, email varchar2(255 char), first_name varchar2(255 char), last_name varchar2(255 char), password varchar2(255 char), primary key (id))
alter table student add constraint FK1kblhr83ee15gn23xwrwj7ukd foreign key (user_details_id) references user_details
alter table student_course_register add constraint FKl9cd7uopcl3x43xxlpl7jetlh foreign key (course_id) references course
alter table student_course_register add constraint FK8qnsabguaws2ptomuqpg2965e foreign key (student_id) references student
INSERT INTO user_details(email,first_Name,last_Name,password) VALUES ('admin@admin.com','admin','admin','admin');
INSERT INTO user_details(email,first_Name,last_Name,password) VALUES ('john@gmail.com','john','doe','johndoe');
INSERT INTO user_details(email,first_Name,last_Name,password) VALUES ('sham@yahoo.com','sham','tis','shamtis');