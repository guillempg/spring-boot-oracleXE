#!/bin/sh

access_token=$(curl -s --location --request POST 'http://127.0.0.1:8088/auth/realms/master/protocol/openid-connect/token' --header 'Content-Type: application/x-www-form-urlencoded' --data-urlencode 'client_id=admin-cli' --data-urlencode 'username=admin' --data-urlencode 'password=admin' --data-urlencode 'grant_type=password' | jq '.access_token' | tr -d '"')

echo "access token:$access_token"

admin_id=$(curl -siN 'http://127.0.0.1:8088/auth/admin/realms/springjpaoracle/users' --header 'Content-Type: application/json' --header "Authorization: Bearer $access_token" --data-raw '{
        "username": "nickfury",
        "email": "nickfury@marvel.com",
        "enabled": true,
        "emailVerified": true,
        "firstName": "Nick",
        "lastName": "Fury",
        "access": {
            "manageGroupMembership": true,
            "view": true,
            "mapRoles": true,
            "impersonate": true,
            "manage": true
        }
    }' | awk 'BEGIN {FS=": "}/^Location/{print $2}' | awk -F '/' '{print $9}' | sed 's/\r//g')
echo "Nick Fury created as admin with id: $admin_id"
url1="http://127.0.0.1:8088/auth/admin/realms/springjpaoracle/users/$admin_id/reset-password"

curl --location --request PUT $url1 \
--header "Authorization: Bearer $access_token" \
--header 'Content-Type: application/json' \
--data-raw '{"type":"password","value":"test1","temporary":false}'

url1="http://127.0.0.1:8088/auth/admin/realms/springjpaoracle/users/$admin_id/role-mappings/realm"

curl --location --request POST $url1 \
--header 'Content-Type: application/json' \
--header "Authorization: Bearer $access_token" \
--data-raw '[    {
        "id": "a4c0e128-8781-4b1c-85e1-86040ac60627",
        "name": "app-admin",
        "composite": true,
        "clientRole": false,
        "containerId": "springjpaoracle"
    }]'




teacher_id=$(curl -siN 'http://127.0.0.1:8088/auth/admin/realms/springjpaoracle/users' --header 'Content-Type: application/json' --header "Authorization: Bearer $access_token" --data-raw '{
        "username": "hulk",
        "email": "brucebanner@marvel.com",
        "enabled": true,
        "emailVerified": true,
        "firstName": "Bruce",
        "lastName": "Banner",
        "access": {
            "manageGroupMembership": true,
            "view": true,
            "mapRoles": true,
            "impersonate": true,
            "manage": true
        }
    }' | awk 'BEGIN {FS=": "}/^Location/{print $2}' | awk -F '/' '{print $9}'| sed 's/\r//g')
echo "Hulk created as teacher with id: $teacher_id"

url1="http://127.0.0.1:8088/auth/admin/realms/springjpaoracle/users/$teacher_id/reset-password"

curl --location --request PUT $url1 \
--header "Authorization: Bearer $access_token" \
--header 'Content-Type: application/json' \
--data-raw '{"type":"password","value":"test1","temporary":false}'

url1="http://127.0.0.1:8088/auth/admin/realms/springjpaoracle/users/$teacher_id/role-mappings/realm"

curl --location --request POST $url1 \
--header 'Content-Type: application/json' \
--header "Authorization: Bearer $access_token" \
--data-raw '[    {
        "id": "207bfd96-95d7-41ae-9417-01eb323a4ba6",
        "name": "app-teacher",
        "composite": true,
        "clientRole": false,
        "containerId": "springjpaoracle"
    }]'


student_id1=$(curl -siN 'http://127.0.0.1:8088/auth/admin/realms/springjpaoracle/users' --header 'Content-Type: application/json' --header "Authorization: Bearer $access_token" --data-raw '{
        "username": "spiderman",
        "email": "peterparker@marvel.com",
        "enabled": true,
        "emailVerified": true,
        "firstName": "Peter",
        "lastName": "Parker",
        "access": {
            "manageGroupMembership": true,
            "view": true,
            "mapRoles": true,
            "impersonate": true,
            "manage": true
        }
    }' | awk 'BEGIN {FS=": "}/^Location/{print $2}' | awk -F '/' '{print $9}'| sed 's/\r//g')
echo "Spiderman created as student with id: $student_id1"

url1="http://127.0.0.1:8088/auth/admin/realms/springjpaoracle/users/$student_id1/reset-password"

curl --location --request PUT $url1 \
--header "Authorization: Bearer $access_token" \
--header 'Content-Type: application/json' \
--data-raw '{"type":"password","value":"test1","temporary":false}'

url1="http://127.0.0.1:8088/auth/admin/realms/springjpaoracle/users/$student_id1/role-mappings/realm"

curl --location --request POST $url1 \
--header 'Content-Type: application/json' \
--header "Authorization: Bearer $access_token" \
--data-raw '[    {
        "id": "e1173071-c0c3-436c-ab2f-78654858288a",
        "name": "app-student",
        "composite": true,
        "clientRole": false,
        "containerId": "springjpaoracle"
    }]'


student_id2=$(curl -siN 'http://127.0.0.1:8088/auth/admin/realms/springjpaoracle/users' --header 'Content-Type: application/json' --header "Authorization: Bearer $access_token" --data-raw '{
        "username": "antman",
        "email": "scottlang@marvel.com",
        "enabled": true,
        "emailVerified": true,
        "firstName": "Scott",
        "lastName": "Lang",
        "access": {
            "manageGroupMembership": true,
            "view": true,
            "mapRoles": true,
            "impersonate": true,
            "manage": true
        }
    }' | awk 'BEGIN {FS=": "}/^Location/{print $2}' | awk -F '/' '{print $9}'| sed 's/\r//g')
echo "Ant-man created as student with id: $student_id2"

url1="http://127.0.0.1:8088/auth/admin/realms/springjpaoracle/users/$student_id2/reset-password"

curl --location --request PUT $url1 \
--header "Authorization: Bearer $access_token" \
--header 'Content-Type: application/json' \
--data-raw '{"type":"password","value":"test1","temporary":false}'

url1="http://127.0.0.1:8088/auth/admin/realms/springjpaoracle/users/$student_id2/role-mappings/realm"

curl --location --request POST $url1 \
--header 'Content-Type: application/json' \
--header "Authorization: Bearer $access_token" \
--data-raw '[    {
        "id": "e1173071-c0c3-436c-ab2f-78654858288a",
        "name": "app-student",
        "composite": true,
        "clientRole": false,
        "containerId": "springjpaoracle"
    }]'