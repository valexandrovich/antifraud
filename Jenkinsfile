node {
  stage('SCM') {
    checkout scm
  }
//   stage('Set root version') {
//     def versionTag = '1.0.' + currentBuild.number + '-SNAPSHOT'
//     def pomFile = 'pom.xml'
//     def pom = readMavenPom file: pomFile
//     def mvn = tool 'Default Maven'
//
//     pom.version = versionTag
//     writeMavenPom file: pomFile, model: pom
//
//     withMaven() {
//         sh "${mvn}/bin/mvn versions:set -DnewVersion=" + versionTag
//     }
//   }
  stage('SonarQube Analysis') {
    def mvn = tool 'Default Maven'

    withSonarQubeEnv() {
      sh "${mvn}/bin/mvn clean verify sonar:sonar"
    }
  }
}