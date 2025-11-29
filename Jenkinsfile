pipeline {
    agent any

    environment {
        HELM_CHART_PATH = './helm/bankapp'  // Путь к зонтичному Helm-чарту для всего приложения
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/MaximSlepukhin/bankapp.git', branch: 'feature/sprint-10'
            }
        }

        stage('Create Namespaces') {
            steps {
                script {
                    sh 'kubectl apply -f ./namespaces.yaml'
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    sh 'eval $(minikube docker-env)'

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
                script {
                    sh 'helm upgrade --install accounts-db ./helm/accounts-db --namespace dev --wait'
                    // sh 'helm upgrade --install keycloak-db ./helm/keycloak-db --namespace dev --wait'
                }
            }
        }

//         stage('Deploy to Kubernetes') {
//             steps {
//                 script {
//                     sh "helm upgrade --install bankapp ${HELM_CHART_PATH} --namespace dev -f ${HELM_CHART_PATH}/values-dev.yaml"
//                 }
//             }
//         }

    } // <-- Закрываем блок stages

} // <-- Закрываем блок pipeline

// Функция для сборки Docker-образов
def buildAndPush(service) {
    sh """
        docker build -t ${service}:latest -f ${service}/Dockerfile ${service}
    """
}
