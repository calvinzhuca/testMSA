#!/usr/bin/groovy

node { 

    def accessToken = "55044249b6efeaa6ff383df3ac3709824ba51f79438ef5aa57b134e381120c78"

    stage ('clean services') {

        def ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services.xml\""
        def listServiceCurl = "curl -v -k -X GET -d \"access_token=" + accessToken + "\" " + ampURL + " >out_listService.txt"
        echo " listServiceCurl: ${listServiceCurl}"
        
        sh "${listServiceCurl}"
        
        def listServiceReply = new File('/var/lib/jenkins/workspace/testPipeline/out_listService.txt').text
        echo "listServiceReply: ${listServiceReply}"  
        
        // Git checkout before load source the file
        checkout scm
        // To know files are checked out or not
        sh "ls -lhrt"
        def rootDir = pwd()
        println("Current Directory: " + rootDir)

        def ReadIdHelper = load("ReadIdHelper.groovy")
        
        //just get 1 service id
        def serviceId = Integer.parseInt(ReadIdHelper.getServiceId2(listServiceReply))
        echo "serviceId2 ${serviceId}"
        
        //use a for loop clean the rest 10 service
        for(int i = serviceId; i < serviceId + 10; ++ i) {
            echo "here ${i}"
            ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + i + ".xml\""
            def deleteServiceCurl = "curl -v -k -X DELETE -d \"access_token=" + accessToken + "\" " + ampURL + " >>out_deleteService.txt"
            echo "deleteServiceCurl: ${deleteServiceCurl}"   
            sh "${deleteServiceCurl}"
        }         
        
    }
    
    
    stage ('create service') {

        
        def serviceName = "printPhoto"
        def ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services.xml\""
        def createServiceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + serviceName + "\" " + ampURL + " >out_createService.txt"
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


        def createServiceReply = new File('/var/lib/jenkins/workspace/testPipeline/out_createService.txt').text
        echo "createServiceReply: ${createServiceReply}"

        // Load the file 'externalMethod.groovy' from the current directory, into a variable called "externalMethod".
        //def externalMethod = load("externalMethod.groovy")
        // Call the method we defined in externalMethod.
        //externalMethod.lookAtThis("Steve")
        
        
        echo 'Here1' 
        def ReadIdHelper = load("ReadIdHelper.groovy")
        def serviceId = ReadIdHelper.getServiceId(createServiceReply)
        echo "serviceId ${serviceId}"

        def createApplicationPlanCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=plan1\" \"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + serviceId + "/application_plans.xml\"  > out_createApplicationPlan.txt "
        echo " createServiceCurl: ${createApplicationPlanCurl}"
        
        sh "${createApplicationPlanCurl}"      

    }
    
    

}
