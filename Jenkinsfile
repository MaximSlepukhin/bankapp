pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/MaximSlepukhin/bankapp.git', branch: 'feature/sprint-10'
            }
        }

        stage('Check Versions') {
            steps {
                // Здесь указаны полные пути к инструментам, чтобы Jenkins их нашёл
                sh '''
                    echo "Checking tool versions..."
                    /usr/local/bin/docker --version
                    /opt/homebrew/bin/helm version --short
                    /usr/local/bin/kubectl version --client
                    /Users/maksim/apps/apache-maven-3.9.9/bin/mvn -v
                    /opt/homebrew/bin/minikube version
                '''
            }
        }

        stage('Build With Maven') {
            steps {
                script {
                    def services = [
                        "accounts-service",
                        "blocker-service",
                        "cash-service",
                        "exchange-generator-service",
                        "exchange-service",
                        "front-ui",
                        "notifications-service",
                        "transfer-service",
                    ]

                    services.each { service ->
                        sh """
                            echo "Building ${service}..."
                            cd ${service}
                            /Users/maksim/apps/apache-maven-3.9.9/bin/mvn clean package -DskipTests
                            cd ..
                        """
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    // Добавляем пути к Docker и Minikube в PATH
                    env.PATH = "/usr/local/bin:/opt/homebrew/bin:${env.PATH}"

                    def services = [
                        "accounts-service",
                        "blocker-service",
                        "cash-service",
                        "exchange-generator-service",
                        "exchange-service",
                        "front-ui",
                        "notifications-service",
                        "transfer-service",
                    ]

                    // Настройка Minikube Docker окружения
                    sh '''
                        echo "Setting up Minikube Docker environment..."
                        eval $("${env.PATH.split(':')[1]}/minikube" docker-env)
                    '''

                    services.each { service ->
                        sh """
                            echo "Building Docker image for ${service}..."
                            /usr/local/bin/docker build -t ${service}:latest -f ${service}/Dockerfile ${service}
                        """
                    }
                }
            }
        }

        stage('Deploy Databases with Helm') {
            steps {
                script {
                    def dbs = [
                        "accounts-db",
                        "keycloak-db"
                    ]

                    dbs.each { db ->
                        sh """
                            echo "Deploying ${db}..."
                            /opt/homebrew/bin/helm upgrade --install ${db} ./helm/bankapp/charts/${db} --namespace dev --create-namespace --wait --timeout 300s
                        """
                    }
                }
            }
        }

        stage('Deploy Microservices with Helm') {
            steps {
                script {
                    def services = [
                        "accounts-service",
                        "blocker-service",
                        "cash-service",
                        "exchange-generator-service",
                        "exchange-service",
                        "front-ui",
                        "notifications-service",
                        "transfer-service",
                        "keycloak"
                    ]

                    services.each { service ->
                        sh """
                            echo "Deploying ${service}..."
                            /opt/homebrew/bin/helm upgrade --install ${service} ./helm/bankapp/charts/${service} --namespace dev --wait --timeout 300s
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline завершён успешно."
        }
        failure {
            echo "Pipeline завершился с ошибкой."
        }
    }
}
