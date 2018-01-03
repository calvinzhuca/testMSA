#!/usr/bin/groovy

node { 

    def accessToken = "55044249b6efeaa6ff383df3ac3709824ba51f79438ef5aa57b134e381120c78"
    
    stage ('create service') {

        
        def serviceName = "test75"
        def ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services.xml\""
        def createServiceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + serviceName + "\" " + ampURL + " >out.txt"
        echo " createServiceCurl: ${createServiceCurl}"
        
        sh "${createServiceCurl}"
    }
    

    stage ('create application plan'){
        // Git checkout before load source the file
        checkout scm

        // To know files are checked out or not
        sh "ls -lhrt"
        def rootDir = pwd()
        println("Current Directory: " + rootDir)


        def createServiceReply = new File('/var/lib/jenkins/workspace/testPipeline/out.txt').text
        echo "createServiceReply: ${createServiceReply}"

        // Load the file 'externalMethod.groovy' from the current directory, into a variable called "externalMethod".
        //def externalMethod = load("externalMethod.groovy")
        // Call the method we defined in externalMethod.
        //externalMethod.lookAtThis("Steve")
        
        
        echo 'Here1234567' 
        def ReadIdHelper = load("ReadIdHelper.groovy")
        def serviceId = ReadIdHelper.getServiceId(createServiceReply)
        echo "serviceId ${serviceId}"

        def createApplicationPlanCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=plan1\" \"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + servicId + "/application_plans.xml\"  > out.txt "
        shell(createApplicationPlanCurl)         

    }
    
    

}
