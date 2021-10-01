CREATE TABLE user_details
(
   id          INT            NOT NULL AUTO_INCREMENT,
   email       VARCHAR(255),
   first_name  VARCHAR(255),
   last_name   VARCHAR(255),
   password    VARCHAR(255),
   PRIMARY KEY (id)
);

INSERT INTO user_details(email,first_Name,last_Name,password) VALUES ('admin@admin.com','admin','admin','admin');

INSERT INTO user_details(email,first_Name,last_Name,password) VALUES ('john@gmail.com','john','doe','johndoe');
INSERT INTO user_details(email,first_Name,last_Name,password) VALUES ('sham@yahoo.com','sham','tis','shamtis');