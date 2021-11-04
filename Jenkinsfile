pipeline {
    agent any
    environment {
        ORACLE_XE_IMAGE='gvenzl/oracle-xe:18.4.0'
    }
    tools {
        gradle 'gradle-7.2'
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