def SERVICES = [
    [name: 'product-service', deployment: 'deployment/product-service'],
    [name: 'order-service',   deployment: 'deployment/order-service'],
    [name: 'api-gateway',     deployment: 'deployment/api-gateway']
]

pipeline {
    agent any

    environment {
        DOCKER_REPO         = "azizbenabdallah/ecommerceback"
        DOCKER_USER         = "azizbenabdallah"
        DOCKER_PASS         = "jc-i5jxUL\$H36N4"
        DOCKER_IMAGE_TAG    = "${env.BUILD_NUMBER}"
        MVN_OPTS            = "-T 1C -B -ntp -Dmaven.repo.local=${WORKSPACE}/.m2/repository"
        K8S_ROLLOUT_TIMEOUT = "180s"
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        skipStagesAfterUnstable()
    }

    stages {

        stage('Checkout') {
            steps {
                deleteDir()
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/AzizBack']],
                    extensions: [
                        [$class: 'CloneOption',
                            depth: 1,
                            shallow: true,
                            noTags: true,
                            timeout: 20
                        ]
                    ],
                    userRemoteConfigs: [[
                        url: 'https://github.com/yazidebouthouri28/Esprit-PI-4SE3-2025-2026-ConnectCamp-Backend.git'
                    ]]
                ])
            }
        }

        stage('Build & Package') {
            steps {
                script {
                    parallel SERVICES.collectEntries { svc ->
                        def service = svc
                        ["Maven › ${service.name}": {
                            timeout(time: 10, unit: 'MINUTES') {
                                dir(service.name) {
                                    sh 'chmod +x mvnw'
                                    sh "./mvnw clean package -DskipTests ${MVN_OPTS}"
                                }
                            }
                        }]
                    }
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    sh "docker login -u ${DOCKER_USER} -p '${DOCKER_PASS}'"

                    parallel SERVICES.collectEntries { svc ->
                        def service = svc
                        ["Docker › ${service.name}": {
                            timeout(time: 10, unit: 'MINUTES') {
                                sh """
                                    docker build \
                                        -t ${DOCKER_REPO}-${service.name}:${BUILD_NUMBER} \
                                        -t ${DOCKER_REPO}-${service.name}:latest \
                                        ./${service.name}
                                """
                                retry(3) {
                                    sh """
                                        docker push ${DOCKER_REPO}-${service.name}:${BUILD_NUMBER}
                                        docker push ${DOCKER_REPO}-${service.name}:latest
                                    """
                                }
                            }
                        }]
                    }

                    sh 'docker logout || true'
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    sh 'kubectl apply -f k8s/'

                    parallel SERVICES.collectEntries { svc ->
                        def service = svc
                        ["Rollout › ${service.name}": {
                            timeout(time: 5, unit: 'MINUTES') {
                                try {
                                    sh "kubectl rollout status ${service.deployment} --timeout=${K8S_ROLLOUT_TIMEOUT}"
                                } catch (err) {
                                    echo "⚠️ Rollout échoué pour ${service.name}"
                                    sh "kubectl rollout undo ${service.deployment}"
                                    error("Rollback déclenché : ${service.name}")
                                }
                            }
                        }]
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
            cleanWs()
        }
        success {
            echo "✅ Build ${BUILD_NUMBER} terminé avec succès"
        }
        failure {
            echo "❌ Build ${BUILD_NUMBER} échoué"
        }
    }
}