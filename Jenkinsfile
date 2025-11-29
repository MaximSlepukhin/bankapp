pipeline {
    agent {
        docker {
            image 'jenkins-k8s'  // Твой кастомный образ с kubectl, helm, docker
            args '-v ~/.kube:/var/jenkins_home/.kube -v /var/run/docker.sock:/var/run/docker.sock'
        }
    }

    environment {
        HELM_CHART_PATH = './helm/bankapp'
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/MaximSlepukhin/bankapp.git', branch: 'feature/sprint-10'
            }
        }

        stage('Check Tools') {
            steps {
                script {
                    // Проверяем доступность kubectl, helm и docker
                    sh 'kubectl version --client'
                    sh 'helm version'
                    sh 'docker version'
                    // Подключаем docker к minikube
                    sh 'eval $(minikube docker-env)'
                }
            }
        }

        stage('Check Docker') {
            steps {
                script {
                    // Проверяем Docker в контексте minikube
                    sh 'eval $(minikube docker-env)'  // Подключаем Docker Minikube
                    sh 'docker version'  // Проверяем доступность Docker
                }
            }
        }

        stage('Create Namespaces') {
            steps {
                sh 'kubectl apply -f ./namespaces.yaml'
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    parallel(
                        'accounts-service': { buildAndPush('accounts-service') },
                        'blocker-service': { buildAndPush('blocker-service') },
                        'cash-service': { buildAndPush('cash-service') },
                        'exchange-generator-service': { buildAndPush('exchange-generator-service') },
                        'exchange-service': { buildAndPush('exchange-service') },
                        'front-ui': { buildAndPush('front-ui') },
                        'notifications-service': { buildAndPush('notifications-service') },
                        'transfer-service': { buildAndPush('transfer-service') }
                    )
                }
            }
        }

        stage('Deploy Databases') {
            steps {
                sh 'helm upgrade --install accounts-db ./helm/accounts-db --namespace dev --wait'
                // sh 'helm upgrade --install keycloak-db ./helm/keycloak-db --namespace dev --wait'
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                // Здесь можно добавить команду для деплоя на Kubernetes, если нужно
                sh "helm upgrade --install bankapp ${HELM_CHART_PATH} --namespace dev -f ${HELM_CHART_PATH}/values-dev.yaml"
            }
        }
    }
}

def buildAndPush(service) {
    sh """
        docker build -t ${service}:latest -f ${service}/Dockerfile ${service}
    """
}
