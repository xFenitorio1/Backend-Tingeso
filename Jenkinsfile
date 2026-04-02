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
                // Esto "conecta" tus secretos de Jenkins con variables que el comando sh puede usar
                withCredentials([
                    string(credentialsId: 'db-user', variable: 'DB_USER'),
                    string(credentialsId: 'db-password', variable: 'DB_PASSWORD')
                ]) {
                    script {
                        echo "--> Actualizando el contenedor de forma segura..."
                        // Las variables $DB_USER y $DB_PASSWORD llenarán los huecos ${...} del docker-compose.yml
                        sh "docker-compose up -d --build --no-deps backend"
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