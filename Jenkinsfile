def genericSh(cmd) {
  if (Boolean.valueOf(env.UNIX)) {
    return sh(script: cmd, returnStdout: true).trim()
  }
  else {
    return bat(script: cmd, returnStdout: true).trim().readLines().drop(1).join(" ")
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

def genericSetVariable(var, val) {
  if (Boolean.valueOf(env.UNIX)) {
    genericSh "${var}=${val}"
  }
  else {
    genericSh "set ${var}=${val}"
  }
}

def genericCopy(src, dst) {
  if (Boolean.valueOf(env.UNIX)) {
    genericSh "cp -f \"${src}\" \"${dst}\""
  }
  else {
    genericSh "echo F|xcopy /v/y \"${src}\" \"${dst}\""
  }
}

def getHome() {
  if (Boolean.valueOf(env.UNIX)) {
    return "~"
  }
  else {
    return "%HOMEDRIVE%%HOMEPATH%"
  }
}

def copyGoogleServicesSecret() {
  withCredentials([file(credentialsId: 'google-services-json', variable: 'GOOGLE_SERVICES_JSON')]) {
    genericCopy(genericVariable('GOOGLE_SERVICES_JSON'), "./app/google-services.json")
  }
}

def loadAppProperties() {
  script {
    String content = readFile("./gradle.properties")
    Properties properties = new Properties()
    properties.load(new StringReader(content))
    env.M_VERSION_NAME = properties.mVersionName
    env.M_APP_NAME = properties.mAppName
    env.M_GITHUB_REPO_OWNER = properties.mGithubRepoOwner
    env.M_GITHUB_REPO = properties.mGithubRepo
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
        loadAppProperties()
        script {
          if (!env.M_VERSION_NAME ||
              !env.M_APP_NAME ||
              !env.M_GITHUB_REPO_OWNER ||
              !env.M_GITHUB_REPO ||
              !env.M_SONAR_PROJECT_KEY) {
            error "Failure: Environment variables not properly set."
          }
        }
        echo "Building ${M_APP_NAME}:${M_VERSION_NAME} from repository https://github.com/${M_GITHUB_REPO_OWNER}/${M_GITHUB_REPO}."
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
        genericSh './gradlew test'
      }
    }

//     stage('Android Test') {
//       when {
//         anyOf {
//           branch 'main'
//           branch 'develop'
//         }
//       }
//       steps {
//         echo 'Running instrumented tests...'
//         genericSh './gradlew connectedAndroidTest'
//       }
//     }
//
//     stage('SonarQube Analysis') {
//       when {
//         anyOf {
//           branch 'main'
//           branch 'develop'
//         }
//       }
//       steps {
//         echo 'Running SonarQube analysis...'
//         script {
//           scannerHome = tool 'qube-scanner'
//         }
//         withSonarQubeEnv(installationName: 'server-sonar') {
//           genericSh "\"${scannerHome}/bin/sonar-scanner\" -Dsonar.projectKey=${M_SONAR_PROJECT_KEY}"
//         }
//       }
//     }
//
//     stage('Quality Gate') {
//       when {
//         anyOf {
//           branch 'main'
//           branch 'develop'
//         }
//       }
//       steps {
//         echo 'Waiting for quality gate...'
//         timeout(time: 4, unit: 'MINUTES') {
//           waitForQualityGate abortPipeline: true
//         }
//       }
//     }

    stage('Publish') {
      when {
        branch 'main'
      }
      steps {
        echo 'Publishing release...'
        script {
          // check if release already exists
          boolean tagExists = genericSh("git tag -l \"${M_VERSION_NAME}\"")?.trim()
          if (tagExists) {
            error "Release already exists, aborting."
          }
          // sign apk
          echo "Building and signing app..."
          withCredentials([
            file(credentialsId: 'android-sign-keystore', variable: 'MY_STORE_FILE'),
            string(credentialsId: 'android-sign-keystore-password', variable: 'MY_STORE_PASSWORD'),
            usernamePassword(credentialsId: 'android-signing-key', usernameVariable: 'MY_KEY_ALIAS', passwordVariable: 'MY_KEY_PASSWORD')
          ]) {
            genericSh './gradlew assembleRelease'
          }
          def apkPath = "./app/build/outputs/apk/release/app-release.apk"

          // create tag
          def credential = genericVariable("GITHUB_PUBLISH_TOKEN")
          def isPrerelease = "${M_VERSION_NAME}".split('\\.')[0] == '0';
          def releaseUrl = "https://api.github.com/repos/${M_GITHUB_REPO_OWNER}/${M_GITHUB_REPO}/releases"
          def postTagRequest = "curl -s -X POST -H \"Accept: application/vnd.github+json\" -H \"Authorization: Bearer ${credential}\""
          postTagRequest += " -d \"{\\\"tag_name\\\":\\\"${M_VERSION_NAME}\\\",\\\"target_commitish\\\":\\\"main\\\",\\\"name\\\":\\\"${M_VERSION_NAME}\\\",\\\"body\\\":\\\"Release ${M_VERSION_NAME}\\\",\\\"draft\\\":false,\\\"prerelease\\\":${isPrerelease},\\\"generate_release_notes\\\":false}\" ${releaseUrl}"
          echo "Requesting to create release tag: ${postTagRequest}"

          // create upload url from response
          def postTagResponseText = ""
          withCredentials([string(credentialsId: 'github-publish-token', variable: 'GITHUB_PUBLISH_TOKEN')]) {
            postTagResponseText = genericSh postTagRequest
          }
          echo "Got create tag response: ${postTagResponseText}"
          def postTagResponse = readJSON(text: postTagResponseText)
          def uploadUrl = postTagResponse.upload_url.split('\\{')[0]
          uploadUrl += "?name=${M_APP_NAME}-${M_VERSION_NAME}.apk"

          // upload asset
          def credential2 = genericVariable("GITHUB_PUBLISH_TOKEN2")
          def signedApkPath = './app/build/outputs/apk/release/app-release-unsigned.apk'
          def uploadAssetRequest = "curl -s -X POST -H \"Accept: application/vnd.github+json\" -H \"Authorization: Bearer ${credential2}\""
          uploadAssetRequest += " -H \"Content-Type: application/vnd.android.package-archive\" -d @\"${apkPath}\" ${uploadUrl}"
          echo "Requesting to upload release asset: ${uploadAssetRequest}"

          // get upload response
          def uploadAssetResponseText = ""
          withCredentials([string(credentialsId: 'github-publish-token', variable: 'GITHUB_PUBLISH_TOKEN2')]) {
            uploadAssetResponseText = genericSh uploadAssetRequest
          }
          echo "Got upload release response: ${uploadAssetResponseText}"
          def uploadAssetResponse = readJSON(text: uploadAssetResponseText)

          // checks if it was uploaded
          if (uploadAssetResponse.state != "uploaded") {
            error "Release file could not be uploaded. Upload response: ${uploadAssetResponseText}"
          }

          echo "Release published"
        }
      }
    }
  }

  post {
    always {
      cleanWs()
    }
  }
}
