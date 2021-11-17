create table course
(
    id          number(10,0) generated as identity,
    description varchar2(255 char),
    name        varchar2(255 char),
    primary key (id)
);

create table phone
(
    id                 number(10,0) generated as identity,
    phone_number       varchar2(255 char),
    related_student_id number(10,0),
    primary key (id)
);

create table student
(
    id          number(10,0) generated as identity,
    keycloak_id varchar2(255 char) not null,
    primary key (id)
);

create table student_course_register
(
    id         number(10,0) generated as identity,
    course_id  number(10,0),
    student_id number(10,0),
    primary key (id)
);

create table student_course_score
(
    id              number(10,0) generated as identity,
    score           double precision,
    registration_id number(10,0),
    teacher_id      number(10,0),
    primary key (id)
);

create table teacher
(
    id          number(10,0) generated as identity,
    keycloak_id varchar2(255 char) not null,
    primary key (id)
);

create table teacher_course_assignations
(
    id         number(10,0) generated as identity,
    course_id  number(10,0),
    teacher_id number(10,0),
    primary key (id)
);

alter table student
    add constraint UK_e22iig40nakkf3ru7wf6cyhx7 unique (keycloak_id);

alter table student_course_register
    add constraint UKil312wtw5b6hux50469a6y2fu unique (student_id, course_id);

alter table teacher
    add constraint UK_h5pqlqxkklrt9hoporxebe58t unique (keycloak_id);

alter table teacher_course_assignations
    add constraint UKloiweylfxa8vvrn38dfyxyxtb unique (teacher_id, course_id);

alter table phone
    add constraint FKknqkcxvh3vlkfp0l7ffdhv5of
        foreign key (related_student_id)
            references student;

alter table student_course_register
    add constraint FKl9cd7uopcl3x43xxlpl7jetlh
        foreign key (course_id)
            references course
            on delete cascade;

alter table student_course_register
    add constraint FK8qnsabguaws2ptomuqpg2965e
        foreign key (student_id)
            references student
            on delete cascade;

alter table student_course_score
    add constraint FKh0vwbcyanryqjugdivow1tosm
        foreign key (registration_id)
            references student_course_register;

alter table student_course_score
    add constraint FKsy3ckynmdkxdvhwsa412rv0lq
        foreign key (teacher_id)
            references teacher;

alter table teacher_course_assignations
    add constraint FKs8lmyr5vyc3wef63qqe3s61gn
        foreign key (course_id)
            references course
            on delete cascade;

alter table teacher_course_assignations
    add constraint FK5ahjvwoktw20qkjlj5rk79wb
        foreign key (teacher_id)
            references teacher
            on delete cascade;
