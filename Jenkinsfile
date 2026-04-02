def SERVICES = [
    [name: 'api-gateway',     deployment: 'deployment/api-gateway'],
    [name: 'order-service',   deployment: 'deployment/order-service'],
    [name: 'product-service', deployment: 'deployment/product-service'],
    [name: 'user-service',    deployment: 'deployment/user-service']
]

def REPO_DIR = '/var/lib/jenkins/projet-backend'

pipeline {
    agent any

    environment {
        DOCKER_REPO         = "azizbenabdallah/ecommerceback"
        DOCKER_USER         = "azizbenabdallah"
        DOCKER_PASS         = "jc-i5jxUL\$H36N4"
        DOCKER_IMAGE_TAG    = "${BUILD_NUMBER}"
        MVN_OPTS            = "-T 1C -B -Dmaven.repo.local=/var/lib/jenkins/.m2/repository -Dmaven.wagon.http.retryHandler.count=5 -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 -Dmaven.wagon.http.pool=false"
        K8S_NAMESPACE       = "devops"
        K8S_ROLLOUT_TIMEOUT = "240m"
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 2, unit: 'HOURS')
        skipStagesAfterUnstable()
    }

    stages {

        stage('Checkout') {
            steps {
                dir("${REPO_DIR}") {
                    sh 'git fetch --depth 1 origin AzizBack'
                    sh 'git reset --hard origin/AzizBack'
                }
            }
        }

        stage('Build & Package') {
            steps {
                script {
                    parallel SERVICES.collectEntries { svc ->
                        def service = svc
                        ["Maven › ${service.name}": {
                            timeout(time: 60, unit: 'MINUTES') {
                                dir("${REPO_DIR}/${service.name}") {
                                    sh 'chmod +x mvnw'
                                    retry(3) {
                                        sh "./mvnw clean package -DskipTests ${MVN_OPTS}"
                                    }
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
                            timeout(time: 60, unit: 'MINUTES') {
                                sh """
                                    docker build \
                                        -t ${DOCKER_REPO}-${service.name}:${DOCKER_IMAGE_TAG} \
                                        -t ${DOCKER_REPO}-${service.name}:latest \
                                        ${REPO_DIR}/${service.name}
                                """
                                retry(3) {
                                    sh "docker push ${DOCKER_REPO}-${service.name}:${DOCKER_IMAGE_TAG}"
                                }
                                retry(3) {
                                    sh "docker push ${DOCKER_REPO}-${service.name}:latest"
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
                    sh "kubectl apply -f ${REPO_DIR}/k8s/"

                    parallel SERVICES.collectEntries { svc ->
                        def service = svc
                        ["Rollout › ${service.name}": {
                            timeout(time: 30, unit: 'MINUTES') {
                                try {
                                    sh "kubectl rollout status ${service.deployment} -n ${K8S_NAMESPACE} --timeout=${K8S_ROLLOUT_TIMEOUT}"
                                } catch (err) {
                                    echo "⚠️ Rollout échoué pour ${service.name}"
                                    sh "kubectl rollout undo ${service.deployment} -n ${K8S_NAMESPACE}"
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
            archiveArtifacts artifacts: "${REPO_DIR}/**/target/*.jar", allowEmptyArchive: true
        }
        success {
            echo "✅ Build ${BUILD_NUMBER} terminé → tag: ${DOCKER_IMAGE_TAG}"
        }
        failure {
            echo "❌ Build ${BUILD_NUMBER} échoué"
        }
    }
}