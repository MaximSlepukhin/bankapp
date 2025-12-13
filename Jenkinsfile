pipeline {
    agent { label 'role=jenkins-master' }

    stages {
        stage('Test Tools') {
            steps {
                echo "Проверим доступность инструментов на агенте"
                sh 'java -version'
                sh 'mvn -version'
                sh 'docker --version'
                sh 'kubectl version --client'
                sh 'helm version'
                sh 'git --version'
            }
        }

        stage('Build All Services') {
            steps {
                script {
                    // список микросервисов
                    def services = [
                        'accounts-service',
                        'blocker-service',
                        'cash-service',
                        'exchange-generator-service',
                        'exchange-service',
                        'front-ui',
                        'notifications-service',
                        'transfer-service'
                    ]

                    // сборка каждого сервиса
                    services.each { service ->
                        echo "Сборка ${service}"
                        dir(service) {
                            sh 'mvn clean package'
                        }
                    }
                }
            }
        }
    }
}
