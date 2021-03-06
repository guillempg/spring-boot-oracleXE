# Sample application demo

This is a sample project to register students/assign teachers into courses whose main purpose is to demonstrate how to
do object relational mapping with JPA against an Oracle DB (the Express Edition XE which can be used for testing), for a
Spring boot application. The main functionality (student registration to courses, assigning courses to teachers, score
students...) is exposed both via REST endpoints and message queues, both configured with Spring integration. REST
endpoints are secured via Spring security using JWT. JWT tokens are issued by a Keycloak docker container.

Oracle XE database is used to store both the entities (Courses, Teachers, Students and their relationships) as well as
Keycloak realm, roles and users. The message queue functionality is provided via a RabbitMQ docker container.

We also configured a Sonarqube docker container to analyze the code and integrated it with a Jenkins CI container.

First, we need to have an Oracle DB installed and available, if you don't have one follow the instructions on next step,
otherwise skip it. Testing in the same database that runs in production helps find problems that would otherwise only be
found when deploying in production, for instance in this application, class `Phone` does have a field `phoneNumber`
which cannot be named `number`, as it is
an [Oracle reserved keyword](https://docs.oracle.com/cd/A97630_01/appdev.920/a42525/apb.htm).

There are several Cucumber test scenarios for the REST service in `student_registration_and_deletion.feature`.

Some of these endpoints can also be accessed via messaging, and are tested
in `student_registration_and_deletion.feature`
using RabbitMQ testcontainers or Spring Integration API.

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

### Backup volume to filesystem (Linux)

We will link this volume (a folder in your filesystem) to a specific folder within the container. You can check where is
the volume in your filesystem with this command `docker volume inspect oracle18.4.0XE`, you will see something like:

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

Next, stop the docker container with `docker stop ora18xe` and copy the Mountpoint folder and all its contents to a
folder in the project's root named `oracle18.4.0XE`, executing the following command within the project's root folder:
`sudo cp -Rp /var/lib/docker/volumes/oracle18.4.0XE/_data oracle18.4.0XE` (the `-p` will preserve the permissions and
ownership of the files). Now start again the docker container with `docker start ora18xe`.

This will be useful as the project is using [Testcontainers](https://www.testcontainers.org/) to automatically start a
docker container with OracleXE, but we want it to use the data that we currently have instead of going through the slow
initialization every time.

* Run docker image previously built. We will link our port 1521 to the container's port 1521, also link the shared
  directory created in the previous step to the container's /opt/oracle/oradata/ folder.

`docker run -i -t -d --hostname ora18xe --name ora18xe -p 1521:1521 -v oracle18.4.0XE:/opt/oracle/oradata oracle/database:18.4.0-xe`

This will take several minutes (~10 in my laptop) before the DB is ready, meanwhile you can check the logs of the
container.

* Inspect the logs of the docker container and note down the password for SYSTEM user, also the pluggable database
  value (ie. `ora18xe/XEPDB1`).

`docker logs ora18xe -f`

The following message in the logs indicates the database is ready:

```
#########################
DATABASE IS READY TO USE!
#########################
```

### Backup volume to filesystem (Mac)

We are going to run the container pointing the volume to our local filesystem so that when the database starts up, all
the files are copied to our project. This enables us to run the container later pointing the volume to the same folder,
hence speeding up the startup times.

Run the following command from the project's root:

`docker run -i -t -d --hostname ora18xe --name ora18xe -p 1521:1521 -v $(pwd)/oracle18.4.0XE:/opt/oracle/oradata oracle/database:18.4.0-xe`

This will take several minutes (~10 in my laptop) before the DB is ready, meanwhile you can check the logs of the
container.

* Inspect the logs of the docker container and note down the password for SYSTEM user, also the pluggable database
  value (ie. `ora18xe/XEPDB1`).

`docker logs ora18xe -f`

The following message in the logs indicates the database is ready:

```
#########################
DATABASE IS READY TO USE!
#########################
```

Make sure your project now shows the folder `oracle18.4.0XE` as the following:

![oracle_folder.png](src/main/resources/images/oracle_folder.png)

## Configure DataSource

* Connect to Oracle to set up a user. You can
  use [Oracle SQL Developer](https://www.oracle.com/database/technologies/appdev/sqldeveloper-landing.html)
  or using your IDE (IntelliJ Idea Database tab allows you to configure DataSource for many databases).

Use the following values to configure a Datasource to access Oracle as SYSTEM user:

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
alter
session set "_ORACLE_SCRIPT"=true;
create
user testuser identified by testpassword
    quota unlimited on users;
grant connect, resource to testuser;
```

After creating the `testuser`, configure a new DataSource to connect to Oracle XE using it. If you want to use a
different username and password, remember to update accordingly the `application.yml` file.

When you have finished you can stop the container and restart it, let's check that the data has survived restarting the
container:
`docker stop ora18xe`
`docker start ora18xe`

## Test the data source

So far we do not have any tables in OracleXE, but we can leverage JPA to create these for us. We can set
the `spring.jpa.hibernate.ddl-auto` property `create` in the file `application.yml`, and run `Application.java` to
create the tables specified in our JPA mapping automatically.

Other values besides `create` are `none`, `update`, `validate`, `create-drop` (
see [this StackOverflow answer](https://stackoverflow.com/a/42147995/923509)), in production `none` or `validate` are a
safer choice than `create`, `update`.

You can check which tables were created executing the following SQL through the DataSource console:

```sql
SELECT table_name
FROM user_tables
ORDER BY table_name;
```

(Oracle DBs does not support `show tables`).

## Data model

This application is a student registration sample app, where students can register to courses, teachers are assigned to
courses and can score students. The following schema summarizes the relationships between `@Entity` classes in this
project ![image](src/main/resources/images/schema.png)

## RabbitMQ admin console

Inside `docker-compose.yml` file there is a rabbitmq3 service that starts a docker container with a rabbitMQ server
exposing port 15672, reachable with a browser by visiting:

`http://localhost:15672`

## Metrics with Prometheus and Grafana

There are two containers, a Prometheus metrics scraper, and a Grafana one to visualize graphics, that can be started
executing the following command within the project root folder:
`docker-compose up -d`

Next visit Grafana URL `http://localhost:3000` with your browser to configure its Prometheus datasource. Add a
Prometheus datasource, and set HTTP URL to `http://prometheus:9090` and click "Save & Test"
![image](src/main/resources/images/prometheus_datasource.png)

Then, click the `+` on the left menu

![image](src/main/resources/images/grafana_dashboard_import_1.jpg)

and import one dashboard for a Spring boot application whose id is `10280`

![image](src/main/resources/images/grafana_dashboard_import_2.png)

You can also visit Prometheus URL `http://localhost:9090`. Several endpoints are available, for instance
`http://localhost:9090/metrics` shows all the metrics scraped by Prometheus (it should match those listed in the
application URL `http://localhost:8080/actuator/prometheus`).

## Keycloak configuration

Authentication for this project is done via [Keycloak](https://www.keycloak.org/).

Keycloak docker container by default comes with an in-memory database, but we will use our oracleXE database to persist
all the keycloak configuration tables and have them readily available.

First we need to create a docker network:

`docker network create keycloak-network`

Then, from root folder of the project, we start an oracle container pointing to the volume inside folder `oracle_init` (
which will create both the application and keycloak's users inside Oracle, and grant their permissions)
of this project:

```bash
docker run -d --name ora18xeBDD --net keycloak-network \
-p 1521:1521 \
-p 5500:5500 \
-v $(pwd)/oracle_init:/container-entrypoint-initdb.d \
gvenzl/oracle-xe:18.4.0-slim
```

you can then create a keycloak user DataSource in the same way you did for users `SYSTEM` and `testuser` before.

In order to configure Keycloak to use our OracleXE DB, we first need
to [download Oracle JDBC driver](https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html), version 18c,
and copy it inside project folder `keycloak`. Make sure the file is named `ojdbc8.jar`.

Keycloak realm and users configuration for this project was exported to file `keycloak/export/kcdump.json`, and this
file can be used by a keycloak container used to configure everything at once:

```bash
docker run -d -p 8088:8080 --name keycloakimport --net keycloak-network \
-e JAVA_OPTS_APPEND="-Dkeycloak.profile.feature.upload_scripts=enabled -Dkeycloak.migration.action=import -Dkeycloak.migration.provider=singleFile -Dkeycloak.migration.file=/tmp/kcdump.json" \
-e KEYCLOAK_USER=admin \
-e KEYCLOAK_PASSWORD=admin \
-e DB_VENDOR=oracle \
-e DB_ADDR=ora18xeBDD \
-e DB_PORT=1521 \
-e DB_DATABASE=XE \
-e DB_USER=keycloak \
-e DB_PASSWORD=keycloak \
-v $(pwd)/keycloak/export/kcdump.json:/tmp/kcdump.json \
-v $(pwd)/keycloak/ojdbc8.jar:/opt/jboss/keycloak/modules/system/layers/base/com/oracle/jdbc/main/driver/ojdbc.jar \
jboss/keycloak
```

Check with `docker logs keycloakimport -f` that the import was successful, and if so, we
stop `docker stop keycloakimport` and remove this container `docker rm keycloakimport`. This container could be started
again, even if the realm and users have already been imported previously, but any changes done would be lost as the
import process first removes the existing realm before importing it from `kcdump.json` (keycloak logs hint at this
behaviour: `Realm 'springjpaoracle' already exists. Removing it before import`)

Next, we start a new keycloak container that will not import anything with:

```bash
docker run -d -p 8088:8080 --name keycloak --net keycloak-network \
-e KEYCLOAK_USER=admin \
-e KEYCLOAK_PASSWORD=admin \
-e DB_VENDOR=oracle \
-e DB_ADDR=ora18xeBDD \
-e DB_PORT=1521 \
-e DB_DATABASE=XE \
-e DB_USER=keycloak \
-e DB_PASSWORD=keycloak \
-v $(pwd)/keycloak/ojdbc8.jar:/opt/jboss/keycloak/modules/system/layers/base/com/oracle/jdbc/main/driver/ojdbc.jar \
jboss/keycloak
```

Have a look at your local Keycloak visiting `http://localhost:8088` with username `admin` and password `admin` as
configured above. Keycloak should have 5 users (Springjpaoracle realm --> Manage --> Users, and click on **View all
users**):
`nickfury` (with `admin` role), `hulk` (with `teacher`role), and `spidermand`, `antman` and `deadpool` all
with `student` role. All five users have the same `test1` password.

The `kcdump.json` full export (including users), might be useful in case that you want to migrate to another database,
It was done creating a temporary docker container like follows:

```bash
docker run -d -p 8088:8080 --name keycloakexport --net keycloak-network \
-e JAVA_OPTS_APPEND="-Dkeycloak.profile.feature.upload_scripts=enabled -Dkeycloak.migration.action=export -Dkeycloak.migration.provider=singleFile -Dkeycloak.migration.file=/tmp/kcdump.json" \
-e KEYCLOAK_USER=admin \
-e KEYCLOAK_PASSWORD=admin \
-e DB_VENDOR=oracle \
-e DB_ADDR=ora18xeBDD \
-e DB_PORT=1521 \
-e DB_DATABASE=XE \
-e DB_USER=keycloak \
-e DB_PASSWORD=keycloak \
-v $(pwd)/keycloak/ojdbc8.jar:/opt/jboss/keycloak/modules/system/layers/base/com/oracle/jdbc/main/driver/ojdbc.jar \
jboss/keycloak
```

and then copying the `kcdump.json` file out of the **keycloakexport** container like this:
`sudo docker cp keycloakexport:/tmp/kcdump.json keycloak/export/`

## Sonarqube

We first create a docker network:

```bash
docker network create devops
```

In order to analyze this project code using Sonarqube, run Sonarqube Docker container from the project's root folder
like this:

```bash
docker run -d --name sonarqube \
    -p 9000:9000 \
    --network devops \
    -v $(pwd)/sonarqube:/opt/sonarqube/data \
    sonarqube:8.9.3-community
```

and visit `localhost:9000` with your browser. Log in with username `admin` and password `admin`, and you will be
prompted to change the password. Then **add a project** manually and give it a project key, for
instance `spring-jpa-oracle`. Then you will have to generate a token (we named it `springjpatoken`)
and run an analysis on the project (select `Gradle`). We already added the sonarqube plugin to build.gradle file.
Sonarqube will give you a command like the following command one to run an analysis (the `-Dsonar.login=` value will be
different for you, also the `-Dsonar.projectKey=` if you didn't use the same one):

```bash
./gradlew sonarqube \
  -Dsonar.projectKey=spring-jpa-oracle \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=49e208586ac4e1e9ae80674bcba0b7f095516930
```

Edit `build.gradle` file to so that the sonarqube properties have the appropiate values:

```groovy
sonarqube {
    properties {
        property "sonar.sources", "src/test/resources/features,src/main/java"
        property 'sonar.host.url', 'http://localhost:9000'
        property 'sonar.projectKey', 'spring-jpa-oracle'
        property 'sonar.login', '49e208586ac4e1e9ae80674bcba0b7f095516930'
    }
}
```

You can start a Sonarqube analysis with the following command, which assumes that username `admin` has
password `sonarqube`:

```bash
./gradlew -Dsonar.host.url=http://localhost:9000 \
-Dsonar.login=admin \
-Dsonar.password=sonarqube \
-Dsonar.projectKey=spring-jpa-oracle \
-Dsonar.binaries=build/classes \
sonarqube
```

## Jenkins

We followed [these instructions](https://www.jenkins.io/doc/book/installing/docker/):

We need to run a "Docker in docker" image:

```bash
docker run \
  --name jenkins-docker \
  --detach \
  --privileged \
  --network devops \
  --network-alias docker \
  --env DOCKER_TLS_CERTDIR=/certs \
  --volume jenkins-docker-certs:/certs/client \
  --volume jenkins-data:/var/jenkins_home \
  --publish 2376:2376 \
  docker:dind \
  --storage-driver overlay2
```

you don't need to create a Dockerfile, we already added it inside jenkins folder in the root of the project, but you
have to build an image, change directory inside `jenkins` folder and run `docker build -t myjenkins-blueocean:1.1 .`

And finally run your myjenkins-blueocean:1.1 image with:

```bash
docker run --name jenkins-blueocean \
  --detach \
  --network devops \
  --env DOCKER_HOST=tcp://docker:2376 \
  --env DOCKER_CERT_PATH=/certs/client \
  --env DOCKER_TLS_VERIFY=1 \
  --volume jenkins-data:/var/jenkins_home \
  --volume jenkins-docker-certs:/certs/client:ro \
  --publish 8086:8080 \
  --publish 50000:50000 \
  myjenkins-blueocean:1.1
```

You can now visit `localhost:8086` with your browser. The first time you login into jenkins, you will need a password
that you can retrieve from the docker logs:

`docker logs jenkins-blueocean -f`

Next, you will be prompted to create a new admin user. Finally, now that both Sonarqube and Jenkins containers are up,
and the project has been analyzed with Sonarqube, we have to configure a WebHook in Sonarqube to report the QualityGate
outcome to Jenkins. Go to `spring-jpa-oracle` project in Sonarqube, and select Project Settings -> WebHooks

![image](src/main/resources/images/webhook.png)

and set the URL to jenkins (as they run in docker, we use the name we configured in docker)

![image](src/main/resources/images/webhook2.png)

Next we will configure Jenkins to read the configuration of the project's Pipeline directly from the Jenkinsfile in
GitHub. In Jenkins (visit http://localhost:8086), configure a `New Item` of type `Pipeline`. Under the Pipeline section,
select `Pipeline from SCM`, choose `Git` as SCM and past the project's GitHub
URL `https://github.com/guillempg/spring-boot-oracleXE.git`. Finally, set the branch to build to be `*/main`, and the
Script Path to point to the Jenkinsfile from the project's root folder (simply `Jenkinsfile`, as this file is in the top
folder of the project). When finished, "Save" and then click "Build now". This pipeline could be configured to check
GitHub's repository for changes regularly and trigger a build, check

