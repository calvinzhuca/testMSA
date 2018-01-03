#!/usr/bin/groovy

node { 

    
    stage ('create service') {

    def accessToken = "55044249b6efeaa6ff383df3ac3709824ba51f79438ef5aa57b134e381120c78"
        
    def serviceName = "test74"
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
        
        
        echo 'Here123456' 
        def ReadIdHelper = load("ReadIdHelper.groovy")
        def returnStr = ReadIdHelper.getServiceId(createServiceReply)
        echo "serviceId ${returnStr}"
        //def serviceId = ReadIdHelper.getServiceId(createServiceReply)
        //echo "serviceId ${serviceId}"
        /*
        // point to exact source file
        
        
        
        //def ReadIdHelper = load("/var/lib/jenkins/workspace/testPipeline/ReadIdHelper.groovy")
        echo 'Here12' 
        def serviceId = ReadIdHelper.getServiceId(createServiceReply)
        echo "serviceId ${serviceId}"
         */
    }
    
    

}
