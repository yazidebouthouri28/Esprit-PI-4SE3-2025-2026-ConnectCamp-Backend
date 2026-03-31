// ============================================================
// Jenkinsfile - E-Commerce Backend Microservices CI/CD Pipeline
// ============================================================

pipeline {
    agent any

    environment {
        DOCKER_USERNAME = 'azizbenabdallah'
        DOCKER_CREDENTIALS = 'docker-hub-credentials'

        IMAGE_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT?.take(7) ?: 'latest'}"

        USER_SERVICE    = 'user-service'
        PRODUCT_SERVICE = 'product-service'
        ORDER_SERVICE   = 'order-service'
        API_GATEWAY     = 'api-gateway'
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
    }

    stages {

        // ========================
        // Checkout
        // ========================
        stage('Checkout') {
            steps {
                echo 'Cloning repository...'
                checkout scm

                script {
                    env.GIT_BRANCH_NAME = sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                    env.GIT_SHORT_COMMIT = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                }
            }
        }

        // ========================
        // Build services
        // ========================
        stage('Build Services') {
            parallel {

                stage('Build user-service') {
                    steps {
                        dir('user-service') {
                            sh 'mvn clean compile -B'
                        }
                    }
                }

                stage('Build product-service') {
                    steps {
                        dir('product-service') {
                            sh 'mvn clean compile -B'
                        }
                    }
                }

                stage('Build order-service') {
                    steps {
                        dir('order-service') {
                            sh 'mvn clean compile -B'
                        }
                    }
                }

                stage('Build api-gateway') {
                    steps {
                        dir('api-gateway') {
                            sh 'mvn clean compile -B'
                        }
                    }
                }
            }
        }

        // ========================
        // Tests
        // ========================
        stage('Run Tests') {
            parallel {

                stage('Test user-service') {
                    steps {
                        dir('user-service') {
                            sh 'mvn test -B'
                        }
                    }
                }

                stage('Test product-service') {
                    steps {
                        dir('product-service') {
                            sh 'mvn test -B'
                        }
                    }
                }

                stage('Test order-service') {
                    steps {
                        dir('order-service') {
                            sh 'mvn test -B'
                        }
                    }
                }

                stage('Test api-gateway') {
                    steps {
                        dir('api-gateway') {
                            sh 'mvn test -B'
                        }
                    }
                }
            }
        }

        // ========================
        // Package
        // ========================
        stage('Package') {
            parallel {

                stage('Package user-service') {
                    steps {
                        dir('user-service') {
                            sh 'mvn package -DskipTests'
                        }
                    }
                }

                stage('Package product-service') {
                    steps {
                        dir('product-service') {
                            sh 'mvn package -DskipTests'
                        }
                    }
                }

                stage('Package order-service') {
                    steps {
                        dir('order-service') {
                            sh 'mvn package -DskipTests'
                        }
                    }
                }

                stage('Package api-gateway') {
                    steps {
                        dir('api-gateway') {
                            sh 'mvn package -DskipTests'
                        }
                    }
                }
            }
        }

        // ========================
        // Build Docker Images
        // ========================
        stage('Build Docker Images') {
            parallel {

                stage('Docker user-service') {
                    steps {
                        dir('user-service') {
                            sh """
                            docker build -t ${DOCKER_USERNAME}/${USER_SERVICE}:${IMAGE_TAG} \
                                         -t ${DOCKER_USERNAME}/${USER_SERVICE}:latest .
                            """
                        }
                    }
                }

                stage('Docker product-service') {
                    steps {
                        dir('product-service') {
                            sh """
                            docker build -t ${DOCKER_USERNAME}/${PRODUCT_SERVICE}:${IMAGE_TAG} \
                                         -t ${DOCKER_USERNAME}/${PRODUCT_SERVICE}:latest .
                            """
                        }
                    }
                }

                stage('Docker order-service') {
                    steps {
                        dir('order-service') {
                            sh """
                            docker build -t ${DOCKER_USERNAME}/${ORDER_SERVICE}:${IMAGE_TAG} \
                                         -t ${DOCKER_USERNAME}/${ORDER_SERVICE}:latest .
                            """
                        }
                    }
                }

                stage('Docker api-gateway') {
                    steps {
                        dir('api-gateway') {
                            sh """
                            docker build -t ${DOCKER_USERNAME}/${API_GATEWAY}:${IMAGE_TAG} \
                                         -t ${DOCKER_USERNAME}/${API_GATEWAY}:latest .
                            """
                        }
                    }
                }
            }
        }

        // ========================
        // Push Docker images
        // ========================
        stage('Push Images') {
            steps {
                script {

                    docker.withRegistry('', DOCKER_CREDENTIALS) {

                        def services = [
                                USER_SERVICE,
                                PRODUCT_SERVICE,
                                ORDER_SERVICE,
                                API_GATEWAY
                        ]

                        for (svc in services) {
                            sh "docker push ${DOCKER_USERNAME}/${svc}:${IMAGE_TAG}"
                            sh "docker push ${DOCKER_USERNAME}/${svc}:latest"
                        }

                    }
                }
            }
        }

        // ========================
        // Deploy Kubernetes
        // ========================
        stage('Deploy Kubernetes') {

            when {
                branch 'main'
            }

            steps {

                input message: 'Deploy to Kubernetes ?', ok: 'Deploy'

                sh """

                sed -i 's|image:.*user-service.*|image: ${DOCKER_USERNAME}/${USER_SERVICE}:${IMAGE_TAG}|' k8s/10-user-service-deployment.yaml
                sed -i 's|image:.*product-service.*|image: ${DOCKER_USERNAME}/${PRODUCT_SERVICE}:${IMAGE_TAG}|' k8s/11-product-service-deployment.yaml
                sed -i 's|image:.*order-service.*|image: ${DOCKER_USERNAME}/${ORDER_SERVICE}:${IMAGE_TAG}|' k8s/12-order-service-deployment.yaml
                sed -i 's|image:.*api-gateway.*|image: ${DOCKER_USERNAME}/${API_GATEWAY}:${IMAGE_TAG}|' k8s/13-api-gateway-deployment.yaml

                kubectl apply -f k8s/00-namespace.yaml
                kubectl apply -f k8s/01-configmap.yaml
                kubectl apply -f k8s/02-secrets.yaml

                kubectl apply -f k8s/03-mysql-deployment.yaml
                kubectl apply -f k8s/03-mysql-service.yaml

                kubectl apply -f k8s/04-zookeeper-deployment.yaml
                kubectl apply -f k8s/04-zookeeper-service.yaml

                kubectl apply -f k8s/04-kafka-deployment.yaml
                kubectl apply -f k8s/04-kafka-service.yaml

                kubectl apply -f k8s/05-redis-deployment.yaml
                kubectl apply -f k8s/05-redis-service.yaml

                kubectl -n ecommerce rollout status deployment/mysql --timeout=120s || true

                kubectl apply -f k8s/10-user-service-deployment.yaml
                kubectl apply -f k8s/10-user-service-service.yaml

                kubectl apply -f k8s/11-product-service-deployment.yaml
                kubectl apply -f k8s/11-product-service-service.yaml

                kubectl apply -f k8s/12-order-service-deployment.yaml
                kubectl apply -f k8s/12-order-service-service.yaml

                kubectl apply -f k8s/13-api-gateway-deployment.yaml
                kubectl apply -f k8s/13-api-gateway-service.yaml

                kubectl apply -f k8s/14-ingress.yaml || true

                kubectl -n ecommerce get pods
                kubectl -n ecommerce get services

                """
            }
        }
    }

    post {

        always {
            archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true, allowEmptyArchive: true
            sh 'docker image prune -f || true'
        }

        success {
            echo 'Pipeline SUCCESS'
        }

        failure {
            echo 'Pipeline FAILED'
        }

        unstable {
            echo 'Pipeline UNSTABLE'
        }

        cleanup {
            cleanWs()
        }
    }
}