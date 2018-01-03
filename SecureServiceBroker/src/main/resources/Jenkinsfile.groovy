#!/usr/bin/groovy

node { 
    stage ('create service') {
        echo 'Hello World2' 
        def username = 'Jenkins'
        echo 'Hello Mr. ${username}'
        echo "I said, Hello Mr. ${username}"    

        def accessToken = "55044249b6efeaa6ff383df3ac3709824ba51f79438ef5aa57b134e381120c78"
        
        def serviceName = "test72"
        def ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services.xml\""
        def createServiceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + serviceName + "\" " + ampURL + " >out.txt"
        echo " createServiceCurl: ${createServiceCurl}"
        
        sh "${createServiceCurl}"
    }

    stage ('create application plan'){
       //def externalMethod = load("externalMethod.groovy")
    }
}
