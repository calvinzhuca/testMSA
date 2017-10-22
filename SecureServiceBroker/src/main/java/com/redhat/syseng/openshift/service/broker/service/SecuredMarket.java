package com.redhat.syseng.openshift.service.broker.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.syseng.openshift.service.broker.model.catalog.ApplicationName;
import com.redhat.syseng.openshift.service.broker.model.catalog.Catalog;
import com.redhat.syseng.openshift.service.broker.model.catalog.Create;
import com.redhat.syseng.openshift.service.broker.model.catalog.Description;
import com.redhat.syseng.openshift.service.broker.model.catalog.Metadata;
import com.redhat.syseng.openshift.service.broker.model.catalog.Parameters;
import com.redhat.syseng.openshift.service.broker.model.catalog.Plan;
import com.redhat.syseng.openshift.service.broker.model.catalog.Properties;
import com.redhat.syseng.openshift.service.broker.model.catalog.Schemas;
import com.redhat.syseng.openshift.service.broker.model.catalog.Service;
import com.redhat.syseng.openshift.service.broker.model.catalog.Service_binding;
import com.redhat.syseng.openshift.service.broker.model.catalog.Service_instance;
import com.redhat.syseng.openshift.service.broker.service.broker.util.BrokerUtil;
import static com.redhat.syseng.openshift.service.broker.service.broker.util.BrokerUtil.restWsCall;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class SecuredMarket {

    private Logger logger = Logger.getLogger(getClass().getName());

    private static final Random random = new Random();

    private void logInfo(String message) {
        logger.log(Level.INFO, message);
    }

    public synchronized String provisioningForSecuredMarket(@PathParam("instance_id") String instance_id, com.redhat.syseng.openshift.service.broker.model.provision.market.Provision provision) //public String provisioning( String testString) {
    {
        logInfo("!!!!!!!!!!provisioning /service_instances/{instance_id} : " + instance_id);
        logInfo("provision.getOrganization_guid() : " + provision.getOrganization_guid());
        logInfo("provision.getService_id() : " + provision.getService_id());
        logInfo("provision.getPlan_id() : " + provision.getPlan_id());
        logInfo("provision.getParameters().getApplicationName() : " + provision.getParameters().getApplicationName());
        logInfo("provision.getParameters().getDescription() : " + provision.getParameters().getDescription());

        String result = "{\"dashboard_url\":\"https://testapi-3scale-apicast-staging.middleware.ocp.cloud.lab.eng.bos.redhat.com:443/?user_key=2491bd25351aeb458fea55381b3d4560\",\"operation\":\"task_10\"}";
        String url = "";

        //looks like I need to have an account ready first, and I don't see a REST api for create account, so I manually create one "brokerGroup", id is "5"
        int account_id = 5;

        //create Application to use the Plan, which will generate a valid user_key
        ArrayList<NameValuePair> postParameters;
        postParameters = new ArrayList();
        postParameters = new ArrayList();
        postParameters.add(new BasicNameValuePair("name", provision.getParameters().getApplicationName()));
        //Add GUID in the description, so later the binding can find this application based on guid. 
        String desc = provision.getParameters().getDescription() + " GUID:" + provision.getOrganization_guid();
        postParameters.add(new BasicNameValuePair("description", desc));
        postParameters.add(new BasicNameValuePair("plan_id", provision.getPlan_id()));

        String ampUrl = "/admin/api/accounts/" + account_id + "/applications.xml";

        //after this step, in the API Integration page, the user_key will automatically replaced with the new one created below
        result = BrokerUtil.restWsCall(ampUrl, postParameters, "POST");
        logInfo("---------------------application is created : " + result);

        String user_key = result.substring(result.indexOf("<user_key>") + "<user_key>".length(), result.indexOf("</user_key>"));
        logInfo("user_key : " + user_key);
        String endpoint = BrokerUtil.searchEndPointBasedOnServiceId(provision.getService_id());
        url = endpoint + "/?user_key=" + user_key;
        result = "{\"dashboard_url\":" + url + ",\"operation\":\"task_10\"}";

        result = "{\"dashboard_url\":\"" + url + "\",\"operation\":\"task_10\"}";
        logInfo("provisioning result" + result);
        return result;
    }

    @GET
    @Path("/catalog")
    @Consumes({"*/*"})
    @Produces({MediaType.APPLICATION_JSON})
    public Response getCatalog() {
        /*
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/catalog.json")));
        String catalog = bufferedReader.lines().collect(Collectors.joining("\n"));
        logInfo("catalog:\n\n" + catalog);
         */
        String result = "";

        ArrayList<NameValuePair> postParameters;
        postParameters = new ArrayList();

        String ampUrl = "/admin/api/services.xml";
        result = restWsCall(ampUrl, postParameters, "GET");
        logInfo("---------------------getCatalog search service : " + result);

        int i = result.indexOf("<id>");
        ArrayList<Service> svcList = new ArrayList<Service>();
        while (i != -1) {
            Service svc = new Service();
            String id = result.substring(result.indexOf("<id>", i) + "<id>".length(),
                    result.indexOf("</id>", i));

            svc.setId(id);
            String name = result.substring(result.indexOf("<name>", i) + "<name>".length(),
                    result.indexOf("</name>", i));
            svc.setDescription(name);

            String systemName = result.substring(result.indexOf("<system_name>", i) + "<system_name>".length(),
                    result.indexOf("</system_name>", i));
            svc.setName(systemName);

            svc.setBindable(true);

            svc.setPlans(readPlansForOneService(id));

            Metadata mt = new Metadata();
            mt.setDisplayName("secured-market-service: " + name);
            mt.setDocumentationUrl("https://access.qa.redhat.com/documentation/en-us/reference_architectures/2017/html/api_management_with_red_hat_3scale_api_management_platform");
            mt.setLongDescription("secured service through 3scale-AMP, name is: " + name);
            svc.setMetadata(mt);

            svcList.add(svc);

            int j = result.indexOf("</service>", i);
            i = result.indexOf("<id>", j);
            //i = -1;
        }

        Service[] svcs = svcList.toArray(new Service[svcList.size()]);
        Catalog cat = new Catalog();
        cat.setServices(svcs);

        //Gson gson = new Gson();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        result = gson.toJson(cat);
        logInfo("Json from gson: " + result);

        return Response.ok(result, MediaType.APPLICATION_JSON).build();
    }

    private Plan[] readPlansForOneService(String serviceId) {
        //call the Application PLan List function
        String ampUrl = ampUrl = "/admin/api/services/" + serviceId + "/application_plans.xml";;
        String result = restWsCall(ampUrl, null, "GET");
        logInfo("---------------------getCatalog search service : " + result);
        int i = result.indexOf("<id>");
        ArrayList<Plan> planList = new ArrayList<Plan>();
        while (i != -1) {
            Plan plan = new Plan();
            String id = result.substring(result.indexOf("<id>", i) + "<id>".length(),
                    result.indexOf("</id>", i));

            plan.setId(id);
            String name = result.substring(result.indexOf("<name>", i) + "<name>".length(),
                    result.indexOf("</name>", i));
            plan.setName(name);
            //TODO: get the description from somewhere  
            plan.setDescription(" plan description ...");
            plan.setFree(true);

            //create service instance
            Properties properties = new Properties();

            Description description = new Description();
            description.setTitle("description");
            description.setType("string");
            ApplicationName applicationName = new ApplicationName();
            applicationName.setTitle("application name");
            applicationName.setType("string");

            properties.setDescription(description);
            properties.setApplicationName(applicationName);
            Parameters parameters = new Parameters();
            parameters.set$schema("http://json-schema.org/draft-04/schema");
            parameters.setAdditionalProperties(false);
            parameters.setType("object");
            String[] required = new String[]{"applicationName", "description"};
            parameters.setRequired(required);
            parameters.setProperties(properties);

            Create create = new Create();
            create.setParameters(parameters);
            Service_instance si = new Service_instance();
            si.setCreate(create);

            //update structure is the same as create
            Create update = new Create();
            si.setUpdate(update);

            Service_binding sb = new Service_binding();

            Schemas schemas = new Schemas();
            schemas.setService_binding(sb);
            schemas.setService_instance(si);
            plan.setSchemas(schemas);

            planList.add(plan);

            int j = result.indexOf("</plan>", i);
            i = result.indexOf("<id>", j);
            //i = -1;

        }
        Plan[] plans = planList.toArray(new Plan[planList.size()]);
        return plans;
    }

}
