node {
  stage('Make sure that Test Service stopped') {
    sh "/usr/bin/sudo /bin/systemctl stop otp"
  }

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
    sh "cd liquibase && ${mvn}/bin/mvn liquibase:update && cd .."
  }

  stage('Start Test Service') {
    sh "/usr/bin/sudo /bin/systemctl start otp"
  }

  stage('Run E2E Tests') {
    dir('e2e') {
      git branch: 'master',
          credentialsId: '6b229787-40c7-4810-b6c3-4b337de89180',
          url: 'https://drSolidity@bitbucket.org/solidityprojects/otp_qa_mvn.git'
      def mvn = tool 'Default Maven';
      sh "${mvn}/bin/mvn clean test -Dotp.url=http://localhost:2022"
    }
  }

  stage('Stop Test Service') {
      sh "/usr/bin/sudo /bin/systemctl stop otp"
  }

  stage('Reports') {
      allure([
          includeProperties: false,
          jdk: '',
          properties: [],
          reportBuildPolicy: 'ALWAYS',
          results: [[path: 'e2e/target/allure-results']]
      ])
  }
}