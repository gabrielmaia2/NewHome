def genericSh(cmd) {
  if (Boolean.valueOf(env.UNIX)) {
    sh cmd
  }
  else {
    bat cmd
  }
}

def copyGoogleServicesSecret() {
  withCredentials([file(credentialsId: 'google-services-json', variable: 'google-services-json')]) {
    if (Boolean.valueOf(env.UNIX)) {
      genericSh "cp -f \$google-services-json ./app/google-services.json"
    }
    else {
      genericSh "copy /b/v/y \"%google-services-json%\" .\\app\\google-services.json"
    }
  }
}

pipeline {
  agent any

  environment {
    UNIX = isUnix()
  }
  
  stages {
    stage('Build') {
      when {
        anyOf {
          branch "main"
          branch "develop"
        }
      }
      steps {
        echo 'Building...'
        copyGoogleServicesSecret()
        genericSh './gradlew assemble'
      }
    }

    stage('Test') {
      when {
        anyOf {
          branch "main"
          branch "develop"
        }
      }
      steps {
        echo 'Testing...'
        copyGoogleServicesSecret()
        genericSh './gradlew test'
      }
    }

    stage('SonarQube Analysis') {
      when {
        branch "main"
      }
      steps {
        withSonarQubeEnv(credentialsId: 'sonarqube-token') {
          sh "${tool('qube-scanner')}/bin/sonar-scanner"
        }
      }
    }
    
    stage("Quality Gate") {
      when {
        branch "main"
      }
      steps {
        timeout(time: 4, unit: 'MINUTES') {
          waitForQualityGate(abortPipeline: true, credentialsId: 'sonarqube-token', webhookSecretId: 'sonarqube-webhook-token')
        }
      }
    }

    stage('Deploy') {
      when {
        branch "main"
      }
      steps {
        echo 'Deploying...'
        copyGoogleServicesSecret()
        genericSh './gradlew clean'
        genericSh './gradlew assembleRelease'
      }
    }
  }
}
