#!/bin/sh

access_token=$(curl -s --location --request POST 'http://127.0.0.1:8088/auth/realms/master/protocol/openid-connect/token' --header 'Content-Type: application/x-www-form-urlencoded' --data-urlencode 'client_id=admin-cli' --data-urlencode 'username=admin' --data-urlencode 'password=admin' --data-urlencode 'grant_type=password' | jq '.access_token' | tr -d '"')

echo "access token:$access_token"

admin_id=$(curl -siN 'http://127.0.0.1:8088/auth/admin/realms/springjpaoracle/users' --header 'Content-Type: application/json' --header "Authorization: Bearer $access_token" --data-raw '{
        "username": "nickfury",
        "email": "nickfury@marvel.com",
        "enabled": true,
        "credentials": [{"type":"password","value":"test1","temporary":false}],
        "groups": ["admin"],
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

teacher_id=$(curl -siN 'http://127.0.0.1:8088/auth/admin/realms/springjpaoracle/users' --header 'Content-Type: application/json' --header "Authorization: Bearer $access_token" --data-raw '{
        "username": "hulk",
        "email": "brucebanner@marvel.com",
        "enabled": true,
        "credentials": [{"type":"password","value":"test1","temporary":false}],
        "groups": ["teachers"],
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

student_id1=$(curl -siN 'http://127.0.0.1:8088/auth/admin/realms/springjpaoracle/users' --header 'Content-Type: application/json' --header "Authorization: Bearer $access_token" --data-raw '{
        "username": "spiderman",
        "email": "peterparker@marvel.com",
        "enabled": true,
        "credentials": [{"type":"password","value":"test1","temporary":false}],
        "groups": ["students"],
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

student_id2=$(curl -siN 'http://127.0.0.1:8088/auth/admin/realms/springjpaoracle/users' --header 'Content-Type: application/json' --header "Authorization: Bearer $access_token" --data-raw '{
        "username": "antman",
        "email": "scottlang@marvel.com",
        "enabled": true,
        "credentials": [{"type":"password","value":"test1","temporary":false}],
        "groups": ["students"],
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

student_id3=$(curl -siN 'http://127.0.0.1:8088/auth/admin/realms/springjpaoracle/users' --header 'Content-Type: application/json' --header "Authorization: Bearer $access_token" --data-raw '{
        "username": "deadpool",
        "email": "wadewinston@marvel.com",
        "enabled": true,
        "credentials": [{"type":"password","value":"test1","temporary":false}],
        "groups": ["students"],
        "emailVerified": true,
        "firstName": "Wade",
        "lastName": "Winston",
        "access": {
            "manageGroupMembership": true,
            "view": true,
            "mapRoles": true,
            "impersonate": true,
            "manage": true
        }
    }' | awk 'BEGIN {FS=": "}/^Location/{print $2}' | awk -F '/' '{print $9}'| sed 's/\r//g')
echo "Deadpool created as student with id: $student_id3"
