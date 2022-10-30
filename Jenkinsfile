def genericSh(cmd) {
  if (Boolean.valueOf(env.UNIX)) {
    sh cmd
  }
  else {
    bat cmd
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
