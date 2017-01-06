node {
   function mvn()
   stage('Preparation') {
      checkout scm
      sh "./mvnw --batch-mode package -DskipTests"
   }
   stage('Build') {
      sh "./mvnw clean verify"
   }
   stage('Results') {
      junit '**/target/surefire-reports/TEST-*.xml'
      archive 'target/*.jar'
   }
}
