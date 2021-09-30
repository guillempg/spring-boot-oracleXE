# Spring boot JPA + Oracle XE running in a container
This is a sample project using JPA against an Oracle DB (the Express Edition XE which can be used for testing).
First, we need to have an Oracle DB installed and available, if you don't have one follow the instructions on next step,
otherwise skip it.

## Pre-requisites (if you don't have access to an Oracle DB)
We will be using Oracle XE in a Docker container, which you can install through the following steps:

* Install [Docker](https://www.docker.com/get-started)  

* Clone Oracle docker-images GitHub repository:

`git clone git@github.com:oracle/docker-images.git`

* enter into this folder
 
`OracleDatabase/SingleInstance/dockerfiles`

* We'll be building a docker container for Oracle DB version 18.4.0 XE. Run the following script:

`./buildContainerImage.sh -v 18.4.0 -x`

* Create a shared directory, for instance: 

`/home/user/oracle-shared`

* Run docker image previously built. We will link our port 1521 to the container's port 1521,
also link the shared directory created in the previous step to the container's /shared folder.

`docker run -i -t -d --hostname ora18xe --name ora18xe -p 1521:1521 -v /home/user/oracle-shared:/shared oracle/database:18.4.0-xe`

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
the `application.properties` file.

