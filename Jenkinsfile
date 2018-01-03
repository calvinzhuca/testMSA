#!/usr/bin/groovy

node { 

    def accessToken = "55044249b6efeaa6ff383df3ac3709824ba51f79438ef5aa57b134e381120c78"
    def ampURL = ""
    def serviceCurl = ""

    stage ('clean services') {
        // Git checkout before load source the file
        checkout scm
        // To know files are checked out or not
        sh "ls -lhrt"
        def rootDir = pwd()
        println("Current Directory: " + rootDir)

        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services.xml\""
        serviceCurl = "curl -v -k -X GET -d \"access_token=" + accessToken + "\" " + ampURL + " >out_listService.txt"
        //echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"
        
        def listServiceReply = new File('/var/lib/jenkins/workspace/testPipeline/out_listService.txt').text
        //echo "listServiceReply: ${listServiceReply}"  
        

        def ReadIdHelper = load("ReadIdHelper.groovy")
        
        //just get 1 service id
        def serviceId = Integer.parseInt(ReadIdHelper.getServiceId2(listServiceReply))
        echo "serviceId ${serviceId}"
        
        //use for loop clean the rest 10 service, should be enough for the lab env
        for(int i = serviceId; i < serviceId + 10; ++ i) {
            echo "here ${i}"
            ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + i + ".xml\""
            serviceCurl = "curl -v -k -X DELETE -d \"access_token=" + accessToken + "\" " + ampURL + " >>out_deleteService.txt"
            //echo "serviceCurl: ${serviceCurl}"   
            sh "${serviceCurl}"
        }         
        
    }
    
    
    stage ('create service') {
       
        def serviceName = "printPhoto"
        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services.xml\""
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + serviceName + "\" " + ampURL + " >out_createService.txt"
        //echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"
    }
    

    stage ('create application plan'){
        def createServiceReply = new File('/var/lib/jenkins/workspace/testPipeline/out_createService.txt').text
        echo "createServiceReply: ${createServiceReply}"

        def ReadIdHelper = load("ReadIdHelper.groovy")
        def serviceId = ReadIdHelper.getServiceId(createServiceReply)
        echo "serviceId ${serviceId}"

        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=plan1\" \"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + serviceId + "/application_plans.xml\"  > out_createApplicationPlan.txt "
        //echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"      
        
        
        //API integration
        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + serviceId + "/proxy.xml\""
        serviceCurl = "curl -v -k -X PATCH -d \"access_token=" + accessToken + "&api_backend=https%3A%2F%2Fgoogle.com\" " + ampURL + " >out_integration.txt"
        echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"      
    
    }
    

}
