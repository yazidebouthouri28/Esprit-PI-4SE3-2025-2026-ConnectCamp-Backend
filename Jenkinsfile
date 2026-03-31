// ============================================================
// Jenkinsfile - E-Commerce Backend Microservices CI/CD Pipeline
// ============================================================

pipeline {
    agent any

    environment {
        DOCKER_USERNAME = 'azizbenabdallah'
        DOCKER_CREDENTIALS = 'docker-hub-credentials'

        USER_SERVICE    = 'user-service'
        PRODUCT_SERVICE = 'product-service'
        ORDER_SERVICE   = 'order-service'
        API_GATEWAY     = 'api-gateway'

        // Maven commands with fallback
        BUILD_CMD   = 'if [ -f mvnw ]; then ./mvnw clean compile -B; else mvn clean compile -B; fi'
        TEST_CMD    = 'if [ -f mvnw ]; then ./mvnw test -B; else mvn test -B; fi'
        PACKAGE_CMD = 'if [ -f mvnw ]; then ./mvnw package -DskipTests -B; else mvn package -DskipTests -B; fi'
    }

    options {
        timeout(time: 60, unit: 'MINUTES')
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
                    env.GIT_BRANCH_NAME  = sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                    env.GIT_SHORT_COMMIT = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    env.IMAGE_TAG        = "${env.BUILD_NUMBER}-${env.GIT_SHORT_COMMIT ?: 'latest'}"
                }
            }
        }

        // ========================
        // Set mvnw Permissions
        // ========================
        stage('Set Maven Wrapper Permissions') {
            steps {
                echo "Ensuring ./mvnw is executable for all services"
                sh '''
                    for dir in user-service product-service order-service api-gateway; do
                        if [ -f $dir/mvnw ]; then
                            chmod +x $dir/mvnw
                        fi
                    done
                '''
            }
        }

        // ========================
        // Build Services
        // ========================
        stage('Build Services') {
            parallel {
                stage('Build user-service') { steps { dir('user-service') { sh "${BUILD_CMD}" } } }
                stage('Build product-service') { steps { dir('product-service') { sh "${BUILD_CMD}" } } }
                stage('Build order-service') { steps { dir('order-service') { sh "${BUILD_CMD}" } } }
                stage('Build api-gateway') { steps { dir('api-gateway') { sh "${BUILD_CMD}" } } }
            }
        }

        // ========================
        // Run Tests
        // ========================
        stage('Run Tests') {
            parallel {
                stage('Test user-service') { steps { dir('user-service') { sh "${TEST_CMD}" } } }
                stage('Test product-service') { steps { dir('product-service') { sh "${TEST_CMD}" } } }
                stage('Test order-service') { steps { dir('order-service') { sh "${TEST_CMD}" } } }
                stage('Test api-gateway') { steps { dir('api-gateway') { sh "${TEST_CMD}" } } }
            }
        }

        // ========================
        // Package Services
        // ========================
        stage('Package') {
            parallel {
                stage('Package user-service') { steps { dir('user-service') { sh "${PACKAGE_CMD}" } } }
                stage('Package product-service') { steps { dir('product-service') { sh "${PACKAGE_CMD}" } } }
                stage('Package order-service') { steps { dir('order-service') { sh "${PACKAGE_CMD}" } } }
                stage('Package api-gateway') { steps { dir('api-gateway') { sh "${PACKAGE_CMD}" } } }
            }
        }

        // ========================
        // Build Docker Images
        // ========================
        stage('Build Docker Images') {
            parallel {
                stage('Docker user-service') {
                    steps { dir('user-service') { sh "docker build -t ${DOCKER_USERNAME}/${USER_SERVICE}:${IMAGE_TAG} -t ${DOCKER_USERNAME}/${USER_SERVICE}:latest ." } }
                }
                stage('Docker product-service') {
                    steps { dir('product-service') { sh "docker build -t ${DOCKER_USERNAME}/${PRODUCT_SERVICE}:${IMAGE_TAG} -t ${DOCKER_USERNAME}/${PRODUCT_SERVICE}:latest ." } }
                }
                stage('Docker order-service') {
                    steps { dir('order-service') { sh "docker build -t ${DOCKER_USERNAME}/${ORDER_SERVICE}:${IMAGE_TAG} -t ${DOCKER_USERNAME}/${ORDER_SERVICE}:latest ." } }
                }
                stage('Docker api-gateway') {
                    steps { dir('api-gateway') { sh "docker build -t ${DOCKER_USERNAME}/${API_GATEWAY}:${IMAGE_TAG} -t ${DOCKER_USERNAME}/${API_GATEWAY}:latest ." } }
                }
            }
        }

        // ========================
        // Push Docker Images
        // ========================
        stage('Push Images') {
            steps {
                script {
                    docker.withRegistry('', DOCKER_CREDENTIALS) {
                        echo 'Pushing Docker images...'
                        def services = [USER_SERVICE, PRODUCT_SERVICE, ORDER_SERVICE, API_GATEWAY]
                        for (svc in services) {
                            sh "docker push ${DOCKER_USERNAME}/${svc}:${IMAGE_TAG}"
                            sh "docker push ${DOCKER_USERNAME}/${svc}:latest"
                        }
                    }
                }
            }
        }

        // ========================
        // Deploy to Kubernetes
        // ========================
        stage('Deploy Kubernetes') {
            when { branch 'main' }

            steps {
                script {
                    timeout(time: 10, unit: 'MINUTES') {
                        input message: 'Deploy to Kubernetes ?', ok: 'Deploy'
                    }

                    // Backup YAMLs
                    sh "cp -r k8s k8s_backup || true"

                    // Update image tags
                    def servicesPorts = [
                        "${USER_SERVICE}": 8084,
                        "${PRODUCT_SERVICE}": 8082,
                        "${ORDER_SERVICE}": 8083,
                        "${API_GATEWAY}": 8088
                    ]

                    servicesPorts.each { svc, port ->
                        sh "sed -i 's|image:.*${svc}.*|image: ${DOCKER_USERNAME}/${svc}:${IMAGE_TAG}|' k8s/*-deployment.yaml"
                    }

                    // Apply manifests
                    sh """
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
    }

    post {
        always {
            archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true, allowEmptyArchive: true
            sh 'docker image prune -f || true'
        }

        success { echo 'Pipeline SUCCESS' }
        failure { echo 'Pipeline FAILED' }
        unstable { echo 'Pipeline UNSTABLE' }

        cleanup { cleanWs() }
    }
}