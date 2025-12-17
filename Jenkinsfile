// pipeline {
//     agent { label 'role=jenkins-master' }
//
//     stages {
//         stage('Test Tools') {
//             steps {
//                 echo "Проверим доступность инструментов на агенте"
//                 sh 'java -version'
//                 sh 'mvn -version'
//                 sh 'docker --version'
//                 sh 'kubectl version --client'
//                 sh 'helm version'
//                 sh 'git --version'
//             }
//         }
//
//         stage('Build All Services') {
//             steps {
//                 script {
//                     // список микросервисов
//                     def services = [
//                         'accounts-service',
//                         'blocker-service',
//                         'cash-service',
//                         'exchange-generator-service',
//                         'exchange-service',
//                         'front-ui',
//                         'notifications-service',
//                         'transfer-service'
//                     ]
//
//                     // сборка каждого сервиса
//                     services.each { service ->
//                         echo "Сборка ${service}"
//                         dir(service) {
//                             sh 'mvn clean package'
//                         }
//                     }
//                 }
//             }
//         }
//
//         stage('Build Docker Images') {
//             steps {
//                 script {
//                     // переключаемся на Docker внутри Minikube
//                     sh """
//                         eval \$(minikube docker-env)
//
//                         docker build -t accounts-service:latest ./accounts-service
//                         docker build -t blocker-service:latest ./blocker-service
//                         docker build -t cash-service:latest ./cash-service
//                         docker build -t exchange-generator-service:latest ./exchange-generator-service
//                         docker build -t exchange-service:latest ./exchange-service
//                         docker build -t front-ui:latest ./front-ui
//                         docker build -t notifications-service:latest ./notifications-service
//                         docker build -t transfer-service:latest ./transfer-service
//                     """
//                 }
//             }
//         }
//     }
// }
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

        stage('Build Docker Images') {
            steps {
                script {
                    // переключаемся на Docker внутри Minikube
                    sh """
                        eval \$(minikube docker-env)

                        docker build -t accounts-service:latest ./accounts-service
                        docker build -t blocker-service:latest ./blocker-service
                        docker build -t cash-service:latest ./cash-service
                        docker build -t exchange-generator-service:latest ./exchange-generator-service
                        docker build -t exchange-service:latest ./exchange-service
                        docker build -t front-ui:latest ./front-ui
                        docker build -t notifications-service:latest ./notifications-service
                        docker build -t transfer-service:latest ./transfer-service
                    """
                }
            }
        }

        stage('Deploy Services with Helm') {
            steps {
                script {
                    // список микросервисов и их Helm-чартов
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

                    services.each { service ->
                        echo "Deploying ${service} with Helm"
                        dir("helm/${service}") {
                            sh """
                                helm upgrade --install ${service} . \
                                    --namespace default \
                                    --set image.repository=localhost:5000/${service} \
                                    --set image.tag=latest
                            """
                        }
                    }
                }
            }
        }
    }
}
