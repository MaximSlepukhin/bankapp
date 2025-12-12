pipeline {
    agent {
        kubernetes {
            label 'jenkins'
        }
    }

    stages {
        stage('Checkout') {
            steps {
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
                            container('maven') {  // выполняем этот шаг в контейнере maven
                                sh 'mvn clean package'
                            }
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
