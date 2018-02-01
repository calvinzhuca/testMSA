#!/usr/bin/groovy

node { 

    def ampURL = ""
    def serviceCurl = ""
    def OC_HOME = "/home/czhu/works/ocClient" 
    def planId
    def planId2
    def serviceId
    checkout scm
    def accessToken
    withCredentials([string(credentialsId: '3SCALE_ACCESS_TOKEN', variable: 'token')]) {
        accessToken = token
    }
    

        
    /*
    This is a working parallel test with stage inside
    parallel (
    phase1: { stage('11'){sh "echo p1; sleep 20s; echo phase1"} },
    phase2: { stage('22'){sh "echo p2; sleep 40s; echo phase2"}}
    )
     */



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
        
        def listServiceReply = new File("${WORKSPACE}/out_listService.txt").text
        echo "listServiceReply: ${listServiceReply}"  
        

        def ReadIdHelper = load("ReadIdHelper.groovy")
        
        //just get 1 service id
        serviceId = Integer.parseInt(ReadIdHelper.getServiceId2(listServiceReply))
        echo "serviceId ${serviceId}"
        
        if (serviceId > 0)
        {
            //use for loop clean the rest 10 service, should be enough for the lab env
            for(int i = serviceId; i < serviceId + 10; ++ i) {
                echo "here ${i}"
                ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + i + ".xml\""
                serviceCurl = "curl -v -k -X DELETE -d \"access_token=" + accessToken + "\" " + ampURL + " >>out_deleteService.txt"
                //echo "serviceCurl: ${serviceCurl}"   
                sh "${serviceCurl}"
            }     
        }else{
            println("no need to clean service, only 1 left")
        }    
        
    }
    

    stage ('create service1') {
        println("create service ----------------------------------")
        def serviceName = "printPhotoMSA"
        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services.xml\""
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + serviceName + "\" " + ampURL + " >out_createService.txt"
        //echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"

        //create application plan
        println("create application plan  ----------------------------------")
        def createServiceReply = new File("${WORKSPACE}/out_createService.txt").text
        echo "createServiceReply: ${createServiceReply}"

        def ReadIdHelper = load("ReadIdHelper.groovy")
        serviceId = ReadIdHelper.getServiceId(createServiceReply)
        echo "serviceId ${serviceId}"

        def planName = "fourInchPlan"
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + planName + "\" \"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + serviceId + "/application_plans.xml\"  > out_createApplicationPlan.txt "
        //echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"      

        
        planName = "tenInchPlan"
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + planName + "\" \"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + serviceId + "/application_plans.xml\"  > out_createApplicationPlan.txt "
        //echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"   
        
       
        def createPlanReply = new File("${WORKSPACE}/out_createApplicationPlan.txt").text
        planId = ReadIdHelper.getPlanId(createPlanReply)
        echo "planId ${planId}"
        
        //API integration
        println("API integration  ----------------------------------")
        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + serviceId + "/proxy.xml\""
        serviceCurl = "curl -v -k -X PATCH -d \"access_token=" + accessToken + "&api_backend=https%3A%2F%2Fgoogle.com\" " + ampURL + " >out_integration.txt"
        echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"  
        
        //create application
        println("create application  ----------------------------------")
        def applicationName = "printPhotoApp"
        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/accounts/4/applications.xml\""
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + applicationName + "&plan_id=" + planId + "&description=" + applicationName + "\" " + ampURL + " >out_integration.txt"
        echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"  
     
    }
    
    stage ('create service2') {
        println("create service ----------------------------------")
        def serviceName = "buyTicket"
        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services.xml\""
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + serviceName + "\" " + ampURL + " >out_createService.txt"
        //echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"

        //create application plan
        println("create application plan  ----------------------------------")
        def createServiceReply = new File("${WORKSPACE}/out_createService.txt").text
        echo "createServiceReply: ${createServiceReply}"

        def ReadIdHelper = load("ReadIdHelper.groovy")
        serviceId = ReadIdHelper.getServiceId(createServiceReply)
        echo "serviceId ${serviceId}"

        def planName = "seasonTicketPlan"
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + planName + "\" \"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + serviceId + "/application_plans.xml\"  > out_createApplicationPlan.txt "
        //echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"      

        def createPlanReply = new File("${WORKSPACE}/out_createApplicationPlan.txt").text
        planId = ReadIdHelper.getPlanId(createPlanReply)
        echo "planId ${planId}"
        
        //API integration
        println("API integration  ----------------------------------")
        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + serviceId + "/proxy.xml\""
        serviceCurl = "curl -v -k -X PATCH -d \"access_token=" + accessToken + "&api_backend=https%3A%2F%2Fgoogle.com\" " + ampURL + " >out_integration.txt"
        echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"  
        
        //create application
        println("create application  ----------------------------------")
        def applicationName = "buyTicketApp"
        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/accounts/4/applications.xml\""
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + applicationName + "&plan_id=" + planId + "&description=" + applicationName + "\" " + ampURL + " >out_integration.txt"
        echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"  
     
    }
        
    stage ('create service3') {
        println("create service ----------------------------------")
        def serviceName = "orderPizza"
        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services.xml\""
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + serviceName + "\" " + ampURL + " >out_createService.txt"
        //echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"

        //create application plan
        println("create application plan  ----------------------------------")
        def createServiceReply = new File("${WORKSPACE}/out_createService.txt").text
        echo "createServiceReply: ${createServiceReply}"

        def ReadIdHelper = load("ReadIdHelper.groovy")
        serviceId = ReadIdHelper.getServiceId(createServiceReply)
        echo "serviceId ${serviceId}"

        def planName = "smallPizza"
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + planName + "\" \"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + serviceId + "/application_plans.xml\"  > out_createApplicationPlan.txt "
        //echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"      

        planName = "largePizza"
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + planName + "\" \"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + serviceId + "/application_plans.xml\"  > out_createApplicationPlan.txt "
        //echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"      
        
        
        def createPlanReply = new File("${WORKSPACE}/out_createApplicationPlan.txt").text
        planId = ReadIdHelper.getPlanId(createPlanReply)
        echo "planId ${planId}"
        
        //API integration
        println("API integration  ----------------------------------")
        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + serviceId + "/proxy.xml\""
        serviceCurl = "curl -v -k -X PATCH -d \"access_token=" + accessToken + "&api_backend=https%3A%2F%2Fgoogle.com\" " + ampURL + " >out_integration.txt"
        echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"  
        
        //create application
        println("create application  ----------------------------------")
        def applicationName = "orderPizzaApp"
        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/accounts/4/applications.xml\""
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + applicationName + "&plan_id=" + planId + "&description=" + applicationName + "\" " + ampURL + " >out_integration.txt"
        echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"  
     
    }


    /*
    stage ('OCP depolyment') {
        
    def OC_HOME = "/home/czhu/works/ocClient"    
    withEnv(["PATH+OC=${OC_HOME}"]) {
    sh "${OC_HOME}/oc projects "
    }

    withMaven(
    // Maven installation declared in the Jenkins "Global Tool Configuration"
    maven: 'maven 3.3.3',
    // Maven settings.xml file defined with the Jenkins Config File Provider Plugin
    // Maven settings and global settings can also be defined in Jenkins Global Tools Configuration
    //mavenSettingsConfig: 'my-maven-settings',
    mavenLocalRepo: '.repository') {

    // Run the maven build
    sh "mvn clean package"

    } // withMaven will discover the generated Maven artifacts, JUnit Surefire & FailSafe & FindBugs reports...
    }    

    
    stage ('Recreate Broker') {
    //API integration
    println("---------------------------------- Recreate Broker  ----------------------------------")
    //delete the old three-scale application first
    withEnv(["PATH+OC=${OC_HOME}"]) {
    try {
    sh "${OC_HOME}/oc delete ClusterServiceBroker 3scale-broker"
    sh "sleep 5"
    sh "${OC_HOME}/oc get ClusterServiceBroker"
    }catch(err) {
    echo "!!!!!!!!!!!!!!!!!!Error means there is no existing 3scale-broker, just continue to create the new one..."
    }
                    
    sh "${OC_HOME}/oc create -f 3scale-broker.yml"
    sh "sleep 5"
    sh "${OC_HOME}/oc describe ClusterServiceBroker 3scale-broker"
    }        

    println("---------------------------------- Recreate Broker is finished ----------------------------------")
    }
     */
       
    
    
    stage ('Test getCatalog') {
        println("---------------------------------- Test getCatalog  ----------------------------------")
        withEnv(["PATH+OC=${OC_HOME}"]) {
            try {
                sh "${OC_HOME}/oc expose service three-scale --hostname=test.broker.com"
            }catch(err) {
                echo "!!!!!!!!!!!!!!!!!!Error means there is an existing test.broker.com, no need to create, just continue for curl test..."
            }            
            sh "sleep 5"
            //            def result = sh "curl http://test.broker.com/v2/catalog "
            
            def result = sh (
                script: 'curl http://test.broker.com/v2/catalog',
                //                script: 'curl http://www.google.com',
                returnStdout: true
            ).trim()    
            echo "curl result: ${result}"   
            
            def expectWords = "{\"services\""
            if (!result.contains(expectWords)){
                echo "result didn't contain following expect words: ${expectWords} "
                currentBuild.result = 'FAILURE'
            }else{
                echo "good result, passed"
                
                //get the planId from the catalog, which will be used in the testing of secured market, just pick the last one
                def tmpStr = "\"plans\":[{\"id\":\""
                def i = result.lastIndexOf(tmpStr);
                def j = result.	indexOf("\"",i +  tmpStr.length())
                planId = result.substring(i +  tmpStr.length(),j)
                echo "planId: ${planId}"
                
                tmpStr = "\"id\":\""
                def k = result.indexOf(tmpStr,i+30)
                def g = result.indexOf("\"", k + tmpStr.length())
                planId2 = result.substring(k +  tmpStr.length(),g)
                echo "planId2: ${planId2}"

                def f = i - 2
                def e = result.lastIndexOf("\"",f-1);
                serviceId = result.substring(e+1,f)
                echo "serviceId: ${serviceId}"
                
            }
 
        }        

        println("---------------------------------- Test getCatalog is finished ----------------------------------")
    }
    
    
    /*
    stage ('Test2: provisionSecureServices') {
    //Test provisionSecuredServices with instance id = 123
    println("---------------------------------- Test provisionSecuredServices  ----------------------------------")
    //do a deprovisioning first, otherwise the provision will be skipped if there is already same instance id in the sqlite DB
    //without deprovisioning first it might also failed because same name exists at 3 scale side. 
    sh "curl  -H \"Content-Type: application/json\" -X DELETE  \"http://test.broker.com/v2/service_instances/123?plan_id=secure-service-plan-id&service_id=secure-service-id\""
        
    def result = sh (
    script: "curl  -H \"Content-Type: application/json\" -X PUT -d '{\"context\":{\"platform\":\"ocp\",\"namespace\":\"some-namespace\"},\"service_id\":\"service-guid-here\",\"plan_id\":\"plan-guid-here\",\"organization_guid\":\"org-guid-here\",\"space_guid\":\"space-guid-here\",\"parameters\":{\"service_name\":\"testapi\",\"application_plan\":\"plan1\",\"input_url\":\"http://www.google.com\",\"application_name\":\"testApp1\"}}'  http://test.broker.com/v2/service_instances/123",
    returnStdout: true
    ).trim()    
    echo "curl result: ${result}"   
            
    def expectWords = "/?user_key="
    if (!result.contains(expectWords)){
    echo "result didn't contain following expect words: ${expectWords} "
    currentBuild.result = 'FAILURE'
    }else{
    echo "good result, passed"
    }
    println("---------------------------------- Test2: provisionSecureServices is finished ----------------------------------")
 
    }  
    
    stage ('Test3: provisionSecuredMarket') {
    //Test provisionSecuredServices with instance id = 123
    println("---------------------------------- Test provisionSecuredMarket  ----------------------------------")
            
    //do a deprovisioning first, otherwise the provision will be skipped if there is already same instance id in the sqlite DB
    //without deprovisioning first it might also failed because same name exists at 3 scale side. 
    sh "curl  -H \"Content-Type: application/json\" -X DELETE  \"http://test.broker.com/v2/service_instances/5555?plan_id=secure-service-plan-id&service_id=secure-service-id\""
        
    def result = sh (
    script: "curl  -H \"Content-Type: application/json\" -X PUT -d '{\"context\":{\"platform\":\"ocp\",\"namespace\":\"some-namespace\"},\"service_id\":${serviceId},\"plan_id\":${planId},\"organization_guid\":\"org-guid-here\",\"space_guid\":\"space-guid-here\",\"parameters\":{\"applicationName\":\"testSecuredMarketApp\",\"description\":\"testSecuredMarketApp\"}}'  http://test.broker.com/v2/service_instances/5555",
    returnStdout: true
    ).trim()    
    echo "curl result: ${result}"   
            
    def expectWords = "/?user_key="
    if (!result.contains(expectWords)){
    echo "result didn't contain following expect words: ${expectWords} "
    currentBuild.result = 'FAILURE'
    }else{
    echo "good result, passed"
    }
    println("---------------------------------- Test3: provisionSecuredMarket is finished ----------------------------------")
 
    }      
*/
    stage ('Test3: provisionSecuredMarket') {
        //Test provisionSecuredMarket with instance id = 5555
        println("---------------------------------- Test3: provisionSecuredMarket  ----------------------------------")
            
        //do a deprovisioning first, otherwise the provision will be skipped if there is already same instance id in the sqlite DB
        //without deprovisioning first it might also failed because same name exists at 3 scale side. 
        //test instance id: 5555
        sh "curl  -H \"Content-Type: application/json\" -X DELETE  \"http://test.broker.com/v2/service_instances/5555?plan_id=secure-service-plan-id&service_id=secure-service-id\""
        
        def result = sh (
            script: "curl  -H \"Content-Type: application/json\" -X PUT -d '{\"context\":{\"platform\":\"ocp\",\"namespace\":\"some-namespace\"},\"service_id\":${serviceId},\"plan_id\":${planId},\"organization_guid\":\"org-guid-here\",\"space_guid\":\"space-guid-here\",\"parameters\":{\"applicationName\":\"testSecuredMarketApp\",\"description\":\"testSecuredMarketApp\"}}'  http://test.broker.com/v2/service_instances/5555",
            returnStdout: true
        ).trim()    
        echo "curl result: ${result}"   
            
        def expectWords = "/?user_key="
        if (!result.contains(expectWords)){
            echo "result didn't contain following expect words: ${expectWords} "
            currentBuild.result = 'FAILURE'
        }else{
            echo "good result, passed"
        }
        println("---------------------------------- Test3: provisionSecuredMarket is finished ----------------------------------")
    }          
    
    /*
    stage ('Test4: BindingForSecuredMarket') {
    //Test provisionSecuredServices with instance id = 123
    println("---------------------------------- Test4: BindingForSecuredMarket  ----------------------------------")
            
    //do a unbinding first, otherwise the provision will be skipped if there is already same instance id in the sqlite DB
    //test instance id: 8888, binding id: 9999, note we didn't really use binding id.
    sh "curl  -H \"Content-Type: application/json\" -X DELETE  \"http://test.broker.com/v2/service_instances/8888/service_bindings/9999\""
        
    def result = sh (
    script: "curl  -H \"Content-Type: application/json\" -X PUT -d '{\"service_id\":${serviceId},\"plan_id\":${planId},\"bind_resource\": {\"app_guid\": \"app-guid-here\"}}'  http://test.broker.com/v2/service_instances/8888/service_bindings/9999",
    returnStdout: true
    ).trim()    
    echo "curl result: ${result}"   
            
    def expectWords = "user_key"
    if (!result.contains(expectWords)){
    echo "result didn't contain following expect words: ${expectWords} "
    currentBuild.result = 'FAILURE'
    }else{
    echo "good result, passed"
    }
    println("---------------------------------- Test4: BindingForSecuredMarket is finished ----------------------------------")
 
    }      

     */
    
    stage ('Test5: updateServiceInstance') {
        println("---------------------------------- Test5: updateServiceInstance  ----------------------------------")
            
        //test instance id: 5555, note this need to be the same as test3's instance id because it's update based on this instance id
        //test3 uses planId (smallPizza plan), while this will use planId2 to update it to (largePizza plan)
        
        def result = sh (
            script: "curl  -H \"Content-Type: application/json\" -X PATCH -d '{\"service_id\":${serviceId},\"plan_id\":${planId2},\"context\":{\"platform\":\"ocp\",\"namespace\":\"some-namespace\"}}'  http://test.broker.com/v2/service_instances/5555",
            returnStdout: true
        ).trim()    
        echo "curl result: ${result}"   
            
        def expectWords = "success"
        if (!result.contains(expectWords)){
            echo "result didn't contain following expect words: ${expectWords} "
            currentBuild.result = 'FAILURE'
        }else{
            echo "good result, passed"
        }
        println("---------------------------------- Test5: updateServiceInstance is finished ----------------------------------")
    }      
}
