pipeline {
    agent {
        docker {
            image 'jenkins-k8s' // твой кастомный образ с kubectl, helm, docker
            args "-v /Users/maksim/.kube:/var/jenkins_home/.kube -v /var/run/docker.sock:/var/run/docker.sock"
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
                sh 'kubectl version --client'
                sh 'helm version'
                sh 'docker version'
                sh 'eval $(minikube docker-env)' // подключаем Docker Minikube
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
                    // подключаем Docker Minikube
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
                sh 'helm upgrade --install accounts-db ./helm/accounts-db --namespace dev --wait'
                // sh 'helm upgrade --install keycloak-db ./helm/keycloak-db --namespace dev --wait'
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh "helm upgrade --install bankapp ${HELM_CHART_PATH} --namespace dev -f ${HELM_CHART_PATH}/values-dev.yaml"
            }
        }
    }
}

def buildAndPush(service) {
    sh """
        docker build -t ${service}:latest -f ${service}/Dockerfile ${service}
        # docker push ${service}:latest  # если нужен пуш в registry
    """
}
