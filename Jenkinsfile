pipeline {
    agent any

    triggers {
        pollSCM 'H/2 * * * *' 
    }

    environment {
        // Nombre de tu imagen local
        BACKEND_IMAGE = 'backend-1:latest'
    }

    stages {
        stage('Descargar Código') {
            steps {
                checkout scm
            }
        }

        stage('Compilar y Probar (Maven)') {
            steps {
                // Ejecuta los tests antes de construir la imagen
                sh 'chmod +x mvnw'

                sh './mvnw clean test -DskipTests'
            }
        }

        stage('Construir Imagen Docker') {
            steps {
                script {
                    echo "--> Construyendo imagen del Backend..."
                    sh "docker build -t ${BACKEND_IMAGE} ."
                }
            }
        }

        stage('Desplegar Localmente') {
            steps {
                withCredentials([
                    string(credentialsId: 'db-user', variable: 'DB_USER'),
                    string(credentialsId: 'db-password', variable: 'DB_PASSWORD')
                ]) {
                    script {
                        echo "--> Limpiando contenedores antiguos y liberando puertos..."
                        sh "docker-compose down --remove-orphans"

                        echo "--> Levantando nueva versión..."
                        sh "docker-compose up -d --build --scale backend=3"

                        echo "--> config del nginx"
                        sh "docker exec balanceador-de-carga nginx -T"

                        echo "--> Esperando 20 segundos a que Keycloak inicie completamente..."
                        sh "sleep 90"

                        echo "--> Viendo si funciona el tema del keycloak"
                        sh "docker exec balanceador-de-carga curl http://keycloak:8080/realms/Tingeso/.well-known/openid-configuration"
                    }
                }
            }
        }

        stage('Limpieza') {
            steps {
                sh 'docker image prune -f'
            }
        }
    }
}