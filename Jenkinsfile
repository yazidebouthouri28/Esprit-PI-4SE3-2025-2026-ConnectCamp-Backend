pipeline {
    agent any

    environment {
        DOCKER_USERNAME = 'azizbenabdallah'
        DOCKER_CREDENTIALS = 'docker-hub-credentials'

        USER_SERVICE    = 'user-service'
        PRODUCT_SERVICE = 'product-service'
        ORDER_SERVICE   = 'order-service'
        API_GATEWAY     = 'api-gateway'
    }

    options {
        timeout(time: 2, unit: 'HOURS')     // augmenté
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm

                script {
                    env.GIT_BRANCH_NAME  = sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                    env.GIT_SHORT_COMMIT = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    env.IMAGE_TAG        = "${env.BUILD_NUMBER}-${env.GIT_SHORT_COMMIT}"
                }
            }
        }

        stage('Prepare Maven Wrapper') {
            steps {
                sh '''
                for dir in user-service product-service order-service api-gateway; do
                    if [ -f $dir/mvnw ]; then
                        chmod +x $dir/mvnw
                    fi
                done
                '''
            }
        }

        // ==============================
        // BUILD + TEST + PACKAGE (1 seule fois)
        // ==============================

        stage('Build Microservices') {
            parallel {

                stage('user-service') {
                    steps {
                        dir('user-service') {
                            sh './mvnw clean package -DskipTests=false -B'
                        }
                    }
                }

                stage('product-service') {
                    steps {
                        dir('product-service') {
                            sh './mvnw clean package -DskipTests=false -B'
                        }
                    }
                }

                stage('order-service') {
                    steps {
                        dir('order-service') {
                            sh './mvnw clean package -DskipTests=false -B'
                        }
                    }
                }

                stage('api-gateway') {
                    steps {
                        dir('api-gateway') {
                            sh './mvnw clean package -DskipTests=false -B'
                        }
                    }
                }
            }
        }

        // ==============================
        // BUILD DOCKER IMAGES
        // ==============================

        stage('Build Docker Images') {
            parallel {

                stage('Docker user-service') {
                    steps {
                        dir('user-service') {
                            sh """
                            docker build \
                            -t ${DOCKER_USERNAME}/${USER_SERVICE}:${IMAGE_TAG} \
                            -t ${DOCKER_USERNAME}/${USER_SERVICE}:latest .
                            """
                        }
                    }
                }

                stage('Docker product-service') {
                    steps {
                        dir('product-service') {
                            sh """
                            docker build \
                            -t ${DOCKER_USERNAME}/${PRODUCT_SERVICE}:${IMAGE_TAG} \
                            -t ${DOCKER_USERNAME}/${PRODUCT_SERVICE}:latest .
                            """
                        }
                    }
                }

                stage('Docker order-service') {
                    steps {
                        dir('order-service') {
                            sh """
                            docker build \
                            -t ${DOCKER_USERNAME}/${ORDER_SERVICE}:${IMAGE_TAG} \
                            -t ${DOCKER_USERNAME}/${ORDER_SERVICE}:latest .
                            """
                        }
                    }
                }

                stage('Docker api-gateway') {
                    steps {
                        dir('api-gateway') {
                            sh """
                            docker build \
                            -t ${DOCKER_USERNAME}/${API_GATEWAY}:${IMAGE_TAG} \
                            -t ${DOCKER_USERNAME}/${API_GATEWAY}:latest .
                            """
                        }
                    }
                }
            }
        }

        // ==============================
        // PUSH IMAGES
        // ==============================

        stage('Push Docker Images') {
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

        // ==============================
        // DEPLOY KUBERNETES
        // ==============================

        stage('Deploy Kubernetes') {

            when { branch 'main' }

            steps {

                timeout(time: 10, unit: 'MINUTES') {
                    input message: 'Deploy to Kubernetes ?', ok: 'Deploy'
                }

                sh """
                sed -i 's|image: .*user-service.*|image: ${DOCKER_USERNAME}/${USER_SERVICE}:${IMAGE_TAG}|' k8s/*user*
                sed -i 's|image: .*product-service.*|image: ${DOCKER_USERNAME}/${PRODUCT_SERVICE}:${IMAGE_TAG}|' k8s/*product*
                sed -i 's|image: .*order-service.*|image: ${DOCKER_USERNAME}/${ORDER_SERVICE}:${IMAGE_TAG}|' k8s/*order*
                sed -i 's|image: .*api-gateway.*|image: ${DOCKER_USERNAME}/${API_GATEWAY}:${IMAGE_TAG}|' k8s/*gateway*
                """

                sh "kubectl apply -f k8s/"
                sh "kubectl -n ecommerce get pods"
            }
        }
    }

    post {

        always {
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
            sh 'docker image prune -f || true'
        }

        success {
            echo 'Pipeline SUCCESS'
        }

        failure {
            echo 'Pipeline FAILED'
        }

        cleanup {
            cleanWs()
        }
    }
}