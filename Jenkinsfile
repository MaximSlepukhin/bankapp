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
                sh '''
                    echo "Checking tool versions..."
                    /usr/local/bin/docker --version
                    /opt/homebrew/bin/helm version --short
                    /usr/local/bin/kubectl version --client
                    /Users/maksim/apps/apache-maven-3.9.9/bin/mvn -v
                '''
            }
        }

        stage('Build With Maven') {
            steps {
                script {
                    // Список микросервисов
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
