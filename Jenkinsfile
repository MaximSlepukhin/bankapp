pipeline {
    agent {
        docker {
            image 'jenkins-k8s'  // Кастомный образ с kubectl, helm, docker
            args '''
                -v /Users/maksim/.kube:/var/jenkins_home/.kube:ro
                -v /Users/maksim/.minikube:/var/jenkins_home/.minikube:ro
                -v /var/run/docker.sock:/var/run/docker.sock
            '''
        }
    }

    environment {
        HELM_CHART_PATH = './helm/bankapp'
        ORIGINAL_KUBECONFIG = '/var/jenkins_home/.kube/config'
        KUBECONFIG = '/tmp/kubeconfig' // используем копию внутри контейнера
        MINIKUBE_HOME = '/var/jenkins_home/.minikube'
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/MaximSlepukhin/bankapp.git', branch: 'feature/sprint-10'
            }
        }

        stage('Prepare kubeconfig') {
            steps {
                sh '''
                # Создаём рабочую копию kubeconfig внутри контейнера
                cp $ORIGINAL_KUBECONFIG $KUBECONFIG
                # Исправляем пути к сертификатам внутри контейнера
                sed -i "s|/Users/maksim/.minikube|$MINIKUBE_HOME|g" $KUBECONFIG
                # Используем host.docker.internal вместо 127.0.0.1 для доступа к Minikube API
                sed -i "s|127.0.0.1:[0-9]*|host.docker.internal:8443|g" $KUBECONFIG
                '''
            }
        }

        stage('Check Tools') {
            steps {
                sh 'kubectl version --client'
                sh 'helm version'
                sh 'docker version'
            }
        }

        stage('Create Namespaces') {
            steps {
                sh 'kubectl apply -f ./namespaces.yaml --validate=false'
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
                sh "helm upgrade --install bankapp ${HELM_CHART_PATH} --namespace dev -f ${HELM_CHART_PATH}/values-dev.yaml"
            }
        }
    }

    post {
        always {
            sh 'docker ps -a'
        }
    }
}

def buildAndPush(service) {
    sh """
        docker build -t ${service}:latest -f ${service}/Dockerfile ${service}
        # docker push ${service}:latest  # если нужно пушить в registry
    """
}
