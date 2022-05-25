#!groovy

timestamps {
    properties([parameters([string(defaultValue: 'Retrying', description: 'Check build before rebuild starts?', name: 'Retry')])])
node {
    try {

        stage('Checkout') {
            checkout([$class: 'GitSCM', branches: [[name: '*/main']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '', url: 'https://github.com/avinash0106/retry_failedbuild.git']]])
        }

        stage('Build') {
            bat 'mvn clean install -DskipTests'
        }

        stage('Unit Test') {
            bat 'mvn test'
        }

        stage('Integration Test') {
            bat 'mvn verify -DskipUnitTests -Parq-wildfly-swarm'
        }

        stage('SonarQube Analysis') {
            withSonarQubeEnv(credentialsId: 'sonarscanner') {
                bat 'mvn sonar:sonar'
            }
        }
        stage() {
            archiveArtifacts allowEmptyArchive: false, artifacts: 'target//*.war', caseSensitive: true, defaultExcludes: true, fingerprint: false, onlyIfSuccessful: false
        }
    } catch (err) {
        echo 'Pipeline failed'
        currentBuild.result = 'FAILURE'
    }

    if (currentBuild.result == 'FAILURE') {
        echo "${params.Retry} Job!"
        build quietPeriod: 300, job: 'retry_scripted' 
       }
    }
}

