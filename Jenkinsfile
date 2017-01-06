node {
   stage('Preparation') {
      git 'https://github.com/lkwg82/de.lgohlke.selenium-pageobjects'
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
