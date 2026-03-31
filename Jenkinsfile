pipeline {
    agent any

    environment {
        DOCKER_USERNAME = 'azizbenabdallah'
        DOCKER_CREDENTIALS = 'docker-hub-credentials'

        USER_SERVICE    = 'user-service'
        PRODUCT_SERVICE = 'product-service'
        ORDER_SERVICE   = 'order-service'
        API_GATEWAY     = 'api-gateway'

        DOCKER_BUILDKIT = "1"
        MAVEN_OPTS      = "-Dmaven.repo.local=${WORKSPACE}/.m2/repository"
    }

    options {
        timeout(time: 2, unit: 'HOURS')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    env.IMAGE_TAG = "${BUILD_NUMBER}-${GIT_COMMIT_SHORT}"
                }
            }
        }

        stage('Prepare Maven Wrapper') {
            steps {
                sh '''
                for dir in user-service product-service order-service api-gateway
                do
                  if [ -f $dir/mvnw ]; then
                    chmod +x $dir/mvnw
                  fi
                done
                '''
            }
        }

        stage('Build Microservices') {
            parallel failFast: false, stages: [
                stage('user-service') { steps { dir('user-service') { sh './mvnw -B clean package -DskipTests' } } },
                stage('product-service') { steps { dir('product-service') { sh './mvnw -B clean package -DskipTests' } } },
                stage('order-service') { steps { dir('order-service') { sh './mvnw -B clean package -DskipTests' } } },
                stage('api-gateway') { steps { dir('api-gateway') { sh './mvnw -B clean package -DskipTests' } } }
            ]
        }

        stage('Build Docker Images') {
            parallel failFast: false, stages: [
                stage('Docker user-service') {
                    steps { dir('user-service') {
                        sh "docker build -t ${DOCKER_USERNAME}/${USER_SERVICE}:${IMAGE_TAG} -t ${DOCKER_USERNAME}/${USER_SERVICE}:latest ."
                    } }
                },
                stage('Docker product-service') {
                    steps { dir('product-service') {
                        sh "docker build -t ${DOCKER_USERNAME}/${PRODUCT_SERVICE}:${IMAGE_TAG} -t ${DOCKER_USERNAME}/${PRODUCT_SERVICE}:latest ."
                    } }
                },
                stage('Docker order-service') {
                    steps { dir('order-service') {
                        sh "docker build -t ${DOCKER_USERNAME}/${ORDER_SERVICE}:${IMAGE_TAG} -t ${DOCKER_USERNAME}/${ORDER_SERVICE}:latest ."
                    } }
                },
                stage('Docker api-gateway') {
                    steps { dir('api-gateway') {
                        sh "docker build -t ${DOCKER_USERNAME}/${API_GATEWAY}:${IMAGE_TAG} -t ${DOCKER_USERNAME}/${API_GATEWAY}:latest ."
                    } }
                }
            ]
        }

        stage('Push Images') {
            steps {
                script {
                    docker.withRegistry('', DOCKER_CREDENTIALS) {
                        parallel failFast: false, stages: [
                            stage('Push user-service') {
                                steps {
                                    sh "docker push ${DOCKER_USERNAME}/${USER_SERVICE}:${IMAGE_TAG}"
                                    sh "docker push ${DOCKER_USERNAME}/${USER_SERVICE}:latest"
                                }
                            },
                            stage('Push product-service') {
                                steps {
                                    sh "docker push ${DOCKER_USERNAME}/${PRODUCT_SERVICE}:${IMAGE_TAG}"
                                    sh "docker push ${DOCKER_USERNAME}/${PRODUCT_SERVICE}:latest"
                                }
                            },
                            stage('Push order-service') {
                                steps {
                                    sh "docker push ${DOCKER_USERNAME}/${ORDER_SERVICE}:${IMAGE_TAG}"
                                    sh "docker push ${DOCKER_USERNAME}/${ORDER_SERVICE}:latest"
                                }
                            },
                            stage('Push api-gateway') {
                                steps {
                                    sh "docker push ${DOCKER_USERNAME}/${API_GATEWAY}:${IMAGE_TAG}"
                                    sh "docker push ${DOCKER_USERNAME}/${API_GATEWAY}:latest"
                                }
                            }
                        ]
                    }
                }
            }
        }

        stage('Deploy Kubernetes') {
            when { branch 'main' }
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    input message: 'Deploy to Kubernetes ?', ok: 'Deploy'
                }

                script {
                    def services = [USER_SERVICE, PRODUCT_SERVICE, ORDER_SERVICE, API_GATEWAY]
                    for (svc in services) {
                        sh "kubectl set image deployment/${svc} ${svc}=${DOCKER_USERNAME}/${svc}:${IMAGE_TAG} -n ecommerce"
                        sh "kubectl rollout status deployment/${svc} -n ecommerce"
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
            sh 'docker image prune -f || true'
        }
        success { echo 'Pipeline SUCCESS' }
        failure { echo 'Pipeline FAILED' }
        cleanup { cleanWs() }
    }
}