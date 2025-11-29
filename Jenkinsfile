pipeline {
    agent any

    environment {
        HELM_CHART_PATH = './helm/bankapp'  // Путь к зонтичному Helm-чарту для всего приложения
    }

    stages {
        stage('Checkout') {
            steps {
                // Клонируем репозиторий с GitHub и указываем ветку
                git url: 'https://github.com/MaximSlepukhin/bankapp.git', branch: 'feature/sprint-10'
            }
        }

        stage('Create Namespaces') {
            steps {
                script {
                    // Применяем манифест для создания пространств имен в Kubernetes
                    sh 'kubectl apply -f ./namespaces.yaml'
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    // Настройка окружения для использования Docker в Minikube
                    sh 'eval $(minikube docker-env)'

                    // Параллельная сборка Docker-образов для каждого микросервиса
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
                    // Устанавливаем базы данных (accounts-db и keycloak-db)
                    sh 'helm upgrade --install accounts-db ./helm/accounts-db --namespace dev --wait'
//                     sh 'helm upgrade --install keycloak-db ./helm/keycloak-db --namespace dev --wait'
                }
            }
        }

//         stage('Deploy to Kubernetes') {
//             steps {
//                 script {
//                     // Деплой всех сервисов через Helm в Minikube
//                     sh "helm upgrade --install bankapp ${HELM_CHART_PATH} --namespace dev -f ${HELM_CHART_PATH}/values-dev.yaml"
//                 }
//             }
//         }
//     }

    // Функция для сборки Docker-образов
    def buildAndPush(service) {
        // Сборка Docker-образа и его пометка
        sh """
            docker build -t ${service}:latest -f ${service}/Dockerfile ${service}
        """
    }
}
