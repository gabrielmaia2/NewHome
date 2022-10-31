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
      genericSh "copy /b/v/y %google-services-json% .\\app\\google-services.json"
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
      steps {
        echo 'Building...'
        copyGoogleServicesSecret()
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
