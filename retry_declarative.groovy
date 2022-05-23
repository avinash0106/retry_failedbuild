#!groovy

pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                mvn 'clean install -DskipTests'
            }
        }

        stage('Unit Test') {
            steps {
                mvn 'test'
            }
        }

        stage('Integration Test') {
            steps {
                mvn 'verify -DskipUnitTests -Parq-wildfly-swarm '
            }
        }
    }

    post {
        always {
            // Archive Unit and integration test results, if any
            junit allowEmptyResults: true,
                    testResults: '**/target/surefire-reports/TEST-*.xml, **/target/failsafe-reports/*.xml'
        }

        changed {
            mail to: "${env.EMAIL_RECIPIENTS}",
                 subject: "${JOB_NAME} - Build #${BUILD_NUMBER} - ${currentBuild.currentResult}!",
                 body: "Check console output at ${BUILD_URL} to view the results."
        }
    }
}