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

                        echo "--> Borrando la mierda esa <--"
                        sh "if [ -d nginx.conf ]; then rm -rf nginx.conf; fi"

                        sh "ls -l"

                        echo "--> Levantando nueva versión..."
                        sh "docker-compose up -d --build"
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