create table person
(
    id          number(10,0) generated as identity,
    keycloak_id varchar2(255 char) not null,
    role        varchar2(255 char) not null,
    primary key (id)
);
-- here there should be a copy of contents from student and teacher tables into person, but don't have any data so we skip this step
drop table student cascade constraints;
drop table teacher cascade constraints;
