pipeline {
    agent any
    environment {
        ORACLE_XE_IMAGE='gvenzl/oracle-xe:18.4.0'
    }
    tools {
        gradle 'gradle-7.2'
        maven 'mvn'
    }
    stages {
        stage('Checkout from GitHub'){
            steps{
                git branch: 'main',
                url: 'https://github.com/guillempg/spring-boot-oracleXE.git'
            }
        }
        stage('Compile'){
            steps{
                sh './gradlew compileJava'
                sh './gradlew compileTestJava'
            }
        }
        stage("Copy OJDB driver"){
            steps {
                sh 'mvn org.apache.maven.plugins:maven-dependency-plugin:2.4:get -Dartifact=com.oracle.database.jdbc:ojdbc8:21.3.0.0 -Ddest=keycloak/ojdbc8.jar'
            }
        }
        stage('JUnit test'){
            steps{
                sh './gradlew test'
            }
        }
        stage('Slow JUnit test'){
            steps{
                sh './gradlew slowJUnit'
            }
        }
        stage('Cucumber tests'){
            steps{
                sh './gradlew cucumber'
            }
        }
    }
}