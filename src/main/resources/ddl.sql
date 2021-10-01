create table user_details (id number(10,0) generated as identity, email varchar2(255 char), first_name varchar2(255 char), last_name varchar2(255 char), password varchar2(255 char), primary key (id))
INSERT INTO user_details(email,first_Name,last_Name,password) VALUES ('admin@admin.com','admin','admin','admin');
INSERT INTO user_details(email,first_Name,last_Name,password) VALUES ('john@gmail.com','john','doe','johndoe');
INSERT INTO user_details(email,first_Name,last_Name,password) VALUES ('sham@yahoo.com','sham','tis','shamtis');