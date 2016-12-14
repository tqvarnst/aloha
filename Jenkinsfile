node {
    def mvnHome = tool 'M3'
    def javaHome = tool 'jdk8'
    
    stage ('Git checkout'){
        echo 'Checking out git repository'
        checkout scm
    }

    stage ('Build project with Maven'){
        echo 'Building project'
        sh "${mvnHome}/bin/mvn package"
    }

    stage ('Build image and deploy in Dev'){
        echo 'Building docker image and deploying to Dev'
        buildApp('helloworld-msa-dev', "aloha")
        // Get the build number
        BUILD_NUMBER = sh (
            script: "oc get bc/aloha --no-headers=true| awk '{print \$4}'",
            returnStdout: true
        ).trim()
        echo "This is the build number: ${BUILD_NUMBER}"
    }

    stage ('Automated tests'){
        echo 'This stage simulates automated tests'
        sh "${mvnHome}/bin/mvn -B -Dmaven.test.failure.ignore verify"
    }

    stage ('Deploy to QA'){
        echo 'Deploying to QA'
        promoteImage('helloworld-msa-dev', 'helloworld-msa-qa', 'aloha', 'latest')
    }

    stage ('Wait for approval'){
        input 'Approve to production?'
    }

    stage ('Deploy to production'){
        echo 'Deploying to production'
        promoteImage('helloworld-msa-dev', 'helloworld-msa', 'aloha', BUILD_NUMBER)
        canaryDeploy('aloha', BUILD_NUMBER)
    }
}

// Login and set the project
def projectSet(String project){
    //Use a credential called openshift-dev
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'openshift-dev', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
        sh "oc login --insecure-skip-tls-verify=true -u $env.USERNAME -p $env.PASSWORD $OPENSHIFT"
    }
    sh "oc new-project ${project} || echo 'Project exists'"
    sh "oc project ${project}"
}


// Creates a Build and triggers it
def buildApp(String project, String app){
    projectSet(project)
    sh "oc new-build --binary --name=${app} -l app=${app} || echo 'Build exists'"
    sh "oc start-build ${app} --from-dir=. --follow"
    deployApp(app)
}

// Tag the ImageStream from an original project to force a deployment
def promoteImage(String origProject, String project, String app, String tag){
    projectSet(project)
    sh "oc policy add-role-to-user system:image-puller system:serviceaccount:${project}:default -n ${origProject}"
    sh "oc tag ${origProject}/${app}:latest ${project}/${app}:${tag}"
    deployApp(app)
}

// Deploy the project based on a existing ImageStream
def deployApp(String app){
    sh "oc new-app ${app} -l app=${app},hystrix.enabled=true || echo 'Aplication already Exists'"
    sh "oc expose service ${app} || echo 'Service already exposed'"
    sh "oc patch dc/${app} -p '{\"spec\":{\"template\":{\"spec\":{\"containers\":[{\"name\":\"${app}\",\"ports\":[{\"containerPort\": 8778,\"name\":\"jolokia\"}]}]}}}}'"
    sh "oc patch dc/${app} -p '{\"spec\":{\"template\":{\"spec\":{\"containers\":[{\"name\":\"${app}\",\"readinessProbe\":{\"httpGet\":{\"path\":\"/api/health\",\"port\":8080}}}]}}}}\'"
}

def canaryDeploy(String app, String version){
    sh "oc new-app --name ${app}-${version} ${app}:${version} -l app=${app},svc=${app}-canary,hystrix.enabled=true"
    sh "oc patch dc/${app} -p '{\"spec\":{\"template\":{\"metadata\":{\"labels\":{\"svc\":\"${app}-canary\"}}}}}'"
    sh "oc patch svc/${app} -p '{\"spec\":{\"selector\":{\"svc\":\"${app}-canary\",\"app\": null, \"deploymentconfig\": null}, \"sessionAffinity\":\"ClientIP\"}}'"
}
