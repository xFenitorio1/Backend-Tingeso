pipeline {
    agent any

    triggers {
        // Revisa GitHub cada 2 minutos (ajusta según prefieras)
        pollSCM 'H/2 * * * *' 
    }

    environment {
        // Nombre de tu imagen local
        BACKEND_IMAGE = 'backend-1:latest'
    }

    stages {
        stage('Descargar Código') {
            steps {
                // Esto descarga tu rama principal
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
                    // NO usamos --no-cache para que sea rápido
                    sh "docker build -t ${BACKEND_IMAGE} ."
                }
            }
        }

        stage('Desplegar Localmente') {
            steps {
                script {
                    echo "--> Actualizando el contenedor en Docker Compose..."
                    // Solo refresca el servicio 'backend' definido en tu docker-compose.yml
                    // El flag --no-deps evita que se reinicie la base de datos
                    sh "cd .. && docker-compose up -d --no-deps backend"
                }
            }
        }

        stage('Limpieza') {
            steps {
                // Borra imágenes viejas sin nombre para no llenar el disco
                sh 'docker image prune -f'
            }
        }
    }
}