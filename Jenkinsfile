pipeline {
    agent any

    environment {
        MAVEN_HOME = tool 'Maven_3.9.9'  // Имя Maven в Jenkins Global Tool Configuration
        KUBECTL    = tool 'kubectl'
        HELM       = tool 'helm'
        DOCKER     = tool 'docker'
        MINIKUBE   = tool 'minikube'
        NAMESPACE  = "dev"
    }

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
                    $DOCKER --version
                    $HELM version --short
                    $KUBECTL version --client
                    $MAVEN_HOME/bin/mvn -v
                    $MINIKUBE version
                '''
            }
        }

        stage('Delete Elasticsearch') {
            steps {
                sh '''
                    $HELM uninstall elasticsearch --namespace default || echo "Elasticsearch not found"
                '''
            }
        }

        stage('Delete Logstash') {
            steps {
                sh '''
                    $HELM uninstall logstash --namespace default || echo "Logstash not found"
                '''
            }
        }

        stage('Delete Kibana') {
            steps {
                sh '''
                    $HELM uninstall kibana --namespace default || echo "Kibana not found"
                '''
            }
        }

        stage('Add Helm Repos Elastic') {
            steps {
                sh '''
                    $HELM repo add elastic https://helm.elastic.co
                    $HELM repo update
                '''
            }
        }

        stage('Deploy Elasticsearch') {
            steps {
                sh '''
                    $HELM upgrade --install elasticsearch elastic/elasticsearch \
                      --namespace default \
                      -f helm/bankapp/charts/elasticsearch/values.yaml \
                      --wait --timeout 600s
                '''
            }
        }

        stage('Deploy Logstash') {
            steps {
                sh '''
                    $HELM upgrade --install logstash elastic/logstash \
                      --namespace default \
                      -f helm/bankapp/charts/logstash/values.yaml \
                      --wait --timeout 300s
                '''
            }
        }

        stage('Deploy Kibana') {
            steps {
                sh '''
                    $HELM upgrade --install kibana elastic/kibana \
                      --namespace default \
                      -f helm/bankapp/charts/kibana/values.yaml \
                      --wait --timeout 300s
                '''
            }
        }

        stage('Deploy Kafka (PLAINTEXT, 1 broker)') {
            steps {
                sh '''
                    $HELM upgrade --install my-kafka bitnami/kafka \
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
                    $KUBECTL wait --for=condition=Ready pod -l app.kubernetes.io/name=kafka -n default --timeout=300s
                '''
            }
        }

        stage('Create Kafka Client Pod') {
            steps {
                sh '''
                    $KUBECTL delete pod kafka-client -n default --ignore-not-found
                    $KUBECTL run kafka-client --restart=Never --image=bitnamilegacy/kafka:4.0.0 --namespace default -- sleep infinity
                    $KUBECTL wait --for=condition=Ready pod/kafka-client -n default --timeout=180s
                '''
            }
        }

        stage('Create Kafka Topics') {
            steps {
                script {
                    def topics = ["exchange-rates", "notifications", "logs"]
                    topics.each { topic ->
                        sh """
                            $KUBECTL exec -n default kafka-client -- kafka-topics.sh \
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
                    services.each { s ->
                        sh """
                            cd ${s}
                            $MAVEN_HOME/bin/mvn clean package -DskipTests
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

                    def minikubeEnv = sh(script: '$MINIKUBE docker-env --shell bash', returnStdout: true).trim()
                    def buildCmd = "${minikubeEnv}\n"
                    services.each { s ->
                        buildCmd += "docker build -t ${s}:latest -f ${s}/Dockerfile ${s}\n"
                    }
                    sh buildCmd
                }
            }
        }

        stage('Deploy Databases with Helm') {
            steps {
                script {
                    ["accounts-db", "keycloak-db"].each { db ->
                        sh "$HELM upgrade --install ${db} ./helm/bankapp/charts/${db} --namespace $NAMESPACE --create-namespace --wait --timeout 300s"
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
                        "transfer-service"
                    ]
                    services.each { s ->
                        sh """
                            $KUBECTL delete pod -l app.kubernetes.io/name=${s} -n $NAMESPACE --ignore-not-found
                            $KUBECTL delete pod -l app=${s} -n $NAMESPACE --ignore-not-found
                        """
                    }
                }
            }
        }

        stage('Deploy Microservices with Helm') {
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
                        "keycloak"
                    ]
                    services.each { s ->
                        sh "$HELM upgrade --install ${s} ./helm/bankapp/charts/${s} --namespace $NAMESPACE --wait --timeout 300s"
                    }
                }
            }
        }

        stage('Deploy Zipkin with Helm') {
            steps {
                sh "$HELM upgrade --install zipkin ./helm/bankapp/charts/zipkin --namespace monitoring --create-namespace --wait --timeout 300s"
            }
        }

        stage('Wait for Zipkin Ready') {
            steps {
                sh "$KUBECTL wait --for=condition=Ready pod -l app=zipkin -n monitoring --timeout=300s"
            }
        }

        stage('Access Information') {
            steps {
                sh '''
                    echo "=================================================="
                    echo "Application access information"
                    echo "Keycloak: kubectl port-forward -n dev svc/keycloak 8080:80"
                    echo "Front UI: kubectl port-forward -n dev svc/front-ui 8081:8080"
                    echo "Zipkin: kubectl port-forward -n monitoring svc/zipkin 9411:9411"
                    echo "Prometheus: kubectl port-forward -n monitoring svc/prometheus-stack-kube-prom-prometheus 9090:9090"
                    echo "Grafana: kubectl port-forward -n monitoring svc/grafana 3000:80"
                    echo "=================================================="
                '''
            }
        }
    }

    post {
        success { echo "Pipeline завершён успешно." }
        failure { echo "Pipeline завершился с ошибкой." }
    }
}