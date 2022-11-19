def genericSh(cmd) {
  if (Boolean.valueOf(env.UNIX)) {
    sh cmd
  }
  else {
    bat cmd
  }
}

def genericVariable(var) {
  if (Boolean.valueOf(env.UNIX)) {
    return '\$' + var
  }
  else {
    return '%' + var + '%'
  }
}

def copyGoogleServicesSecret() {
  withCredentials([file(credentialsId: 'google-services-json', variable: 'google-services-json')]) {
    if (Boolean.valueOf(env.UNIX)) {
      genericSh 'cp -f ' + genericVariable('google-services-json') + ' ./app/google-services.json'
    }
    else {
      genericSh 'copy /b/v/y "' + genericVariable('google-services-json') + '" .\\app\\google-services.json'
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
          branch 'main'
          branch 'develop'
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
          branch 'main'
          branch 'develop'
        }
      }
      steps {
        echo 'Testing...'
        copyGoogleServicesSecret()
        genericSh './gradlew test'
      }
    }

    stage('SonarQube Analysis') {
      steps {
        echo 'Running SonarQube analysis...'
        withSonarQubeEnv(installationName: 'server-sonar') {
          echo tool("qube-scanner")
          withMaven() {
            genericSh 'mvn clean package sonar:sonar'
          }
          withCredentials([string(credentialsId: 'sonarqube-token', variable: 'sonarqube-token')]) {
            genericSh(tool("qube-scanner") + '/bin/sonar-scanner -Dsonar.login=' + genericVariable('sonarqube-token'))
          }
        }
      }
    }
    
    stage('Quality Gate') {
      steps {
        echo 'Waiting for quality gate...'
        timeout(time: 4, unit: 'MINUTES') {
          waitForQualityGate abortPipeline: true
        }
      }
    }

    stage('Deploy') {
      when {
        branch 'main'
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
