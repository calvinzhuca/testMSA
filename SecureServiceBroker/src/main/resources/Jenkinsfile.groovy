#!/usr/bin/groovy

node { 

    /*
    stage ('create service') {
    echo 'Hello World2' 
    def username = 'Jenkins'
    echo 'Hello Mr. ${username}'
    echo "I said, Hello Mr. ${username}"    

    def accessToken = "55044249b6efeaa6ff383df3ac3709824ba51f79438ef5aa57b134e381120c78"
        
    def serviceName = "test73"
    def ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services.xml\""
    def createServiceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + serviceName + "\" " + ampURL + " >out.txt"
    echo " createServiceCurl: ${createServiceCurl}"
        
    sh "${createServiceCurl}"
    }
     */

    stage ('create application plan'){
        echo 'Here11' 
        def createServiceReply = new File('/var/lib/jenkins/workspace/testPipeline/out.txt').text
        echo "createServiceReply: ${createServiceReply}"
        def ReadIdHelper = load("/var/lib/jenkins/workspace/testPipeline/ReadIdHelper.groovy")
        echo 'Here12' 
        def serviceId = ReadIdHelper.getServiceId(createServiceReply)
        echo "serviceId ${serviceId}"
    }
    
    

}