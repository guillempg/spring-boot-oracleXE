pipeline {
  agent any

  environment {
        ORACLE_XE_IMAGE='gvenzl/oracle-xe:18.4.0'
  }
  stages {
    stage('build') {
      steps {
        sh './gradlew build'
      }
    }
    stage('cucumber') {
      steps {
          withCredentials([usernamePassword(credentialsId: '6c838ddf-11ab-445f-baef-e59037697f55', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
            sh 'docker login -u $USERNAME -p $PASSWORD'
            sh './gradlew cucumber'
        }

      }
    }
  }
}