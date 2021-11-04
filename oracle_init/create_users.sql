alter
session set "_ORACLE_SCRIPT"=true;
create
user testuser identified by testpassword
    quota unlimited on users;
grant connect, resource to testuser;
create
user keycloak identified by keycloak
    quota unlimited on users;
grant connect, resource to keycloak;