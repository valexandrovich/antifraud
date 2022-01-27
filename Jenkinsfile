node {
  stage('SCM') {
    checkout scm
  }

  stage('SonarQube Analysis') {
    def mvn = tool 'Default Maven'

    withSonarQubeEnv() {
      sh "${mvn}/bin/mvn clean verify sonar:sonar"
    }
  }

  stage('Migrate Database') {
    def mvn = tool 'Default Maven';
    sh "cd liquibase && ${mvn}/bin/mvn liquibase:update"
  }
}