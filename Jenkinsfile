pipeline {
    agent any
    tools {
        jdk "JDK 8"        
    }
    stages {
        stage('Test') {
            steps {
                sh './gradlew clean build publish jacocoTestReport coveralls --stacktrace --info'
            }
        }
    }
}

