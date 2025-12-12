pipeline {
    agent any  // статический Jenkins

    environment {
        MAVEN_HOME = "/opt/apache-maven-3.9.5"
        PATH = "${MAVEN_HOME}/bin:${env.PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'feature/sprint-10', url: 'https://github.com/MaximSlepukhin/bankapp.git'
            }
        }

        stage('Check Environment') {
            steps {
                sh 'echo "MAVEN_HOME=$MAVEN_HOME"'
                sh 'echo "PATH=$PATH"'
                sh 'which mvn'
                sh 'mvn -v'
                sh 'java -version'
                sh 'docker --version'
                sh 'kubectl version --client'
                sh 'helm version'
                sh 'git --version'
            }
        }

        // Пока не выполняем стадии сборки
        // stage('Build with Maven') {
        //     steps {
        //         script {
        //             def services = [
        //                 "accounts-service",
        //                 "blocker-service",
        //                 "cash-service",
        //                 "exchange-generator-service",
        //                 "exchange-service",
        //                 "front-ui",
        //                 "notifications-service",
        //                 "transfer-service"
        //             ]
        //
        //             for (service in services) {
        //                 dir(service) {
        //                     echo "Building ${service}..."
        //                     sh 'mvn clean package'
        //                 }
        //             }
        //         }
        //     }
        // }
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
