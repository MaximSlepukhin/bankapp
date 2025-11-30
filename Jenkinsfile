// // // // // // // // // pipeline {
// // // // // // // // //     agent {
// // // // // // // // //         docker {
// // // // // // // // //             image 'jenkins-k8s'  // Кастомный образ с kubectl, helm, docker
// // // // // // // // //             args '''
// // // // // // // // //                 -v /Users/maksim/.kube:/var/jenkins_home/.kube:ro
// // // // // // // // //                 -v /Users/maksim/.minikube:/var/jenkins_home/.minikube:ro
// // // // // // // // //                 -v /var/run/docker.sock:/var/run/docker.sock
// // // // // // // // //             '''
// // // // // // // // //         }
// // // // // // // // //     }
// // // // // // // // //
// // // // // // // // //     environment {
// // // // // // // // //         HELM_CHART_PATH = './helm/bankapp'
// // // // // // // // //         ORIGINAL_KUBECONFIG = '/var/jenkins_home/.kube/config'
// // // // // // // // //         KUBECONFIG = '/tmp/kubeconfig' // используем копию внутри контейнера
// // // // // // // // //         MINIKUBE_HOME = '/var/jenkins_home/.minikube'
// // // // // // // // //     }
// // // // // // // // //
// // // // // // // // //     stages {
// // // // // // // // //         stage('Checkout') {
// // // // // // // // //             steps {
// // // // // // // // //                 git url: 'https://github.com/MaximSlepukhin/bankapp.git', branch: 'feature/sprint-10'
// // // // // // // // //             }
// // // // // // // // //         }
// // // // // // // // //
// // // // // // // // //         stage('Prepare kubeconfig') {
// // // // // // // // //             steps {
// // // // // // // // //                 sh '''
// // // // // // // // //                 # Создаём рабочую копию kubeconfig внутри контейнера
// // // // // // // // //                 cp $ORIGINAL_KUBECONFIG $KUBECONFIG
// // // // // // // // //
// // // // // // // // //                 # Исправляем пути к сертификатам внутри контейнера
// // // // // // // // //                 sed -i "s|/Users/maksim/.minikube|$MINIKUBE_HOME|g" $KUBECONFIG
// // // // // // // // //
// // // // // // // // //                 # Меняем API-сервер на фактический адрес Minikube
// // // // // // // // //                 sed -i "s|127.0.0.1:[0-9]*|host.docker.internal:50049|g" $KUBECONFIG
// // // // // // // // //                 '''
// // // // // // // // //             }
// // // // // // // // //         }
// // // // // // // // //
// // // // // // // // //         stage('Check Tools') {
// // // // // // // // //             steps {
// // // // // // // // //                 sh 'kubectl version --client'
// // // // // // // // //                 sh 'helm version'
// // // // // // // // //                 sh 'docker version'
// // // // // // // // //             }
// // // // // // // // //         }
// // // // // // // // //
// // // // // // // // //         stage('Build with Maven') {
// // // // // // // // //             agent {
// // // // // // // // //                 docker {
// // // // // // // // //                     image 'maven:3.9.5-eclipse-temurin-17'
// // // // // // // // //                     args '-v $WORKSPACE:/workspace'  // монтируем рабочую директорию
// // // // // // // // //                 }
// // // // // // // // //             }
// // // // // // // // //             steps {
// // // // // // // // //                 dir('/workspace') {  // чтобы Maven работал в корне репозитория
// // // // // // // // //                     sh 'mvn clean package -DskipTests'
// // // // // // // // //                 }
// // // // // // // // //             }
// // // // // // // // //         }
// // // // // // // // //
// // // // // // // // //
// // // // // // // // //         stage('Create Namespaces') {
// // // // // // // // //             steps {
// // // // // // // // //                 // Игнорируем TLS, чтобы не было ошибок x509
// // // // // // // // //                 sh 'kubectl --insecure-skip-tls-verify apply -f ./namespaces.yaml --validate=false'
// // // // // // // // //             }
// // // // // // // // //         }
// // // // // // // // //
// // // // // // // // //         stage('Build Docker Images') {
// // // // // // // // //             steps {
// // // // // // // // //                 script {
// // // // // // // // //                     parallel(
// // // // // // // // //                         'accounts-service': { buildAndPush('accounts-service') },
// // // // // // // // //                         'blocker-service': { buildAndPush('blocker-service') },
// // // // // // // // //                         'cash-service': { buildAndPush('cash-service') },
// // // // // // // // //                         'exchange-generator-service': { buildAndPush('exchange-generator-service') },
// // // // // // // // //                         'exchange-service': { buildAndPush('exchange-service') },
// // // // // // // // //                         'front-ui': { buildAndPush('front-ui') },
// // // // // // // // //                         'notifications-service': { buildAndPush('notifications-service') },
// // // // // // // // //                         'transfer-service': { buildAndPush('transfer-service') }
// // // // // // // // //                     )
// // // // // // // // //                 }
// // // // // // // // //             }
// // // // // // // // //         }
// // // // // // // // //
// // // // // // // // //         stage('Deploy Databases') {
// // // // // // // // //             steps {
// // // // // // // // //                 sh 'helm upgrade --install accounts-db ./helm/accounts-db --namespace dev --wait'
// // // // // // // // //                 // sh 'helm upgrade --install keycloak-db ./helm/keycloak-db --namespace dev --wait'
// // // // // // // // //             }
// // // // // // // // //         }
// // // // // // // // //
// // // // // // // // //         stage('Deploy to Kubernetes') {
// // // // // // // // //             steps {
// // // // // // // // //                 sh "helm upgrade --install bankapp ${HELM_CHART_PATH} --namespace dev -f ${HELM_CHART_PATH}/values-dev.yaml"
// // // // // // // // //             }
// // // // // // // // //         }
// // // // // // // // //     }
// // // // // // // // //
// // // // // // // // //     post {
// // // // // // // // //         always {
// // // // // // // // //             sh 'docker ps -a'
// // // // // // // // //         }
// // // // // // // // //     }
// // // // // // // // // }
// // // // // // // // //
// // // // // // // // // def buildAndPush(service) {
// // // // // // // // //     sh """
// // // // // // // // //         docker build -t ${service}:latest -f ${service}/Dockerfile ${service}
// // // // // // // // //         # docker push ${service}:latest  # если нужно пушить в registry
// // // // // // // // //     """
// // // // // // // // // }
// // // // // // // // pipeline {
// // // // // // // //     agent {
// // // // // // // //         docker {
// // // // // // // //             image 'jenkins-k8s'
// // // // // // // //             args '''
// // // // // // // //                 -v /Users/maksim/.kube:/var/jenkins_home/.kube:ro
// // // // // // // //                 -v /Users/maksim/.minikube:/var/jenkins_home/.minikube:ro
// // // // // // // //                 -v /var/run/docker.sock:/var/run/docker.sock
// // // // // // // //             '''
// // // // // // // //         }
// // // // // // // //     }
// // // // // // // //
// // // // // // // //     environment {
// // // // // // // //         HELM_CHART_PATH = './helm/bankapp'
// // // // // // // //         ORIGINAL_KUBECONFIG = '/var/jenkins_home/.kube/config'
// // // // // // // //         KUBECONFIG = '/tmp/kubeconfig'
// // // // // // // //         MINIKUBE_HOME = '/var/jenkins_home/.minikube'
// // // // // // // //     }
// // // // // // // //
// // // // // // // //     stages {
// // // // // // // //
// // // // // // // //         stage('Checkout') {
// // // // // // // //             steps {
// // // // // // // //                 git url: 'https://github.com/MaximSlepukhin/bankapp.git', branch: 'feature/sprint-10'
// // // // // // // //             }
// // // // // // // //         }
// // // // // // // //
// // // // // // // //         stage('Prepare kubeconfig') {
// // // // // // // //             steps {
// // // // // // // //                 sh '''
// // // // // // // //                 cp $ORIGINAL_KUBECONFIG $KUBECONFIG
// // // // // // // //                 sed -i "s|/Users/maksim/.minikube|$MINIKUBE_HOME|g" $KUBECONFIG
// // // // // // // //                 sed -i "s|127.0.0.1:[0-9]*|host.docker.internal:50049|g" $KUBECONFIG
// // // // // // // //                 '''
// // // // // // // //             }
// // // // // // // //         }
// // // // // // // //
// // // // // // // //         stage('Check Tools') {
// // // // // // // //             steps {
// // // // // // // //                 sh 'kubectl version --client'
// // // // // // // //                 sh 'helm version'
// // // // // // // //                 sh 'docker version'
// // // // // // // //             }
// // // // // // // //         }
// // // // // // // //
// // // // // // // //         /* -------------- FIXED MAVEN STAGE -------------- */
// // // // // // // //
// // // // // // // //         stage('Build with Maven') {
// // // // // // // //
// // // // // // // //             agent {
// // // // // // // //                 docker {
// // // // // // // //                     image 'maven:3.9.8-eclipse-temurin-21'
// // // // // // // //                     args """
// // // // // // // //                         -v ${WORKSPACE}:${WORKSPACE}
// // // // // // // //                         -w ${WORKSPACE}
// // // // // // // //                     """
// // // // // // // //                 }
// // // // // // // //             }
// // // // // // // //
// // // // // // // //             steps {
// // // // // // // //                 sh 'mvn -version'
// // // // // // // //                 sh 'mvn clean package -DskipTests'
// // // // // // // //             }
// // // // // // // //         }
// // // // // // // //
// // // // // // // //         /* ------------------------------------------------ */
// // // // // // // //
// // // // // // // //         stage('Create Namespaces') {
// // // // // // // //             steps {
// // // // // // // //                 sh 'kubectl --insecure-skip-tls-verify apply -f ./namespaces.yaml --validate=false'
// // // // // // // //             }
// // // // // // // //         }
// // // // // // // //
// // // // // // // //         stage('Build Docker Images') {
// // // // // // // //             steps {
// // // // // // // //                 script {
// // // // // // // //                     parallel(
// // // // // // // //                         'accounts-service': { buildAndPush('accounts-service') },
// // // // // // // //                         'blocker-service': { buildAndPush('blocker-service') },
// // // // // // // //                         'cash-service': { buildAndPush('cash-service') },
// // // // // // // //                         'exchange-generator-service': { buildAndPush('exchange-generator-service') },
// // // // // // // //                         'exchange-service': { buildAndPush('exchange-service') },
// // // // // // // //                         'front-ui': { buildAndPush('front-ui') },
// // // // // // // //                         'notifications-service': { buildAndPush('notifications-service') },
// // // // // // // //                         'transfer-service': { buildAndPush('transfer-service') }
// // // // // // // //                     )
// // // // // // // //                 }
// // // // // // // //             }
// // // // // // // //         }
// // // // // // // //
// // // // // // // //         stage('Deploy Databases') {
// // // // // // // //             steps {
// // // // // // // //                 sh 'helm upgrade --install accounts-db ./helm/accounts-db --namespace dev --wait'
// // // // // // // //             }
// // // // // // // //         }
// // // // // // // //
// // // // // // // //         stage('Deploy to Kubernetes') {
// // // // // // // //             steps {
// // // // // // // //                 sh "helm upgrade --install bankapp ${HELM_CHART_PATH} --namespace dev -f ${HELM_CHART_PATH}/values-dev.yaml"
// // // // // // // //             }
// // // // // // // //         }
// // // // // // // //     }
// // // // // // // //
// // // // // // // //     post {
// // // // // // // //         always {
// // // // // // // //             sh 'docker ps -a'
// // // // // // // //         }
// // // // // // // //     }
// // // // // // // // }
// // // // // // // //
// // // // // // // // def buildAndPush(service) {
// // // // // // // //     sh """
// // // // // // // //         docker build -t ${service}:latest -f ${service}/Dockerfile ${service}
// // // // // // // //     """
// // // // // // // // }
// // // // // // // pipeline {
// // // // // // //     agent {
// // // // // // //         docker {
// // // // // // //             image 'jenkins-k8s'
// // // // // // //             args """
// // // // // // //                 -v /Users/maksim/.kube:/var/jenkins_home/.kube:ro
// // // // // // //                 -v /Users/maksim/.minikube:/var/jenkins_home/.minikube:ro
// // // // // // //                 -v /var/run/docker.sock:/var/run/docker.sock
// // // // // // //             """
// // // // // // //         }
// // // // // // //     }
// // // // // // //
// // // // // // //     environment {
// // // // // // //         HELM_CHART_PATH = './helm/bankapp'
// // // // // // //         ORIGINAL_KUBECONFIG = '/var/jenkins_home/.kube/config'
// // // // // // //         KUBECONFIG = '/tmp/kubeconfig'
// // // // // // //         MINIKUBE_HOME = '/var/jenkins_home/.minikube'
// // // // // // //     }
// // // // // // //
// // // // // // //     stages {
// // // // // // //
// // // // // // //         stage('Checkout') {
// // // // // // //             steps {
// // // // // // //                 git url: 'https://github.com/MaximSlepukhin/bankapp.git', branch: 'feature/sprint-10'
// // // // // // //             }
// // // // // // //         }
// // // // // // //
// // // // // // //         stage('Prepare kubeconfig') {
// // // // // // //             steps {
// // // // // // //                 sh '''
// // // // // // //                 cp $ORIGINAL_KUBECONFIG $KUBECONFIG
// // // // // // //                 sed -i "s|/Users/maksim/.minikube|$MINIKUBE_HOME|g" $KUBECONFIG
// // // // // // //                 sed -i "s|127.0.0.1:[0-9]*|host.docker.internal:50049|g" $KUBECONFIG
// // // // // // //                 '''
// // // // // // //             }
// // // // // // //         }
// // // // // // //
// // // // // // //         stage('Check Tools') {
// // // // // // //             steps {
// // // // // // //                 sh 'kubectl version --client'
// // // // // // //                 sh 'helm version'
// // // // // // //                 sh 'docker version'
// // // // // // //             }
// // // // // // //         }
// // // // // // //
// // // // // // //         /* ------------ FIXED MAVEN STAGE ------------ */
// // // // // // //
// // // // // // //         stage('Build with Maven') {
// // // // // // //
// // // // // // //             agent {
// // // // // // //                 docker {
// // // // // // //                     image 'maven:3.9.8-eclipse-temurin-21'
// // // // // // //                     args """
// // // // // // //                         -v ${env.WORKSPACE}:${env.WORKSPACE}
// // // // // // //                         -w ${env.WORKSPACE}
// // // // // // //                     """
// // // // // // //                 }
// // // // // // //             }
// // // // // // //
// // // // // // //             steps {
// // // // // // //                 sh 'mvn -version'
// // // // // // //                 sh 'mvn clean package -DskipTests'
// // // // // // //             }
// // // // // // //         }
// // // // // // //
// // // // // // //         /* ------------------------------------------- */
// // // // // // //
// // // // // // //
// // // // // // //         stage('Create Namespaces') {
// // // // // // //             steps {
// // // // // // //                 sh 'kubectl --insecure-skip-tls-verify apply -f ./namespaces.yaml --validate=false'
// // // // // // //             }
// // // // // // //         }
// // // // // // //
// // // // // // //         /* ------------ FIXED DOCKER BUILD ------------ */
// // // // // // //
// // // // // // //         stage('Build Docker Images') {
// // // // // // //             steps {
// // // // // // //                 script {
// // // // // // //                     parallel(
// // // // // // //                         'accounts-service': { buildAndPush('accounts-service') },
// // // // // // //                         'blocker-service': { buildAndPush('blocker-service') },
// // // // // // //                         'cash-service': { buildAndPush('cash-service') },
// // // // // // //                         'exchange-generator-service': { buildAndPush('exchange-generator-service') },
// // // // // // //                         'exchange-service': { buildAndPush('exchange-service') },
// // // // // // //                         'front-ui': { buildAndPush('front-ui') },
// // // // // // //                         'notifications-service': { buildAndPush('notifications-service') },
// // // // // // //                         'transfer-service': { buildAndPush('transfer-service') }
// // // // // // //                     )
// // // // // // //                 }
// // // // // // //             }
// // // // // // //         }
// // // // // // //
// // // // // // //         /* --------------------------------------------- */
// // // // // // //
// // // // // // //
// // // // // // //         stage('Deploy Databases') {
// // // // // // //             steps {
// // // // // // //                 sh 'helm upgrade --install accounts-db ./helm/accounts-db --namespace dev --wait'
// // // // // // //             }
// // // // // // //         }
// // // // // // //
// // // // // // //         stage('Deploy to Kubernetes') {
// // // // // // //             steps {
// // // // // // //                 sh "helm upgrade --install bankapp ${HELM_CHART_PATH} --namespace dev -f ${HELM_CHART_PATH}/values-dev.yaml"
// // // // // // //             }
// // // // // // //         }
// // // // // // //     }
// // // // // // //
// // // // // // //     post {
// // // // // // //         always {
// // // // // // //             sh 'docker ps -a'
// // // // // // //         }
// // // // // // //     }
// // // // // // // }
// // // // // // //
// // // // // // // /* ==========================================================
// // // // // // //    Правильный Docker build: workspace проброшен в контейнер
// // // // // // //    ========================================================== */
// // // // // // //
// // // // // // // def buildAndPush(service) {
// // // // // // //     sh """
// // // // // // //         docker run --rm \
// // // // // // //             -v ${env.WORKSPACE}:${env.WORKSPACE} \
// // // // // // //             -w ${env.WORKSPACE} \
// // // // // // //             -v /var/run/docker.sock:/var/run/docker.sock \
// // // // // // //             docker:24.0 \
// // // // // // //             docker build -t ${service}:latest -f ${service}/Dockerfile ${service}
// // // // // // //     """
// // // // // // // }
// // // // // // pipeline {
// // // // // //     agent {
// // // // // //         docker {
// // // // // //             image 'jenkins-k8s'
// // // // // //             args """
// // // // // //                 -v /Users/maksim/.kube:/var/jenkins_home/.kube:ro
// // // // // //                 -v /Users/maksim/.minikube:/var/jenkins_home/.minikube:ro
// // // // // //                 -v /var/run/docker.sock:/var/run/docker.sock
// // // // // //             """
// // // // // //         }
// // // // // //     }
// // // // // //
// // // // // //     environment {
// // // // // //         HELM_CHART_PATH = './helm/bankapp'
// // // // // //         ORIGINAL_KUBECONFIG = '/var/jenkins_home/.kube/config'
// // // // // //         KUBECONFIG = '/tmp/kubeconfig'
// // // // // //         MINIKUBE_HOME = '/var/jenkins_home/.minikube'
// // // // // //     }
// // // // // //
// // // // // //     stages {
// // // // // //
// // // // // //         stage('Checkout') {
// // // // // //             steps {
// // // // // //                 git url: 'https://github.com/MaximSlepukhin/bankapp.git', branch: 'feature/sprint-10'
// // // // // //             }
// // // // // //         }
// // // // // //
// // // // // //         stage('Prepare kubeconfig') {
// // // // // //             steps {
// // // // // //                 sh '''
// // // // // //                 cp $ORIGINAL_KUBECONFIG $KUBECONFIG
// // // // // //                 sed -i "s|/Users/maksim/.minikube|$MINIKUBE_HOME|g" $KUBECONFIG
// // // // // //                 sed -i "s|127.0.0.1:[0-9]*|host.docker.internal:50049|g" $KUBECONFIG
// // // // // //                 '''
// // // // // //             }
// // // // // //         }
// // // // // //
// // // // // //         stage('Check Tools') {
// // // // // //             steps {
// // // // // //                 sh 'kubectl version --client'
// // // // // //                 sh 'helm version'
// // // // // //                 sh 'docker version'
// // // // // //             }
// // // // // //         }
// // // // // //
// // // // // //         stage('Build with Maven') {
// // // // // //             agent {
// // // // // //                 docker {
// // // // // //                     image 'maven:3.9.8-eclipse-temurin-21'
// // // // // //                     args """
// // // // // //                         -v ${env.WORKSPACE}:${env.WORKSPACE}
// // // // // //                         -w ${env.WORKSPACE}
// // // // // //                     """
// // // // // //                 }
// // // // // //             }
// // // // // //
// // // // // //             steps {
// // // // // //                 sh 'mvn -version'
// // // // // //                 sh 'mvn clean package -DskipTests'
// // // // // //             }
// // // // // //         }
// // // // // //
// // // // // //         /* ================= DEBUG STAGE ================= */
// // // // // //         stage('Debug Workspace Before Docker Build') {
// // // // // //             steps {
// // // // // //                 sh 'echo "Current workspace: $WORKSPACE"'
// // // // // //                 sh 'ls -1 $WORKSPACE'
// // // // // //                 sh 'ls -1 $WORKSPACE/accounts-service'
// // // // // //             }
// // // // // //         }
// // // // // //         /* =============================================== */
// // // // // //
// // // // // //         stage('Create Namespaces') {
// // // // // //             steps {
// // // // // //                 sh 'kubectl --insecure-skip-tls-verify apply -f ./namespaces.yaml --validate=false'
// // // // // //             }
// // // // // //         }
// // // // // //
// // // // // //         stage('Build Docker Images') {
// // // // // //             steps {
// // // // // //                 script {
// // // // // //                     parallel(
// // // // // //                         'accounts-service': { buildAndPush('accounts-service') },
// // // // // //                         'blocker-service': { buildAndPush('blocker-service') },
// // // // // //                         'cash-service': { buildAndPush('cash-service') },
// // // // // //                         'exchange-generator-service': { buildAndPush('exchange-generator-service') },
// // // // // //                         'exchange-service': { buildAndPush('exchange-service') },
// // // // // //                         'front-ui': { buildAndPush('front-ui') },
// // // // // //                         'notifications-service': { buildAndPush('notifications-service') },
// // // // // //                         'transfer-service': { buildAndPush('transfer-service') }
// // // // // //                     )
// // // // // //                 }
// // // // // //             }
// // // // // //         }
// // // // // //
// // // // // //         stage('Deploy Databases') {
// // // // // //             steps {
// // // // // //                 sh 'helm upgrade --install accounts-db ./helm/accounts-db --namespace dev --wait'
// // // // // //             }
// // // // // //         }
// // // // // //
// // // // // //         stage('Deploy to Kubernetes') {
// // // // // //             steps {
// // // // // //                 sh "helm upgrade --install bankapp ${HELM_CHART_PATH} --namespace dev -f ${HELM_CHART_PATH}/values-dev.yaml"
// // // // // //             }
// // // // // //         }
// // // // // //     }
// // // // // //
// // // // // //     post {
// // // // // //         always {
// // // // // //             sh 'docker ps -a'
// // // // // //         }
// // // // // //     }
// // // // // // }
// // // // // //
// // // // // // /* ==========================================================
// // // // // //    Docker build с пробросом workspace
// // // // // //    ========================================================== */
// // // // // // def buildAndPush(service) {
// // // // // //     sh """
// // // // // //         echo "Building $service in workspace: $WORKSPACE"
// // // // // //         ls -1 $WORKSPACE/$service
// // // // // //         docker run --rm \
// // // // // //             -v ${env.WORKSPACE}:${env.WORKSPACE} \
// // // // // //             -w ${env.WORKSPACE} \
// // // // // //             -v /var/run/docker.sock:/var/run/docker.sock \
// // // // // //             docker:24.0 \
// // // // // //             docker build -t ${service}:latest -f ${service}/Dockerfile ${service}
// // // // // //     """
// // // // // // }
// // // // // pipeline {
// // // // //     agent {
// // // // //         docker {
// // // // //             image 'jenkins-k8s'
// // // // //             args """
// // // // //                 -v /Users/maksim/.kube:/var/jenkins_home/.kube:ro
// // // // //                 -v /Users/maksim/.minikube:/var/jenkins_home/.minikube:ro
// // // // //                 -v /var/run/docker.sock:/var/run/docker.sock
// // // // //             """
// // // // //         }
// // // // //     }
// // // // //
// // // // //     environment {
// // // // //         HELM_CHART_PATH = './helm/bankapp'
// // // // //         ORIGINAL_KUBECONFIG = '/var/jenkins_home/.kube/config'
// // // // //         KUBECONFIG = '/tmp/kubeconfig'
// // // // //         MINIKUBE_HOME = '/var/jenkins_home/.minikube'
// // // // //     }
// // // // //
// // // // //     stages {
// // // // //
// // // // //         stage('Checkout') {
// // // // //             steps {
// // // // //                 git url: 'https://github.com/MaximSlepukhin/bankapp.git', branch: 'feature/sprint-10'
// // // // //             }
// // // // //         }
// // // // //
// // // // //         stage('Prepare kubeconfig') {
// // // // //             steps {
// // // // //                 sh '''
// // // // //                 cp $ORIGINAL_KUBECONFIG $KUBECONFIG
// // // // //                 sed -i "s|/Users/maksim/.minikube|$MINIKUBE_HOME|g" $KUBECONFIG
// // // // //                 sed -i "s|127.0.0.1:[0-9]*|host.docker.internal:50049|g" $KUBECONFIG
// // // // //                 '''
// // // // //             }
// // // // //         }
// // // // //
// // // // //         stage('Check Tools') {
// // // // //             steps {
// // // // //                 sh 'kubectl version --client'
// // // // //                 sh 'helm version'
// // // // //                 sh 'docker version'
// // // // //             }
// // // // //         }
// // // // //
// // // // //         stage('Build with Maven') {
// // // // //             agent {
// // // // //                 docker {
// // // // //                     image 'maven:3.9.8-eclipse-temurin-21'
// // // // //                     args """
// // // // //                         -v ${env.WORKSPACE}:${env.WORKSPACE}
// // // // //                         -w ${env.WORKSPACE}
// // // // //                     """
// // // // //                 }
// // // // //             }
// // // // //
// // // // //             steps {
// // // // //                 sh 'mvn -version'
// // // // //                 sh 'mvn clean package -DskipTests'
// // // // //             }
// // // // //         }
// // // // //
// // // // //         stage('Debug Workspace Before Docker Build') {
// // // // //             steps {
// // // // //                 sh 'echo "Current workspace: $WORKSPACE"'
// // // // //                 sh 'ls -1 $WORKSPACE'
// // // // //                 sh 'ls -1 $WORKSPACE/accounts-service'
// // // // //             }
// // // // //         }
// // // // //
// // // // //         stage('Create Namespaces') {
// // // // //             steps {
// // // // //                 sh 'kubectl --insecure-skip-tls-verify apply -f ./namespaces.yaml --validate=false'
// // // // //             }
// // // // //         }
// // // // //
// // // // //         stage('Build Docker Images') {
// // // // //             steps {
// // // // //                 script {
// // // // //                     parallel(
// // // // //                         'accounts-service': { buildAndPush('accounts-service') },
// // // // //                         'blocker-service': { buildAndPush('blocker-service') },
// // // // //                         'cash-service': { buildAndPush('cash-service') },
// // // // //                         'exchange-generator-service': { buildAndPush('exchange-generator-service') },
// // // // //                         'exchange-service': { buildAndPush('exchange-service') },
// // // // //                         'front-ui': { buildAndPush('front-ui') },
// // // // //                         'notifications-service': { buildAndPush('notifications-service') },
// // // // //                         'transfer-service': { buildAndPush('transfer-service') }
// // // // //                     )
// // // // //                 }
// // // // //             }
// // // // //         }
// // // // //
// // // // //         stage('Deploy Databases') {
// // // // //             steps {
// // // // //                 sh 'helm upgrade --install accounts-db ./helm/accounts-db --namespace dev --wait'
// // // // //             }
// // // // //         }
// // // // //
// // // // //         stage('Deploy to Kubernetes') {
// // // // //             steps {
// // // // //                 sh "helm upgrade --install bankapp ${HELM_CHART_PATH} --namespace dev -f ${HELM_CHART_PATH}/values-dev.yaml"
// // // // //             }
// // // // //         }
// // // // //     }
// // // // //
// // // // //     post {
// // // // //         always {
// // // // //             sh 'docker ps -a'
// // // // //         }
// // // // //     }
// // // // // }
// // // // //
// // // // // /* ==========================================================
// // // // //    Docker build через хостовый Docker (не DinD)
// // // // //    ========================================================== */
// // // // // def buildAndPush(service) {
// // // // //     sh """
// // // // //         echo "Building $service in workspace: $WORKSPACE/$service"
// // // // //         ls -1 $WORKSPACE/$service
// // // // //         docker build -t ${service}:latest -f $WORKSPACE/$service/Dockerfile $WORKSPACE/$service
// // // // //     """
// // // // // }
// // // // pipeline {
// // // //     agent {
// // // //         docker {
// // // //             image 'jenkins-k8s'
// // // //             args """
// // // //                 -v /Users/maksim/.kube:/var/jenkins_home/.kube:ro
// // // //                 -v /Users/maksim/.minikube:/var/jenkins_home/.minikube:ro
// // // //                 -v /var/run/docker.sock:/var/run/docker.sock
// // // //             """
// // // //         }
// // // //     }
// // // //
// // // //     environment {
// // // //         HELM_CHART_PATH = './helm/bankapp'
// // // //         ORIGINAL_KUBECONFIG = '/var/jenkins_home/.kube/config'
// // // //         KUBECONFIG = '/tmp/kubeconfig'
// // // //         MINIKUBE_HOME = '/var/jenkins_home/.minikube'
// // // //     }
// // // //
// // // //     stages {
// // // //
// // // //         stage('Checkout') {
// // // //             steps {
// // // //                 git url: 'https://github.com/MaximSlepukhin/bankapp.git', branch: 'feature/sprint-10'
// // // //             }
// // // //         }
// // // //
// // // //         stage('Prepare kubeconfig') {
// // // //             steps {
// // // //                 sh '''
// // // //                 cp $ORIGINAL_KUBECONFIG $KUBECONFIG
// // // //                 sed -i "s|/Users/maksim/.minikube|$MINIKUBE_HOME|g" $KUBECONFIG
// // // //                 sed -i "s|127.0.0.1:[0-9]*|host.docker.internal:50049|g" $KUBECONFIG
// // // //                 '''
// // // //             }
// // // //         }
// // // //
// // // //         stage('Check Tools') {
// // // //             steps {
// // // //                 sh 'kubectl version --client'
// // // //                 sh 'helm version'
// // // //                 sh 'docker version'
// // // //             }
// // // //         }
// // // //
// // // //         stage('Build with Maven') {
// // // //             agent {
// // // //                 docker {
// // // //                     image 'maven:3.9.8-eclipse-temurin-21'
// // // //                     args """
// // // //                         -v ${env.WORKSPACE}:${env.WORKSPACE}
// // // //                         -w ${env.WORKSPACE}
// // // //                     """
// // // //                 }
// // // //             }
// // // //
// // // //             steps {
// // // //                 sh 'mvn -version'
// // // //                 sh 'mvn clean package -DskipTests'
// // // //             }
// // // //         }
// // // //
// // // //         stage('Debug Workspace Before Docker Build') {
// // // //             steps {
// // // //                 sh 'echo "Current workspace: $WORKSPACE"'
// // // //                 sh 'ls -1 "$WORKSPACE"'
// // // //                 sh 'ls -1 "$WORKSPACE/accounts-service"'
// // // //             }
// // // //         }
// // // //
// // // //         stage('Create Namespaces') {
// // // //             steps {
// // // //                 sh 'kubectl --insecure-skip-tls-verify apply -f ./namespaces.yaml --validate=false'
// // // //             }
// // // //         }
// // // //
// // // //         stage('Build Docker Images') {
// // // //             steps {
// // // //                 script {
// // // //                     parallel(
// // // //                         'accounts-service': { buildAndPush('accounts-service') },
// // // //                         'blocker-service': { buildAndPush('blocker-service') },
// // // //                         'cash-service': { buildAndPush('cash-service') },
// // // //                         'exchange-generator-service': { buildAndPush('exchange-generator-service') },
// // // //                         'exchange-service': { buildAndPush('exchange-service') },
// // // //                         'front-ui': { buildAndPush('front-ui') },
// // // //                         'notifications-service': { buildAndPush('notifications-service') },
// // // //                         'transfer-service': { buildAndPush('transfer-service') }
// // // //                     )
// // // //                 }
// // // //             }
// // // //         }
// // // //
// // // //         stage('Deploy Databases') {
// // // //             steps {
// // // //                 sh 'helm upgrade --install accounts-db ./helm/accounts-db --namespace dev --wait'
// // // //             }
// // // //         }
// // // //
// // // //         stage('Deploy to Kubernetes') {
// // // //             steps {
// // // //                 sh "helm upgrade --install bankapp ${HELM_CHART_PATH} --namespace dev -f ${HELM_CHART_PATH}/values-dev.yaml"
// // // //             }
// // // //         }
// // // //     }
// // // //
// // // //     post {
// // // //         always {
// // // //             sh 'docker ps -a'
// // // //         }
// // // //     }
// // // // }
// // // //
// // // // /* ==========================================================
// // // //    Docker build через хостовый Docker (не DinD)
// // // //    Исправлен путь к JAR для Dockerfile
// // // //    ========================================================== */
// // // // def buildAndPush(service) {
// // // //     def targetJar = "$WORKSPACE/$service/target/${service}-1.0-SNAPSHOT.jar"
// // // //
// // // //     sh """
// // // //         echo "Building $service in workspace: $WORKSPACE/$service"
// // // //         echo "Looking for JAR: $targetJar"
// // // //         if [ ! -f "$targetJar" ]; then
// // // //             echo "ERROR: JAR file not found!"
// // // //             ls -l "$WORKSPACE/$service/target"
// // // //             exit 1
// // // //         fi
// // // //
// // // //         docker build -t ${service}:latest -f "$WORKSPACE/$service/Dockerfile" "$WORKSPACE/$service"
// // // //     """
// // // // }
// // // pipeline {
// // //     agent {
// // //         docker {
// // //             image 'jenkins-k8s'
// // //             args """
// // //                 -v /Users/maksim/.kube:/var/jenkins_home/.kube:ro
// // //                 -v /Users/maksim/.minikube:/var/jenkins_home/.minikube:ro
// // //                 -v /var/run/docker.sock:/var/run/docker.sock
// // //             """
// // //         }
// // //     }
// // //
// // //     environment {
// // //         HELM_CHART_PATH = './helm/bankapp'
// // //         ORIGINAL_KUBECONFIG = '/var/jenkins_home/.kube/config'
// // //         KUBECONFIG = '/tmp/kubeconfig'
// // //         MINIKUBE_HOME = '/var/jenkins_home/.minikube'
// // //     }
// // //
// // //     stages {
// // //
// // //         stage('Clean Workspace') {
// // //             steps {
// // //                 deleteDir()
// // //             }
// // //         }
// // //
// // //         stage('Checkout') {
// // //             steps {
// // //                 git url: 'https://github.com/MaximSlepukhin/bankapp.git', branch: 'feature/sprint-10'
// // //             }
// // //         }
// // //
// // //         stage('Prepare kubeconfig') {
// // //             steps {
// // //                 sh '''
// // //                 cp $ORIGINAL_KUBECONFIG $KUBECONFIG
// // //                 sed -i "s|/Users/maksim/.minikube|$MINIKUBE_HOME|g" $KUBECONFIG
// // //                 sed -i "s|127.0.0.1:[0-9]*|host.docker.internal:50049|g" $KUBECONFIG
// // //                 '''
// // //             }
// // //         }
// // //
// // //         stage('Check Tools') {
// // //             steps {
// // //                 sh 'kubectl version --client'
// // //                 sh 'helm version'
// // //                 sh 'docker version'
// // //             }
// // //         }
// // //
// // //         stage('Build with Maven') {
// // //             agent {
// // //                 docker {
// // //                     image 'maven:3.9.8-eclipse-temurin-21'
// // //                     args """
// // //                         -v ${env.WORKSPACE}:${env.WORKSPACE}
// // //                         -w ${env.WORKSPACE}
// // //                     """
// // //                 }
// // //             }
// // //
// // //             steps {
// // //                 sh 'mvn -version'
// // //                 sh 'mvn clean package -DskipTests'
// // //             }
// // //         }
// // //
// // //         stage('Debug Workspace Before Docker Build') {
// // //             steps {
// // //                 sh 'echo "Current workspace: $WORKSPACE"'
// // //                 sh 'ls -1 "$WORKSPACE"'
// // //                 sh 'ls -1 "$WORKSPACE/accounts-service"'
// // //             }
// // //         }
// // //
// // //         stage('Create Namespaces') {
// // //             steps {
// // //                 sh 'kubectl --insecure-skip-tls-verify apply -f ./namespaces.yaml --validate=false'
// // //             }
// // //         }
// // //
// // //         stage('Build Docker Images') {
// // //             steps {
// // //                 script {
// // //                     parallel(
// // //                         'accounts-service': { buildAndPush('accounts-service') },
// // //                         'blocker-service': { buildAndPush('blocker-service') },
// // //                         'cash-service': { buildAndPush('cash-service') },
// // //                         'exchange-generator-service': { buildAndPush('exchange-generator-service') },
// // //                         'exchange-service': { buildAndPush('exchange-service') },
// // //                         'front-ui': { buildAndPush('front-ui') },
// // //                         'notifications-service': { buildAndPush('notifications-service') },
// // //                         'transfer-service': { buildAndPush('transfer-service') }
// // //                     )
// // //                 }
// // //             }
// // //         }
// // //
// // //         stage('Deploy Databases') {
// // //             steps {
// // //                 sh 'helm upgrade --install accounts-db ./helm/accounts-db --namespace dev --wait'
// // //             }
// // //         }
// // //
// // //         stage('Deploy to Kubernetes') {
// // //             steps {
// // //                 sh "helm upgrade --install bankapp ${HELM_CHART_PATH} --namespace dev -f ${HELM_CHART_PATH}/values-dev.yaml"
// // //             }
// // //         }
// // //     }
// // //
// // //     post {
// // //         always {
// // //             sh 'docker ps -a'
// // //         }
// // //     }
// // // }
// // //
// // // /* ==========================================================
// // //    Docker build через хостовый Docker (не DinD)
// // //    ========================================================== */
// // // def buildAndPush(service) {
// // //     def targetJar = "$WORKSPACE/$service/target/${service}-1.0-SNAPSHOT.jar"
// // //
// // //     sh """
// // //         echo "Building $service in workspace: $WORKSPACE/$service"
// // //         echo "Looking for JAR: $targetJar"
// // //         if [ ! -f "$targetJar" ]; then
// // //             echo "ERROR: JAR file not found!"
// // //             ls -l "$WORKSPACE/$service/target"
// // //             exit 1
// // //         fi
// // //
// // //         docker build -t ${service}:latest -f "$WORKSPACE/$service/Dockerfile" "$WORKSPACE/$service"
// // //     """
// // // }
// // pipeline {
// //     agent {
// //         docker {
// //             image 'jenkins-k8s'
// //             args """
// //                 -v /Users/maksim/.kube:/var/jenkins_home/.kube:ro
// //                 -v /Users/maksim/.minikube:/var/jenkins_home/.minikube:ro
// //                 -v /var/run/docker.sock:/var/run/docker.sock
// //             """
// //         }
// //     }
// //
// //     environment {
// //         HELM_CHART_PATH = './helm/bankapp'
// //         ORIGINAL_KUBECONFIG = '/var/jenkins_home/.kube/config'
// //         KUBECONFIG = '/tmp/kubeconfig'
// //         MINIKUBE_HOME = '/var/jenkins_home/.minikube'
// //     }
// //
// //     stages {
// //
// //         stage('Clean Workspace') {
// //             steps {
// //                 deleteDir()
// //             }
// //         }
// //
// //         stage('Checkout') {
// //             steps {
// //                 git url: 'https://github.com/MaximSlepukhin/bankapp.git', branch: 'feature/sprint-10'
// //             }
// //         }
// //
// //         stage('Prepare kubeconfig') {
// //             steps {
// //                 sh '''
// //                 cp $ORIGINAL_KUBECONFIG $KUBECONFIG
// //                 sed -i "s|/Users/maksim/.minikube|$MINIKUBE_HOME|g" $KUBECONFIG
// //                 sed -i "s|127.0.0.1:[0-9]*|host.docker.internal:50049|g" $KUBECONFIG
// //                 '''
// //             }
// //         }
// //
// //         stage('Check Tools') {
// //             steps {
// //                 sh 'kubectl version --client'
// //                 sh 'helm version'
// //                 sh 'docker version'
// //             }
// //         }
// //
// //         stage('Build with Maven') {
// //             agent {
// //                 docker {
// //                     image 'maven:3.9.8-eclipse-temurin-21'
// //                     args """
// //                         -v ${env.WORKSPACE}:${env.WORKSPACE}
// //                         -w ${env.WORKSPACE}
// //                     """
// //                 }
// //             }
// //
// //             steps {
// //                 sh 'mvn -version'
// //                 sh 'mvn clean package -DskipTests'
// //             }
// //         }
// //
// //         stage('Debug Workspace Before Docker Build') {
// //             steps {
// //                 sh '''
// //                 echo "Current workspace: $WORKSPACE"
// //                 ls -l "$WORKSPACE"
// //                 ls -l "$WORKSPACE/accounts-service"
// //                 '''
// //             }
// //         }
// //
// //         stage('Create Namespaces') {
// //             steps {
// //                 sh 'kubectl --insecure-skip-tls-verify apply -f ./namespaces.yaml --validate=false'
// //             }
// //         }
// //
// //         stage('Build Docker Images') {
// //             steps {
// //                 script {
// //                     parallel(
// //                         'accounts-service': { buildAndPush('accounts-service') },
// //                         'blocker-service': { buildAndPush('blocker-service') },
// //                         'cash-service': { buildAndPush('cash-service') },
// //                         'exchange-generator-service': { buildAndPush('exchange-generator-service') },
// //                         'exchange-service': { buildAndPush('exchange-service') },
// //                         'front-ui': { buildAndPush('front-ui') },
// //                         'notifications-service': { buildAndPush('notifications-service') },
// //                         'transfer-service': { buildAndPush('transfer-service') }
// //                     )
// //                 }
// //             }
// //         }
// //
// //         stage('Deploy Databases') {
// //             steps {
// //                 sh 'helm upgrade --install accounts-db ./helm/accounts-db --namespace dev --wait'
// //             }
// //         }
// //
// //         stage('Deploy to Kubernetes') {
// //             steps {
// //                 sh "helm upgrade --install bankapp ${HELM_CHART_PATH} --namespace dev -f ${HELM_CHART_PATH}/values-dev.yaml"
// //             }
// //         }
// //     }
// //
// //     post {
// //         always {
// //             sh 'docker ps -a'
// //         }
// //     }
// // }
// //
// // /* ==========================================================
// //    Docker build через хостовый Docker (не DinD)
// //    Добавлена отладка пути к JAR
// //    ========================================================== */
// // def buildAndPush(service) {
// //     def targetJar = "${env.WORKSPACE}/${service}/target/${service}-1.0-SNAPSHOT.jar"
// //
// //     sh """
// //         echo "Building $service in workspace: $WORKSPACE/$service"
// //         echo "Looking for JAR: $targetJar"
// //         if [ ! -f "$targetJar" ]; then
// //             echo "ERROR: JAR file not found!"
// //             echo "Listing target directory:"
// //             ls -l "$WORKSPACE/$service/target" || true
// //             exit 1
// //         fi
// //
// //         docker build -t ${service}:latest -f "$WORKSPACE/$service/Dockerfile" "$WORKSPACE/$service"
// //     """
// // }
// pipeline {
//     agent {
//         docker {
//             image 'jenkins-k8s'
//             args """
//                 -v /Users/maksim/.kube:/var/jenkins_home/.kube:ro
//                 -v /Users/maksim/.minikube:/var/jenkins_home/.minikube:ro
//                 -v /var/run/docker.sock:/var/run/docker.sock
//             """
//         }
//     }
//
//     environment {
//         HELM_CHART_PATH = './helm/bankapp'
//         ORIGINAL_KUBECONFIG = '/var/jenkins_home/.kube/config'
//         KUBECONFIG = '/tmp/kubeconfig'
//         MINIKUBE_HOME = '/var/jenkins_home/.minikube'
//     }
//
//     stages {
//
//         stage('Clean Workspace') {
//             steps {
//                 deleteDir()
//             }
//         }
//
//         stage('Checkout') {
//             steps {
//                 git url: 'https://github.com/MaximSlepukhin/bankapp.git', branch: 'feature/sprint-10'
//             }
//         }
//
//         stage('Prepare kubeconfig') {
//             steps {
//                 sh '''
//                 cp $ORIGINAL_KUBECONFIG $KUBECONFIG
//                 sed -i "s|/Users/maksim/.minikube|$MINIKUBE_HOME|g" $KUBECONFIG
//                 sed -i "s|127.0.0.1:[0-9]*|host.docker.internal:50049|g" $KUBECONFIG
//                 '''
//             }
//         }
//
//         stage('Check Tools') {
//             steps {
//                 sh 'kubectl version --client'
//                 sh 'helm version'
//                 sh 'docker version'
//             }
//         }
//
//         stage('Build with Maven') {
//             agent {
//                 docker {
//                     image 'maven:3.9.8-eclipse-temurin-21'
//                     args """
//                         -v ${env.WORKSPACE}:${env.WORKSPACE}
//                         -w ${env.WORKSPACE}
//                     """
//                 }
//             }
//
//             steps {
//                 sh 'mvn -version'
//                 sh 'mvn clean package -DskipTests'
//             }
//         }
//
//         stage('Debug Workspace Before Docker Build') {
//             steps {
//                 sh '''
//                 echo "Current workspace: $WORKSPACE"
//                 ls -l "$WORKSPACE"
//                 ls -l "$WORKSPACE/accounts-service"
//                 '''
//             }
//         }
//
//         stage('Create Namespaces') {
//             steps {
//                 sh 'kubectl --insecure-skip-tls-verify apply -f ./namespaces.yaml --validate=false'
//             }
//         }
//
//         stage('Build Docker Images') {
//             steps {
//                 script {
//                     parallel(
//                         'accounts-service': { buildAndPush('accounts-service') },
//                         'blocker-service': { buildAndPush('blocker-service') },
//                         'cash-service': { buildAndPush('cash-service') },
//                         'exchange-generator-service': { buildAndPush('exchange-generator-service') },
//                         'exchange-service': { buildAndPush('exchange-service') },
//                         'front-ui': { buildAndPush('front-ui') },
//                         'notifications-service': { buildAndPush('notifications-service') },
//                         'transfer-service': { buildAndPush('transfer-service') }
//                     )
//                 }
//             }
//         }
//
//         stage('Deploy Databases') {
//             steps {
//                 sh 'helm upgrade --install accounts-db ./helm/accounts-db --namespace dev --wait'
//             }
//         }
//
//         stage('Deploy to Kubernetes') {
//             steps {
//                 sh "helm upgrade --install bankapp ${HELM_CHART_PATH} --namespace dev -f ${HELM_CHART_PATH}/values-dev.yaml"
//             }
//         }
//     }
//
//     post {
//         always {
//             sh 'docker ps -a'
//         }
//     }
// }
//
// /* ==========================================================
//    Docker build через хостовый Docker (не DinD)
//    Добавлена отладка пути к JAR
//    ========================================================== */
// def buildAndPush(service) {
//     def targetJar = "${env.WORKSPACE}/${service}/target/${service}-1.0-SNAPSHOT.jar"
//
//     sh """
//         echo "Building $service in workspace: $WORKSPACE/$service"
//         echo "Looking for JAR: $targetJar"
//         if [ ! -f "$targetJar" ]; then
//             echo "ERROR: JAR file not found!"
//             echo "Listing target directory:"
//             ls -l "$WORKSPACE/$service/target" || true
//             exit 1
//         fi
//
//         docker build -t ${service}:latest -f "$WORKSPACE/$service/Dockerfile" "$WORKSPACE/$service"
//     """
// }
pipeline {
    agent {
        docker {
            image 'jenkins-k8s'
            args """
                -v /Users/maksim/.kube:/var/jenkins_home/.kube:ro
                -v /Users/maksim/.minikube:/var/jenkins_home/.minikube:ro
                -v /var/run/docker.sock:/var/run/docker.sock
            """
        }
    }

    environment {
        HELM_CHART_PATH = './helm/bankapp'
        ORIGINAL_KUBECONFIG = '/var/jenkins_home/.kube/config'
        KUBECONFIG = '/tmp/kubeconfig'
        MINIKUBE_HOME = '/var/jenkins_home/.minikube'
    }

    stages {

        stage('Clean Workspace') {
            steps {
                deleteDir()
            }
        }

        stage('Checkout') {
            steps {
                git url: 'https://github.com/MaximSlepukhin/bankapp.git', branch: 'feature/sprint-10'
            }
        }

        stage('Prepare kubeconfig') {
            steps {
                sh '''
                cp $ORIGINAL_KUBECONFIG $KUBECONFIG
                sed -i "s|/Users/maksim/.minikube|$MINIKUBE_HOME|g" $KUBECONFIG
                sed -i "s|127.0.0.1:[0-9]*|host.docker.internal:50049|g" $KUBECONFIG
                '''
            }
        }

        stage('Check Tools') {
            steps {
                sh 'kubectl version --client'
                sh 'helm version'
                sh 'docker version'
            }
        }

        stage('Build with Maven') {
            agent {
                docker {
                    image 'maven:3.9.8-eclipse-temurin-21'
                    args """
                        -v ${env.WORKSPACE}:${env.WORKSPACE}
                        -w ${env.WORKSPACE}
                    """
                }
            }

            steps {
                sh 'mvn -version'
                sh 'mvn clean package -DskipTests'
                sh 'ls -l $WORKSPACE'
                sh 'chmod -R 777 $WORKSPACE/*'
            }
        }

        stage('Debug Workspace Before Docker Build') {
            steps {
                sh '''
                echo "Current workspace: $WORKSPACE"
                ls -l "$WORKSPACE"
                ls -l "$WORKSPACE/accounts-service"
                '''
            }
        }

        stage('Create Namespaces') {
            steps {
                sh 'kubectl --insecure-skip-tls-verify apply -f ./namespaces.yaml --validate=false'
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    parallel(
                        'accounts-service': { buildAndPush('accounts-service') },
                        'blocker-service': { buildAndPush('blocker-service') },
                        'cash-service': { buildAndPush('cash-service') },
                        'exchange-generator-service': { buildAndPush('exchange-generator-service') },
                        'exchange-service': { buildAndPush('exchange-service') },
                        'front-ui': { buildAndPush('front-ui') },
                        'notifications-service': { buildAndPush('notifications-service') },
                        'transfer-service': { buildAndPush('transfer-service') }
                    )
                }
            }
        }

        stage('Deploy Databases') {
            steps {
                sh 'helm upgrade --install accounts-db ./helm/accounts-db --namespace dev --wait'
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh "helm upgrade --install bankapp ${HELM_CHART_PATH} --namespace dev -f ${HELM_CHART_PATH}/values-dev.yaml"
            }
        }
    }

    post {
        always {
            sh 'docker ps -a'
        }
    }
}

/* ==========================================================
   Docker build через хостовый Docker (не DinD)
   Добавлена отладка пути к JAR
   ========================================================== */
def buildAndPush(service) {
    def targetJar = "${env.WORKSPACE}/${service}/target/${service}-1.0-SNAPSHOT.jar"

    sh """
        echo "Building $service in workspace: $WORKSPACE/$service"
        echo "Looking for JAR: $targetJar"
        if [ ! -f "$targetJar" ]; then
            echo "ERROR: JAR file not found!"
            echo "Listing target directory:"
            ls -l "$WORKSPACE/$service/target" || true
            exit 1
        fi

        docker build -t ${service}:latest -f "$WORKSPACE/$service/Dockerfile" "$WORKSPACE/$service"
    """
}
