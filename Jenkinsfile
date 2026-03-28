pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'farah893/connectcamp-backend'
        DOCKER_CREDENTIALS = 'dockerhub-credentials'
    }

    tools {
        maven 'Maven 3.9'
        jdk 'JDK 17'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Package') {
            steps {
                dir('backConnect') {
                    sh 'mvn clean package -DskipTests -B'
                }
            }
        }

        stage('Unit Tests') {
            steps {
                dir('backConnect') {
                    sh 'mvn test -Dtest="SiteServiceTest,EmergencyAlertServiceTest,CampingServiceServiceTest,PackServiceTest" -B'
                }
            }
            post {
                always {
                    junit 'backConnect/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                dir('backConnect') {
                    sh "docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} -t ${DOCKER_IMAGE}:latest ."
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${DOCKER_CREDENTIALS}",
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_TOKEN'
                )]) {
                    sh "echo ${DOCKER_TOKEN} | docker login -u ${DOCKER_USER} --password-stdin"
                    sh "docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}"
                    sh "docker push ${DOCKER_IMAGE}:latest"
                    sh "docker logout"
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline backend réussi — image ${DOCKER_IMAGE}:${BUILD_NUMBER} publiée sur Docker Hub"
        }
        failure {
            echo "Pipeline backend échoué — vérifier les logs ci-dessus"
        }
        always {
            cleanWs()
        }
    }
}
