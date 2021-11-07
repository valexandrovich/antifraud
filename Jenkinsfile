node {
  stage('SCM') {
    checkout scm
  }
//   stage('Set root version') {
//     def now = new Date()
//     w = date.getAt(Calendar.WEEK_OF_YEAR)
//     printf("Week: %s", w)

//     def versionTag = '1.0.0-SNAPSHOT'
//     def pomFile = 'pom.xml'
//     def pom = readMavenPom file: pomFile
//     pom.version = versionTag
//     writeMavenPom file: pomFile, model: pom
//   }
//   stage('Set modules version') {
//     pom = readMavenPom file: 'pom.xml'
//     printf("Version: %s", pom.version)
//
//     printf ("Modules: %s", pom.getModules().join(","))
//
//     version = getVersion(pom)
//
//     withMaven {
//         sh "mvn versions:set -DnewVersion=${version}"
//     }
//
//     printf("Version set to: %s", version)
//   }
  stage('SonarQube Analysis') {
    def mvn = tool 'Default Maven';
    withSonarQubeEnv() {
      sh "${mvn}/bin/mvn clean verify sonar:sonar"
    }
  }
}