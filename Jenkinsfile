pipeline {
    agent {
        docker {
            image 'maven:3.9.8-eclipse-temurin-21'
        }
    }

    stages {
        stage('Checkout') {
            steps {
                // Клонируем репозиторий
                git branch: 'feature/sprint-10', url: 'https://github.com/MaximSlepukhin/bankapp.git'
            }
        }

        stage('Build with Maven') {
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
                        "transfer-service"
                    ]

                    for (service in services) {
                        dir(service) {
                            echo "Building ${service}..."
                            sh 'mvn clean package'
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed.'
        }
    }
}
