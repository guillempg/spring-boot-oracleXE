CREATE TABLE course
(
      id          NUMBER(10,0) generated AS identity,
      description VARCHAR2(255 CHAR),
      name        VARCHAR2(255 CHAR),
      PRIMARY KEY (id)
);

CREATE TABLE phone
(
      id                 NUMBER(10,0) generated AS identity,
      phone_number       VARCHAR2(255 CHAR),
      related_student_id NUMBER(10,0),
      PRIMARY KEY (id)
);

CREATE TABLE student
(
      id          NUMBER(10,0) generated AS identity,
      keycloak_id VARCHAR2(255 CHAR) NOT NULL,
      PRIMARY KEY (id)
);

CREATE TABLE student_course_register
(
      id         NUMBER(10,0) generated AS identity,
      course_id  NUMBER(10,0),
      student_id NUMBER(10,0),
      PRIMARY KEY (id)
);

CREATE TABLE student_course_score
(
      id NUMBER(10,0) generated AS identity,
      score DOUBLE PRECISION,
      registration_id NUMBER(10,0),
      teacher_id      NUMBER(10,0),
      PRIMARY KEY (id)
);

CREATE TABLE teacher
(
      id          NUMBER(10,0) generated AS identity,
      keycloak_id VARCHAR2(255 CHAR) NOT NULL,
      PRIMARY KEY (id)
);

CREATE TABLE teacher_course_assignations
(
      id         NUMBER(10,0) generated AS identity,
      course_id  NUMBER(10,0),
      teacher_id NUMBER(10,0),
      PRIMARY KEY (id)
);

ALTER TABLE student ADD CONSTRAINT student_keycloak_id_uk UNIQUE (keycloak_id);
ALTER TABLE student_course_register ADD CONSTRAINT student_course_register_student_id_course_id_uk UNIQUE (student_id, course_id);
ALTER TABLE teacher ADD CONSTRAINT teacher_keycloak_id_uk UNIQUE (keycloak_id);
ALTER TABLE teacher_course_assignations ADD CONSTRAINT teacher_course_assignations_teacher_id_course_id_uk UNIQUE (teacher_id, course_id);

ALTER TABLE phone ADD CONSTRAINT phone_related_student_id_fk FOREIGN KEY (related_student_id) REFERENCES student;
ALTER TABLE student_course_register ADD CONSTRAINT student_course_register_course_id_fk FOREIGN KEY (course_id) REFERENCES course;
ALTER TABLE student_course_register ADD CONSTRAINT student_course_register_student_id_fk FOREIGN KEY (student_id) REFERENCES student;
ALTER TABLE student_course_score ADD CONSTRAINT student_course_score_registration_id_fk FOREIGN KEY (registration_id) REFERENCES student_course_register;
ALTER TABLE student_course_score ADD CONSTRAINT student_course_score_teacher_id_fk FOREIGN KEY (teacher_id) REFERENCES teacher;
ALTER TABLE teacher_course_assignations ADD CONSTRAINT teacher_course_assignations_course_id_fk FOREIGN KEY (course_id) REFERENCES course;
ALTER TABLE teacher_course_assignations ADD CONSTRAINT teacher_course_assignations_teacher_id_fk FOREIGN KEY (teacher_id) REFERENCES teacher;