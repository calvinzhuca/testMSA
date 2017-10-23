package com.redhat.syseng.openshift.service.broker.service;

import com.redhat.syseng.openshift.service.broker.model.provision.secure.Provision;
import com.redhat.syseng.openshift.service.broker.service.broker.util.BrokerUtil;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.commons.lang3.RandomStringUtils;
import static com.redhat.syseng.openshift.service.broker.service.broker.util.BrokerUtil.restWsCall;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ServiceSecurer {

    private Logger logger = Logger.getLogger(getClass().getName());

    private static final Random random = new Random();

    private void logInfo(String message) {
        logger.log(Level.INFO, message);
    }

    
    

//    public synchronized String provisioning2(@PathParam("instance_id") String instance_id, Provision provision) //public String provisioning( String testString) {
    synchronized String provisioningForSecureService(String instance_id, Provision provision) //public String provisioning( String testString) {
    {
        logInfo("!!!!!!!!!!provisioning /service_instances/{instance_id} : " + instance_id);
        logInfo("provision.getService_id() : " + provision.getService_id());
        logInfo("provision.getOrganization_guid() : " + provision.getOrganization_guid());
        logInfo("provision.getParameters().getService_name() : " + provision.getParameters().getService_name());
        logInfo("provision.getParameters().getApplication_plan() : " + provision.getParameters().getApplication_plan());
        logInfo("provision.getParameters().getInput_url() : " + provision.getParameters().getInput_url());
        logInfo("provision.getParameters().getApplication_name() : " + provision.getParameters().getApplication_name());

        String result = "{\"dashboard_url\":\"https://testapi-3scale-apicast-staging.middleware.ocp.cloud.lab.eng.bos.redhat.com:443/?user_key=2491bd25351aeb458fea55381b3d4560\",\"operation\":\"task_10\"}";
        String url = "";

        //looks like I need to have an account ready first, and I don't see a REST api for create account, so I manually create one "brokerGroup", id is "5"
        int account_id = 5;
        url = searchServiceInstance(provision.getParameters().getService_name(), account_id);
        //no existing service, need to create one
        if ("".equals(url)) {

            ArrayList<NameValuePair> postParameters;
            postParameters = new ArrayList();
            postParameters.add(new BasicNameValuePair("name", provision.getParameters().getService_name()));
            postParameters.add(new BasicNameValuePair("system_name", provision.getParameters().getService_name()));
            postParameters.add(new BasicNameValuePair("description", instance_id));

            String ampUrl = "/admin/api/services.xml";
            result = restWsCall(ampUrl, postParameters, "POST");
            logInfo("---------------------services is created : " + result);
            String serviceID = result.substring(result.indexOf("<id>") + "<id>".length(), result.indexOf("</id>"));
            logInfo("serviceID : " + serviceID);

            //create applicaiton plan
            ampUrl = "/admin/api/services/" + serviceID + "/application_plans.xml";
            postParameters = new ArrayList();
            postParameters.add(new BasicNameValuePair("name", provision.getParameters().getApplication_plan()));
            postParameters.add(new BasicNameValuePair("system_name", provision.getParameters().getApplication_plan()));
            result = restWsCall(ampUrl, postParameters, "POST");
            logInfo("---------------------application plan is created: " + result);
            String planID = result.substring(result.indexOf("<id>") + "<id>".length(), result.indexOf("</id>"));
            logInfo("planID : " + planID);

            createMappingRules(serviceID);

            //API integration
            ampUrl = "/admin/api/services/" + serviceID + "/proxy.xml";
            postParameters = new ArrayList();
            postParameters.add(new BasicNameValuePair("service_id", serviceID));
            postParameters.add(new BasicNameValuePair("api_backend", provision.getParameters().getInput_url()));
            result = restWsCall(ampUrl, postParameters, "PATCH");
            logInfo("---------------------integration result : " + result);

            //create Application to use the Plan, which will generate a valid user_key
            postParameters = new ArrayList();
            postParameters.add(new BasicNameValuePair("name", provision.getParameters().getApplication_name()));
            postParameters.add(new BasicNameValuePair("description", provision.getParameters().getApplication_name()));
            postParameters.add(new BasicNameValuePair("plan_id", planID));

            ampUrl = "/admin/api/accounts/" + account_id + "/applications.xml";

            //after this step, in the API Integration page, the user_key will automatically replaced with the new one created below
            result = restWsCall(ampUrl, postParameters, "POST");
            logInfo("---------------------application is created : " + result);
            String user_key = result.substring(result.indexOf("<user_key>") + "<user_key>".length(), result.indexOf("</user_key>"));
            logInfo("user_key : " + user_key);

            String domain = "-3scale-apicast-staging.middleware.ocp.cloud.lab.eng.bos.redhat.com:443";
            url = "https://" + provision.getParameters().getService_name() + domain + "/?user_key=" + user_key;
            result = "{\"dashboard_url\":" + url + ",\"operation\":\"task_10\"}";

        }

        result = "{\"dashboard_url\":\"" + url + "\",\"operation\":\"task_10\"}";
        logInfo("provisioning result" + result);
        return result;
    }

    private void createUser(String userName, String password) {

        ArrayList<NameValuePair> postParameters;
        postParameters = new ArrayList();
        postParameters.add(new BasicNameValuePair("username", userName));
        postParameters.add(new BasicNameValuePair("password", password));
        String email = userName + "@example.com";
        postParameters.add(new BasicNameValuePair("email", email));

        //looks like I need to have an account ready first, and I don't see a REST api for create account, so I manually create one "brokerGroup", id is "5"
        int account_id = 5;
        String ampUrl = "/admin/api/accounts/" + account_id + "/users.xml";
        String result = restWsCall(ampUrl, postParameters, "POST");
        logInfo("user is created : " + result);
        String tmpID = result.substring(result.indexOf("<id>") + "<id>".length(), result.indexOf("</id>"));
        logInfo("user ID : " + tmpID);

        //now activate the new user, the default state is "pending"
        ampUrl = "/admin/api/accounts/" + account_id + "/users/" + tmpID + "/activate.xml";
        postParameters = new ArrayList();
        result = restWsCall(ampUrl, postParameters, "PUT");
        logInfo("user is activated : " + result);

    }


    public synchronized String binding(String inputStr) {
        //public String binding(@PathParam("instance_id") String instance_id, @PathParam("binding_id") String binding_id) {
        //  String result = "test";

        boolean useLetters = true;
        boolean useNumbers = false;
        String userName = "user_" + RandomStringUtils.random(4, useLetters, useNumbers);

        useNumbers = true;
        String passWord = RandomStringUtils.random(15, useLetters, useNumbers);
        logInfo("binding userName: " + userName);
        logInfo("binding passWord: " + passWord);

        createUser(userName, passWord);
//        String responseStr = System.getenv("RESPONSE_STRING");
        //String result = "{\"route_service_url\":\"http://172.30.244.67:8080\"}";
        //String result = "{\"credentials\":{\"username\":\"mysqluser\",\"password\":\"pass\"}}";
        String result = "{\"credentials\":{\"username\":\"" + userName + "\",\"password\":\"" + passWord + "\",\"url\":\"https://3scale.middleware.ocp.cloud.lab.eng.bos.redhat.com/login\"}}";
        //logInfo("binding instance_id : " + instance_id);
        //logInfo("binding binding_id : " + binding_id);
        logInfo("binding inputStr 6: " + inputStr);
        logInfo("binding result: " + result);
        //logInfo("!!!!!!!!!!!!!!!binding instance_id: " + instance_id);
        //logInfo("binding binding_id: " + binding_id);
        //result = "{/\"credentials/\":{/\"uri/\":/\"mysql://mysqluser:pass@mysqlhost:3306/dbname/\",/\"username/\":/\"mysqluser/\",/\"password/\":/\"pass/\",/\"host/\":/\"mysqlhost/\",/\"port/\":3306,/\"database/\":/\"dbname/\"}}";
        //result = "{/\"credentials/\":{/\"ttt/\":/\"12345678901111111111111111111111111111111111/\",/\"username/\":/\"mysqluser/\",/\"password/\":/\"pass/\",/\"hhhh/\":/\"222222222/\",/\"port/\":3306,/\"database/\":/\"dbname/\"}}";
        //result = "{\"dashboard_url\":\"\",\"operation\":\"task_10\"}";
        //result = "{\"test\":\"111111111111111\",\"test2\":\"task_10\"}";
        return result;
    }



    private void createMappingRules(String serviceID) {
        //create mapping rule, first need to get the "hit" metric id.
        String ampUrl = "/admin/api/services/" + serviceID + "/metrics.xml";
        String result = restWsCall(ampUrl, null, "GET");
        logInfo("get metric result : " + result);
        String metricID = result.substring(result.indexOf("<id>") + "<id>".length(), result.indexOf("</id>"));
        logInfo("metricID : " + metricID);

        ampUrl = "/admin/api/services/" + serviceID + "/proxy/mapping_rules.xml";
        ArrayList<NameValuePair> postParameters;
        postParameters = new ArrayList();

        //now create mapping rule for POST under metric "hit"
        postParameters.add(new BasicNameValuePair("pattern", "/"));
        postParameters.add(new BasicNameValuePair("delta", "1"));
        postParameters.add(new BasicNameValuePair("metric_id", metricID));
        postParameters.add(3, new BasicNameValuePair("http_method", "POST"));
        result = restWsCall(ampUrl, postParameters, "POST");
        logInfo("creating mapping result : " + result);

        //now create mapping rule for PUT under metric "hit"
        postParameters.remove(3);
        postParameters.add(3, new BasicNameValuePair("http_method", "PUT"));
        result = restWsCall(ampUrl, postParameters, "POST");
        logInfo("creating mapping result : " + result);

        //now create mapping rule for PATCH under metric "hit"
        postParameters.remove(3);
        postParameters.add(3, new BasicNameValuePair("http_method", "PATCH"));
        result = restWsCall(ampUrl, postParameters, "POST");
        logInfo("creating mapping result : " + result);

        //now create mapping rule for DELETE under metric "hit"
        postParameters.remove(3);
        postParameters.add(3, new BasicNameValuePair("http_method", "DELETE"));
        result = restWsCall(ampUrl, postParameters, "POST");
        logInfo("creating mapping result : " + result);

    }

    private String searchServiceInstance(String inputServiceSystemName, int account_id) {
        ArrayList<NameValuePair> postParameters;
        postParameters = new ArrayList();

        String ampUrl = "/admin/api/services.xml";
        String result = restWsCall(ampUrl, postParameters, "GET");
        logInfo("services are listed : " + result);

        int i = result.indexOf("<system_name>" + inputServiceSystemName + "</system_name>");

        if (i > -1) {
            String serviceId = result.substring(result.lastIndexOf("<service><id>", i) + "<service><id>".length(), result.lastIndexOf("</id>", i));
            logInfo("---------------------found same system_name service, id : " + serviceId);
            String user_key = searchUserKeyBasedOnServiceId(serviceId, account_id);
            String endpoint = BrokerUtil.searchEndPointBasedOnServiceId(serviceId);

            String url = endpoint + "/?user_key=" + user_key;

            return url;

        } else {
            logInfo("---------------------didn't found same system_name service : " + inputServiceSystemName);
            return "";
        }

    }

    private String searchUserKeyBasedOnServiceId(String serviceId, int accountId) {
        ArrayList<NameValuePair> postParameters;
        postParameters = new ArrayList();

        //String ampUrl = "/admin/api/accounts/" + accountId + "/applications.xml ";
        String ampUrl = "/admin/api/applications.xml";
        String result = restWsCall(ampUrl, postParameters, "GET");
        logInfo("application is listed : " + result);

        int i = result.indexOf("<service_id>" + serviceId + "</service_id>");
        String user_key = "";
        if (i > -1) {
            user_key = result.substring(result.indexOf("<user_key>", i) + "<user_key>".length(), result.indexOf("</user_key>", i));
            logInfo("---------------------found user_key for this service id : " + user_key);

        } else {
            logInfo("---------------------didn't found same service id in this application: " + serviceId);
        }
        return user_key;

    }

}
