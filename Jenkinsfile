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
// // // //         stage('Clean Workspace') {
// // // //             steps { deleteDir() }
// // // //         }
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
// // // //             steps {
// // // //                 sh 'mvn -version'
// // // //                 sh 'mvn clean package -DskipTests'
// // // //                 sh 'chmod -R 777 $WORKSPACE/*'
// // // //             }
// // // //         }
// // // //
// // // //         stage('Debug Target JARs') {
// // // //             steps {
// // // //                 sh '''
// // // //                 echo "Listing all target directories and JAR files:"
// // // //                 for dir in "$WORKSPACE"/*/target; do
// // // //                     if [ -d "$dir" ]; then
// // // //                         echo "Contents of $dir:"
// // // //                         ls -l "$dir"
// // // //                     fi
// // // //                 done
// // // //                 '''
// // // //             }
// // // //         }
// // // //
// // // //         stage('Build Docker Images in Minikube') {
// // // //             steps {
// // // //                 script {
// // // //                     // Настраиваем Docker для использования Minikube daemon
// // // //                     sh 'eval $(minikube -p minikube docker-env)'
// // // //
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
// // // //                 sh 'helm upgrade --install accounts-db ./helm/bankapp/charts/accounts-db --namespace dev --wait --kube-insecure-skip-tls-verify'
// // // //             }
// // // //         }
// // // //
// // // //         stage('Deploy to Kubernetes') {
// // // //             steps {
// // // //                 sh "helm upgrade --install bankapp ${HELM_CHART_PATH} --namespace dev -f ${HELM_CHART_PATH}/values-dev.yaml --kube-insecure-skip-tls-verify"
// // // //             }
// // // //         }
// // // //     }
// // // //
// // // //     post {
// // // //         always { sh 'docker ps -a' }
// // // //     }
// // // // }
// // // //
// // // // /* ==========================================================
// // // //    buildAndPush для Minikube Docker daemon
// // // //    ========================================================== */
// // // // def buildAndPush(service) {
// // // //     sh """
// // // //         echo "Building Docker image for $service in Minikube"
// // // //
// // // //         workspace="/var/jenkins_home/workspace/BankCICD@2"
// // // //         jarPath="\$workspace/$service/target/${service}-1.0-SNAPSHOT.jar"
// // // //
// // // //         if [ ! -f "\$jarPath" ]; then
// // // //             echo "ERROR: JAR not found for $service at \$jarPath!"
// // // //             exit 1
// // // //         fi
// // // //
// // // //         cp "\$jarPath" "\$workspace/$service/app.jar"
// // // //
// // // //         docker build -t ${service}:latest "\$workspace/$service"
// // // //         echo "Docker image ${service}:latest built successfully in Minikube"
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
// // //         DOCKER_TAR_DIR = '/tmp/docker-tars'
// // //         HARDCODED_WORKSPACE = '/var/jenkins_home/workspace/BankCICD@2'
// // //     }
// // //
// // //     stages {
// // //
// // //         stage('Clean Workspace') {
// // //             steps { deleteDir() }
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
// // //             steps {
// // //                 sh 'mvn -version'
// // //                 sh 'mvn clean package -DskipTests'
// // //                 sh 'chmod -R 777 $WORKSPACE/*'
// // //             }
// // //         }
// // //
// // //         stage('Debug Target JARs') {
// // //             steps {
// // //                 sh '''
// // //                 echo "Listing all target directories and JAR files:"
// // //                 for dir in "$HARDCODED_WORKSPACE"/*/target; do
// // //                     if [ -d "$dir" ]; then
// // //                         echo "Contents of $dir:"
// // //                         ls -l "$dir"
// // //                     fi
// // //                 done
// // //                 '''
// // //             }
// // //         }
// // //
// // //         stage('Build Docker Images and Load to Minikube') {
// // //             steps {
// // //                 script {
// // //                     // Указываем правильный workspace
// // //                     def workspace = "/var/jenkins_home/workspace/BankAppCICD@2"
// // //                     def DOCKER_TAR_DIR = "/tmp/docker-tars"
// // //
// // //                     def services = [
// // //                         'accounts-service',
// // //                         'blocker-service',
// // //                         'cash-service',
// // //                         'exchange-generator-service',
// // //                         'exchange-service',
// // //                         'front-ui',
// // //                         'notifications-service',
// // //                         'transfer-service'
// // //                     ]
// // //
// // //                     sh "mkdir -p ${DOCKER_TAR_DIR}"
// // //
// // //                     for (svc in services) {
// // //                         sh """
// // //                             echo "Building Docker image for ${svc}"
// // //
// // //                             jarPath=${workspace}/${svc}/target/${svc}-1.0-SNAPSHOT.jar
// // //
// // //                             if [ ! -f "\${jarPath}" ]; then
// // //                                 echo "ERROR: JAR not found for ${svc} at \${jarPath}!"
// // //                                 exit 1
// // //                             fi
// // //
// // //                             cp "\${jarPath}" "${workspace}/${svc}/app.jar"
// // //                             docker build -t ${svc}:latest "${workspace}/${svc}"
// // //                             docker save -o ${DOCKER_TAR_DIR}/${svc}.tar ${svc}:latest
// // //                             echo "Docker image ${svc}:latest saved to tar"
// // //                         """
// // //
// // //                         sh "docker exec minikube docker load -i ${DOCKER_TAR_DIR}/${svc}.tar"
// // //                     }
// // //                 }
// // //             }
// // //         }
// // //
// // //         stage('Deploy Databases') {
// // //             steps {
// // //                 sh 'helm upgrade --install accounts-db ./helm/bankapp/charts/accounts-db --namespace dev --wait --kube-insecure-skip-tls-verify'
// // //             }
// // //         }
// // //
// // //         stage('Deploy to Kubernetes') {
// // //             steps {
// // //                 sh "helm upgrade --install bankapp ${HELM_CHART_PATH} --namespace dev -f ${HELM_CHART_PATH}/values-dev.yaml --kube-insecure-skip-tls-verify"
// // //             }
// // //         }
// // //     }
// // //
// // //     post {
// // //         always { sh 'docker ps -a' }
// // //     }
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
// //         HARDCODED_WORKSPACE = '/var/jenkins_home/workspace/BankAppCICD@2'
// //     }
// //
// //     stages {
// //
// //         stage('Clean Workspace') {
// //             steps { deleteDir() }
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
// //             steps {
// //                 sh 'mvn -version'
// //                 sh 'mvn clean package -DskipTests'
// //                 sh 'chmod -R 777 $WORKSPACE/*'
// //             }
// //         }
// //
// //         stage('Debug Target JARs') {
// //             steps {
// //                 sh '''
// //                 echo "Listing all target directories and JAR files:"
// //                 for dir in "$HARDCODED_WORKSPACE"/*/target; do
// //                     if [ -d "$dir" ]; then
// //                         echo "Contents of $dir:"
// //                         ls -l "$dir"
// //                     fi
// //                 done
// //                 '''
// //             }
// //         }
// //
// //         stage('Set Docker env for Minikube') {
// //             steps {
// //                 sh '''
// //                 echo "Switching Docker to Minikube daemon..."
// //                 eval $(minikube -p minikube docker-env)
// //                 docker info
// //                 '''
// //             }
// //         }
// //
// //         stage('Build Docker Images') {
// //             steps {
// //                 script {
// //                     def workspace = "/var/jenkins_home/workspace/BankAppCICD@2"
// //
// //                     def services = [
// //                         'accounts-service',
// //                         'blocker-service',
// //                         'cash-service',
// //                         'exchange-generator-service',
// //                         'exchange-service',
// //                         'front-ui',
// //                         'notifications-service',
// //                         'transfer-service'
// //                     ]
// //
// //                     for (svc in services) {
// //                         sh """
// //                             echo "Building Docker image for ${svc}"
// //
// //                             jarPath=${workspace}/${svc}/target/${svc}-1.0-SNAPSHOT.jar
// //
// //                             if [ ! -f "\${jarPath}" ]; then
// //                                 echo "ERROR: JAR not found for ${svc} at \${jarPath}!"
// //                                 exit 1
// //                             fi
// //
// //                             cp "\${jarPath}" "${workspace}/${svc}/app.jar"
// //                             docker build -t ${svc}:latest "${workspace}/${svc}"
// //                             echo "Docker image ${svc}:latest built in Minikube Docker"
// //                         """
// //                     }
// //                 }
// //             }
// //         }
// //
// //         stage('Deploy Databases') {
// //             steps {
// //                 sh 'helm upgrade --install accounts-db ./helm/bankapp/charts/accounts-db --namespace dev --wait --kube-insecure-skip-tls-verify'
// //             }
// //         }
// //
// //         stage('Deploy to Kubernetes') {
// //             steps {
// //                 sh "helm upgrade --install bankapp ${HELM_CHART_PATH} --namespace dev -f ${HELM_CHART_PATH}/values-dev.yaml --kube-insecure-skip-tls-verify"
// //             }
// //         }
// //     }
// //
// //     post {
// //         always { sh 'docker ps -a' }
// //     }
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
//         HARDCODED_WORKSPACE = '/var/jenkins_home/workspace/BankAppCICD@2'
//     }
//
//     stages {
//
//         stage('Clean Workspace') {
//             steps { deleteDir() }
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
//                 export KUBECONFIG=$KUBECONFIG
//                 kubectl get nodes
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
//             steps {
//                 sh 'mvn -version'
//                 sh 'mvn clean package -DskipTests'
//                 sh 'chmod -R 777 $WORKSPACE/*'
//             }
//         }
//
//         stage('Debug Target JARs') {
//             steps {
//                 sh '''
//                 echo "Listing all target directories and JAR files:"
//                 for dir in "$HARDCODED_WORKSPACE"/*/target; do
//                     if [ -d "$dir" ]; then
//                         echo "Contents of $dir:"
//                         ls -l "$dir"
//                     fi
//                 done
//                 '''
//             }
//         }
//
//         stage('Set Docker env for Minikube') {
//             steps {
//                 sh '''
//                 echo "Switching Docker to Minikube daemon..."
//                 eval $(minikube -p minikube docker-env)
//                 docker info
//                 '''
//             }
//         }
//
//         stage('Build Docker Images') {
//             steps {
//                 script {
//                     def workspace = "/var/jenkins_home/workspace/BankAppCICD@2"
//
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
//                     for (svc in services) {
//                         sh """
//                             echo "Building Docker image for ${svc}"
//
//                             jarPath=${workspace}/${svc}/target/${svc}-1.0-SNAPSHOT.jar
//
//                             if [ ! -f "\${jarPath}" ]; then
//                                 echo "ERROR: JAR not found for ${svc} at \${jarPath}!"
//                                 exit 1
//                             fi
//
//                             cp "\${jarPath}" "${workspace}/${svc}/app.jar"
//                             docker build -t ${svc}:latest "${workspace}/${svc}"
//                             echo "Docker image ${svc}:latest built in Minikube Docker"
//                         """
//                     }
//                 }
//             }
//         }
//
//         stage('Deploy Databases') {
//             steps {
//                 sh 'helm upgrade --install accounts-db ./helm/bankapp/charts/accounts-db --namespace dev --wait --kube-insecure-skip-tls-verify'
//             }
//         }
//
//         stage('Deploy to Kubernetes') {
//             steps {
//                 sh "helm upgrade --install bankapp ${HELM_CHART_PATH} --namespace dev -f ${HELM_CHART_PATH}/values-dev.yaml --kube-insecure-skip-tls-verify"
//             }
//         }
//     }
//
//     post {
//         always { sh 'docker ps -a' }
//     }
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
        HARDCODED_WORKSPACE = '/var/jenkins_home/workspace/BankAppCICD@2'
    }

    stages {

        stage('Clean Workspace') {
            steps { deleteDir() }
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
                export KUBECONFIG=$KUBECONFIG
                kubectl get nodes
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
                sh 'chmod -R 777 $WORKSPACE/*'
            }
        }

        stage('Debug Target JARs') {
            steps {
                sh '''
                echo "Listing all target directories and JAR files:"
                for dir in "$HARDCODED_WORKSPACE"/*/target; do
                    if [ -d "$dir" ]; then
                        echo "Contents of $dir:"
                        ls -l "$dir"
                    fi
                done
                '''
            }
        }

        stage('Build Docker Images in Minikube') {
            steps {
                script {
                    def workspace = "/var/jenkins_home/workspace/BankAppCICD@2"

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

                    for (svc in services) {
                        sh """
                        echo "Switching Docker to Minikube daemon and building image for ${svc}..."
                        eval \$(minikube -p minikube docker-env)

                        jarPath=${workspace}/${svc}/target/${svc}-1.0-SNAPSHOT.jar

                        if [ ! -f "\${jarPath}" ]; then
                            echo "ERROR: JAR not found for ${svc} at \${jarPath}!"
                            exit 1
                        fi

                        cp "\${jarPath}" "${workspace}/${svc}/app.jar"
                        docker build -t ${svc}:latest "${workspace}/${svc}"
                        echo "Docker image ${svc}:latest built in Minikube Docker"
                        """
                    }
                }
            }
        }

        stage('Verify Images in Minikube') {
            steps {
                sh '''
                echo "Verifying that Docker images exist in Minikube..."
                eval $(minikube -p minikube docker-env)
                for img in accounts-service blocker-service cash-service exchange-generator-service exchange-service front-ui notifications-service transfer-service; do
                    if ! docker images | grep -q "$img"; then
                        echo "ERROR: Docker image $img not found in Minikube!"
                        exit 1
                    fi
                done
                echo "All Docker images are present in Minikube."
                '''
            }
        }

        stage('Deploy Databases') {
            steps {
                sh 'helm upgrade --install accounts-db ./helm/bankapp/charts/accounts-db --namespace dev --wait --kube-insecure-skip-tls-verify'
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh "helm upgrade --install bankapp ${HELM_CHART_PATH} --namespace dev -f ${HELM_CHART_PATH}/values-dev.yaml --kube-insecure-skip-tls-verify"
            }
        }
    }

    post {
        always { sh 'docker ps -a' }
    }
}
