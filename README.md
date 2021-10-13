# Spring boot JPA + Oracle XE running in a container
This is a sample project using JPA against an Oracle DB (the Express Edition XE which can be used for testing).
First, we need to have an Oracle DB installed and available, if you don't have one follow the instructions on next step,
otherwise skip it. Testing in the same database that runs in production helps find problems that would otherwise only be found
when deploying in production, for instance in this application, class `Phone` does have a field `phoneNumber` which cannot
be named `number`, as it is an [Oracle reserved keyword](https://docs.oracle.com/cd/A97630_01/appdev.920/a42525/apb.htm). 

This application encodes a REST service (defined in `StudentController.java`) with several endpoints that can be 
accessed through GET, DELETE and POST Http methods. There are several Cucumber test scenarios for the REST service in
`student_registration_and_deletion.feature`. 

Some of these endpoints can also be accessed via messaging, and are tested in `student_registration_and_deletion.feature`
using RabbitMQ testcontainers or Spring Integration API.

The following schema summarizes the relationships between `@Entity` classes in this project ![image](src/main/resources/images/schema.png)

## Pre-requisites (if you don't have access to an Oracle DB)
We will be using Oracle XE in a Docker container, which you can install through the following steps:

* Install [Docker](https://www.docker.com/get-started)  

* Clone Oracle docker-images GitHub repository:

`git clone git@github.com:oracle/docker-images.git`

* enter into this folder
 
`OracleDatabase/SingleInstance/dockerfiles`

* We'll be building a docker container for Oracle DB version 18.4.0 XE. Run the following script:

`./buildContainerImage.sh -v 18.4.0 -x`

* Create a docker volume so that data in the DB is not lost after restarting the container:

`docker volume create oracle18.4.0XE`

We will link this volume (a folder in your filesystem) to a specific folder within the container.
You can check where is the volume in your filesystem with this command `docker volume inspect oracle18.4.0XE`,
you will see something like: 
```
[
    {
        "CreatedAt": "2021-10-01T09:40:52+02:00",
        "Driver": "local",
        "Labels": {},
        "Mountpoint": "/var/lib/docker/volumes/oracle18.4.0XE/_data",
        "Name": "oracle18.4.0XE",
        "Options": {},
        "Scope": "local"
    }
]
```

Next, stop the docker container with `docker stop ora18xe` and copy that the Mountpoint folder and all its contents to
a folder in the project's root named `oracle18.4.0XE`, executing the following command within the project's root folder:
`sudo cp -Rp /var/lib/docker/volumes/oracle18.4.0XE/_data oracle18.4.0XE` (the `-p` will preserve the permissions and ownership of the files).
Now start again the docker container with `docker start ora18xe`.

This will be useful as the project is using [Testcontainers](https://www.testcontainers.org/) to automatically start a docker container
with OracleXE, but we want it to use the data that we currently have instead of going through the slow initialization every time. 

* Run docker image previously built. We will link our port 1521 to the container's port 1521,
also link the shared directory created in the previous step to the container's /opt/oracle/oradata/ folder.

`docker run -i -t -d --hostname ora18xe --name ora18xe -p 1521:1521 -v oracle18.4.0XE:/opt/oracle/oradata oracle/database:18.4.0-xe`

This will take several minutes (~10 in my laptop) before the DB is ready, meanwhile you can check the logs of the container.

* Inspect the logs of the docker container and note down the password for SYSTEM user, also the pluggable database value (ie. `ora18xe/XEPDB1`).
Every time you run the docker container it will create and assign a different password to SYS and SYSTEM users, so the DataSource pointing to it might need
need to be updated if is using the SYS or SYSTEM user.

`docker logs ora18xe -f`

The following message in the logs indicates the database is ready:
```
#########################
DATABASE IS READY TO USE!
#########################
```

## Configure DataSource 

* Connect to Oracle to set up a user. You can use [Oracle SQL Developer](https://www.oracle.com/database/technologies/appdev/sqldeveloper-landing.html)
or using your IDE (IntelliJ Idea Database tab allows you to configure DataSource for many databases).

Use the following values to access Oracle as SYSTEM user:
```
Host: localhost
SID: XE
User: SYSTEM
Password: (the password noted down from the logs)
URL: jdbc:oracle:thin:@localhost:1521:XE
```
If you use SQL developer, the service name is `XEPDB1` (from the pluggable database value noted down from the logs).

Then execute the following SQL to create user `testuser` with password `testpassword`:
```sql
alter session set "_ORACLE_SCRIPT"=true;
create user testuser identified by testpassword
    quota unlimited on users;
grant connect, resource to testuser;
```
After creating the `testuser`, configure a new DataSource to connect to Oracle XE using it.
If you want to use a different username and password, remember to update accordingly
the `application.yml` file.

When you have finished you can stop the container and restart it, let's check that the data has survived restarting the container:
`docker stop ora18xe`
`docker start ora18xe`

## Test the data source
So far we do not have any tables in OracleXE, but we can leverage JPA to create these for us. 
We can set the `spring.jpa.hibernate.ddl-auto` property `create` in the file `application.yml`,
and run `Application.java` to create the tables specified in our JPA mapping automatically.

Other values besides `create` are `none`, `update`, `validate`, `create-drop` (see [this StackOverflow answer](https://stackoverflow.com/a/42147995/923509)),
in production `none` or `validate` are a safer choice than `create`, `update`. 

You can check which tables were created executing the following SQL through the DataSource console:
```sql
SELECT table_name
FROM user_tables
ORDER BY table_name;
```

(Oracle DBs does not support `show tables`).

## Data model

## RabbitMQ admin console

http://localhost:15672
