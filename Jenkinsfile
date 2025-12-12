pipeline {
    agent { label 'role=jenkins-master' }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'feature/sprint-10', url: 'https://github.com/MaximSlepukhin/bankapp.git'
            }
        }
        stage('Check Environment') {
            steps {
                sh 'mvn -v'
                sh 'docker --version'
                sh 'kubectl version --client'
                sh 'helm version'
                sh 'git --version'
                sh 'java -version'
            }
        }
    }
}
