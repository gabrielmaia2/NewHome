def genericSh(cmd) {
  if (Boolean.valueOf(env.UNIX)) {
    sh cmd
  }
  else {
    bat cmd
  }
}

def copyGoogleServicesSecret() {
}

pipeline {
  agent any

  environment {
    UNIX = isUnix()
  }
  
  withCredentials([file(credentialsId: 'google-services-json', variable: 'google-services-json')]) {
    genericSh "cp \$google-services-json ./app/google-services.json"
  }
  
  stages {
    stage('Build') {
      steps {
        echo 'Building...'
        genericSh './gradlew clean'
        genericSh './gradlew assemble'
      }
    }

    stage('Test') {
      steps {
        echo 'Testing...'
        genericSh './gradlew test'
      }
    }

    stage('Deploy') {
      steps {
        echo 'Deploying...'
      }
    }
  }
}
