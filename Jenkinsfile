//  pipeline {
//         agent any
//         parameters {
//             string(name: 'myInput', description: 'Some pipeline parameters')
//         }
//         stages {
//             stage('Stage one') {
//                 steps {
//                     script {
//                         echo "Parameter from template creation: "
//                     }
//                 }
//             }
//             stage('Stage two') {
//                 steps {
//                     script {
//                         echo "Job input parameter: " + params.myInput
//                     }
//                 }
//             }
//         }
//     }

pipeline {
    agent any
    stages {
        stage('SCM') {
            steps {
                git branch: 'master',
                    credentialsId: '80aacc35-c23b-4702-a14e-3aec5b463f0b', 
                    url: 'https://github.com/AldroidOng/OrderTrackingDemoApp.git'
            }
        }
        stage('build && SonarQube analysis') {
            steps {
                echo "hello there"
                script {
                          def scannerHome = tool 'sonar-scanner';
                          withSonarQubeEnv() {
                          bat "${scannerHome}/bin/sonar-scanner"
                            }
                }


                // withSonarQubeEnv('My SonarQube Server') {
                //     // Optionally use a Maven environment you've configured already
                //     // withMaven(maven:'Maven 3.5') {
                //     //     sh 'mvn clean package sonar:sonar'
                //     // }
                // }
            }
        }
        stage("Quality Gate") {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    // def qg = waitForQualityGate()
                    // if (qg.status != 'OK') {
                    //     error "Pipeline aborted due to quality gate failure: ${qg.status}"
                    //     abortPipeline: true
                    // Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
                    // true = set pipeline to UNSTABLE, false = don't
                    waitForQualityGate abortPipeline: true
                    }
 

                }
        }
    }
}
    