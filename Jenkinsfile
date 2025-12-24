pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                git url: 'https://github.com/MaximSlepukhin/bankapp.git', branch: 'feature/sprint-12'
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
                    /opt/homebrew/bin/minikube version
                '''
            }
        }

        stage('Deploy Kafka (PLAINTEXT, 1 broker)') {
            steps {
                sh '''
                    echo "Deploying Kafka..."
                    /opt/homebrew/bin/helm upgrade --install my-kafka bitnami/kafka \
                      --namespace default \
                      --set image.repository=bitnamilegacy/kafka \
                      --set image.tag=4.0.0 \
                      --set replicaCount=1 \
                      --set auth.enabled=false \
                      --set kraft.enabled=false \
                      --set zookeeper.enabled=true \
                      --set listeners.client.protocol=PLAINTEXT \
                      --set listeners.interbroker.protocol=PLAINTEXT \
                      --wait --timeout 300s
                '''
            }
        }

        stage('Wait for Kafka Ready') {
            steps {
                sh '''
                    echo "Waiting for Kafka pod to be Ready..."
                    /usr/local/bin/kubectl wait \
                      --for=condition=Ready pod \
                      -l app.kubernetes.io/name=kafka \
                      -n default \
                      --timeout=300s
                '''
            }
        }

        stage('Create Kafka Client Pod') {
            steps {
                sh '''
                    /usr/local/bin/kubectl delete pod kafka-client -n default --ignore-not-found
                    /usr/local/bin/kubectl run kafka-client \
                      --restart=Never \
                      --image=bitnamilegacy/kafka:4.0.0 \
                      --namespace default \
                      -- sleep infinity
                    /usr/local/bin/kubectl wait \
                      --for=condition=Ready pod/kafka-client \
                      -n default \
                      --timeout=180s
                '''
            }
        }

        stage('Create Kafka Topics') {
            steps {
                script {
                    def topics = ["exchange-rates", "notifications"]
                    topics.each { topic ->
                        sh """
                            echo "Creating topic ${topic}..."
                            /usr/local/bin/kubectl exec -n default kafka-client -- \
                              kafka-topics.sh \
                                --bootstrap-server my-kafka.default.svc.cluster.local:9092 \
                                --create --if-not-exists \
                                --topic ${topic} \
                                --partitions 1 \
                                --replication-factor 1
                        """
                    }
                }
            }
        }

        stage('Build With Maven') {
            steps {
                script {
                    [
                        "accounts-service",
                        "blocker-service",
                        "cash-service",
                        "exchange-generator-service",
                        "exchange-service",
                        "front-ui",
                        "notifications-service",
                        "transfer-service"
                    ].each { service ->
                        sh """
                            cd ${service}
                            /Users/maksim/apps/apache-maven-3.9.9/bin/mvn clean package -DskipTests
                            cd ..
                        """
                    }
                }
            }
        }

        stage('Build Docker Images in Minikube') {
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

                    def minikubeEnv = sh(
                        script: '/opt/homebrew/bin/minikube docker-env --shell bash',
                        returnStdout: true
                    ).trim()

                    def buildCmd = "${minikubeEnv}\n"
                    services.each { service ->
                        buildCmd += "docker build -t ${service}:latest -f ${service}/Dockerfile ${service}\n"
                    }

                    sh buildCmd
                }
            }
        }

        stage('Deploy Databases with Helm') {
            steps {
                script {
                    ["accounts-db", "keycloak-db"].each { db ->
                        sh """
                          /opt/homebrew/bin/helm upgrade --install ${db} \
                            ./helm/bankapp/charts/${db} \
                            --namespace dev --create-namespace \
                            --wait --timeout 300s
                        """
                    }
                }
            }
        }

        stage('Recreate Microservice Pods') {
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
                        "transfer-service",
                    ]

                    services.each { service ->
                        sh """
                            echo "Deleting pods for ${service}..."
                            /usr/local/bin/kubectl delete pod -l app.kubernetes.io/name=${service} -n dev --ignore-not-found
                            /usr/local/bin/kubectl delete pod -l app=${service} -n dev --ignore-not-found
                        """
                    }
                }
            }
        }

        stage('Deploy Microservices with Helm') {
            steps {
                script {
                    [
                        "accounts-service",
                        "blocker-service",
                        "cash-service",
                        "exchange-generator-service",
                        "exchange-service",
                        "front-ui",
                        "notifications-service",
                        "transfer-service",
                        "keycloak"
                    ].each { service ->
                        sh """
                          /opt/homebrew/bin/helm upgrade --install ${service} \
                            ./helm/bankapp/charts/${service} \
                            --namespace dev --wait --timeout 300s
                        """
                    }
                }
            }
        }

        stage('Deploy Zipkin with Helm') {
            steps {
                sh '''
                    echo "Deploying Zipkin..."
                    /opt/homebrew/bin/helm upgrade --install zipkin \
                      ./helm/bankapp/charts/zipkin \
                      --namespace monitoring \
                      --create-namespace \
                      --wait --timeout 300s
                '''
            }
        }

        stage('Wait for Zipkin Ready') {
            steps {
                sh '''
                    echo "Waiting for Zipkin pod to be Ready..."
                    /usr/local/bin/kubectl wait \
                      --for=condition=Ready pod \
                      -l app=zipkin \
                      -n monitoring \
                      --timeout=300s
                '''
            }
        }

        stage('Add Helm Repos') {
            steps {
                sh '''
                    echo "Adding Helm repos..."

                    /opt/homebrew/bin/helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
                    /opt/homebrew/bin/helm repo add grafana https://grafana.github.io/helm-charts
                    /opt/homebrew/bin/helm repo add elastic https://helm.elastic.co

                    /opt/homebrew/bin/helm repo update
                '''
            }
        }

        stage('Deploy Elasticsearch') {
            steps {
                sh '''
                    echo "Deploying Elasticsearch..."

                    /opt/homebrew/bin/helm upgrade --install elasticsearch elastic/elasticsearch \
                      -n monitoring --create-namespace \
                      -f helm/bankapp/charts/elasticsearch/values.yaml \
                      --wait --timeout 300s
                '''
            }
        }

        stage('Install Prometheus Stack') {
            steps {
                sh '''
                    echo "Installing kube-prometheus-stack..."
                    /opt/homebrew/bin/helm upgrade --install prometheus-stack prometheus-community/kube-prometheus-stack \
                      -n monitoring --create-namespace --wait --timeout 300s
                '''
            }
        }

        stage('Install Grafana with dashboards') {
            steps {
                sh '''
                    echo "Installing Grafana..."
                    /opt/homebrew/bin/helm upgrade --install grafana grafana/grafana \
                      -n monitoring --create-namespace \
                      -f ./helm/bankapp/charts/grafana/values.yaml \
                      --wait --timeout 300s
                '''
            }
        }

        stage('Apply ServiceMonitor') {
            steps {
                sh '''
                    echo "Applying ServiceMonitor..."
                    /usr/local/bin/kubectl apply -f helm/bankapp/charts/prometheus/templates/servicemonitor.yaml
                '''
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
