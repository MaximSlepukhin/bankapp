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
    }
}
