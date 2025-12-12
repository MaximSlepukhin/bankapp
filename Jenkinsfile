pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                // Клонируем репозиторий
                git branch: 'feature/sprint-10', url: 'https://github.com/MaximSlepukhin/bankapp.git'
            }
        }
    }
}