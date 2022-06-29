#!groovy

timestamps {
    properties([parameters([choice(choices: ['Yes', 'No'], description: 'Retry pipeline if fails at any stage.', name: 'Retry')])])
node {
    try {
        
        stage('Clone') {
            git branch: 'main', url: 'https://github.com/avinash0106/retry_failedbuild.git'
        }
        
        stage('Build') {
            sh 'mvn clean install -DskipTests'
        }

        stage('Unit Test') {
            sh 'mvn test'
        }

        stage('Integration Test') {
            sh 'mvn verify -DskipUnitTests -Parq-wildfly-swarm'
        }

        stage('SonarQube Analysis') {
            withSonarQubeEnv(credentialsId: 'sonarscanner') {
                sh 'mvn sonar:sonar'
            }
        }
        
        stage('Archive Artifacts') {
            archiveArtifacts allowEmptyArchive: false, artifacts: 'target//*.war', caseSensitive: true, defaultExcludes: true, fingerprint: false, onlyIfSuccessful: false
        }
        
        stage('Clean WS') {
            cleanWs()
        }
        
    } catch (err) {
        echo 'Pipeline failed'
        currentBuild.result = 'FAILURE'
    }
    
    if ("${params.Retry}" == "Yes" && currentBuild.result == 'FAILURE') {
        echo "Aborting the build due to failed Retry!"
        build.getExecutor().interrupt(Result.ABORTED)
    }
    
    if (currentBuild.result == 'FAILURE') {
        echo "Retrying Job!"
        build quietPeriod: 300, job: "${JOB_NAME}"
       }
    }
}

