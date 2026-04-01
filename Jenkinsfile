// ============================================================
//  Configuration centrale
// ============================================================
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
        DOCKER_IMAGE_TAG    = "${env.BRANCH_NAME ?: 'local'}-${BUILD_NUMBER}"
        GIT_SHORT_COMMIT    = "${env.GIT_COMMIT?.take(7) ?: 'unknown'}"
        MVN_OPTS            = "-T 1C -B -ntp -Dmaven.repo.local=${WORKSPACE}/.m2/repository"
        K8S_ROLLOUT_TIMEOUT = "180s"
    }

    options {
        timestamps()
        ansiColor('xterm')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        skipStagesAfterUnstable()
    }

    stages {

        stage('Checkout') {
            steps { checkout scm }
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
                    // ✅ Login en clair
                    sh "docker login -u ${DOCKER_USER} -p '${DOCKER_PASS}'"

                    parallel SERVICES.collectEntries { svc ->
                        def service = svc
                        ["Docker › ${service.name}": {
                            timeout(time: 10, unit: 'MINUTES') {
                                sh """
                                    docker build \
                                        --cache-from ${DOCKER_REPO}-${service.name}:latest \
                                        --build-arg BUILD_NUMBER=${BUILD_NUMBER} \
                                        --build-arg BRANCH=${env.BRANCH_NAME ?: 'local'} \
                                        --label git-commit=${GIT_SHORT_COMMIT} \
                                        --label build-date=\$(date -u +%Y-%m-%dT%H:%M:%SZ) \
                                        -t ${DOCKER_REPO}-${service.name}:${DOCKER_IMAGE_TAG} \
                                        -t ${DOCKER_REPO}-${service.name}:latest \
                                        ./${service.name}
                                """
                                retry(3) {
                                    sh """
                                        docker push ${DOCKER_REPO}-${service.name}:${DOCKER_IMAGE_TAG}
                                        docker push ${DOCKER_REPO}-${service.name}:latest
                                    """
                                }
                            }
                        }]
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            when { branch 'main' }
            steps {
                script {
                    sh 'kubectl config current-context'
                    sh 'kubectl apply -f k8s/'

                    parallel SERVICES.collectEntries { svc ->
                        def service = svc
                        ["Rollout › ${service.name}": {
                            timeout(time: 5, unit: 'MINUTES') {
                                try {
                                    sh "kubectl rollout status ${service.deployment} --timeout=${K8S_ROLLOUT_TIMEOUT}"
                                } catch (err) {
                                    echo "⚠️ Rollout échoué pour ${service.name} — rollback..."
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
            sh 'docker logout || true'
            cleanWs()
        }
        success {
            echo "✅ [${env.BRANCH_NAME ?: 'local'}] #${BUILD_NUMBER} → ${DOCKER_IMAGE_TAG}"
        }
        failure {
            echo "❌ [${env.BRANCH_NAME ?: 'local'}] #${BUILD_NUMBER} échoué."
        }
    }
}