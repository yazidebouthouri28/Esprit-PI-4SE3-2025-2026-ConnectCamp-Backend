pipeline {
agent any

```
environment {

    DOCKER_USERNAME = 'azizbenabdallah'
    DOCKER_CREDENTIALS = 'docker-hub-credentials'

    USER_SERVICE    = 'user-service'
    PRODUCT_SERVICE = 'product-service'
    ORDER_SERVICE   = 'order-service'
    API_GATEWAY     = 'api-gateway'

    DOCKER_BUILDKIT = "1"
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

        parallel {

            stage('user-service') {
                steps {
                    dir('user-service') {
                        sh './mvnw -B clean package -DskipTests'
                    }
                }
            }

            stage('product-service') {
                steps {
                    dir('product-service') {
                        sh './mvnw -B clean package -DskipTests'
                    }
                }
            }

            stage('order-service') {
                steps {
                    dir('order-service') {
                        sh './mvnw -B clean package -DskipTests'
                    }
                }
            }

            stage('api-gateway') {
                steps {
                    dir('api-gateway') {
                        sh './mvnw -B clean package -DskipTests'
                    }
                }
            }
        }
    }

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

    stage('Push Images') {

        steps {

            script {

                docker.withRegistry('', DOCKER_CREDENTIALS) {

                    parallel(

                    "Push user": {
                        sh "docker push ${DOCKER_USERNAME}/${USER_SERVICE}:${IMAGE_TAG}"
                        sh "docker push ${DOCKER_USERNAME}/${USER_SERVICE}:latest"
                    },

                    "Push product": {
                        sh "docker push ${DOCKER_USERNAME}/${PRODUCT_SERVICE}:${IMAGE_TAG}"
                        sh "docker push ${DOCKER_USERNAME}/${PRODUCT_SERVICE}:latest"
                    },

                    "Push order": {
                        sh "docker push ${DOCKER_USERNAME}/${ORDER_SERVICE}:${IMAGE_TAG}"
                        sh "docker push ${DOCKER_USERNAME}/${ORDER_SERVICE}:latest"
                    },

                    "Push gateway": {
                        sh "docker push ${DOCKER_USERNAME}/${API_GATEWAY}:${IMAGE_TAG}"
                        sh "docker push ${DOCKER_USERNAME}/${API_GATEWAY}:latest"
                    }

                    )
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

            sh """
            kubectl set image deployment/user-service user-service=${DOCKER_USERNAME}/${USER_SERVICE}:${IMAGE_TAG} -n ecommerce
            kubectl set image deployment/product-service product-service=${DOCKER_USERNAME}/${PRODUCT_SERVICE}:${IMAGE_TAG} -n ecommerce
            kubectl set image deployment/order-service order-service=${DOCKER_USERNAME}/${ORDER_SERVICE}:${IMAGE_TAG} -n ecommerce
            kubectl set image deployment/api-gateway api-gateway=${DOCKER_USERNAME}/${API_GATEWAY}:${IMAGE_TAG} -n ecommerce
            """

            sh "kubectl rollout status deployment/user-service -n ecommerce"
            sh "kubectl rollout status deployment/product-service -n ecommerce"
            sh "kubectl rollout status deployment/order-service -n ecommerce"
            sh "kubectl rollout status deployment/api-gateway -n ecommerce"
        }
    }
}

post {

    always {
        archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
        sh 'docker system prune -af || true'
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
```

}
