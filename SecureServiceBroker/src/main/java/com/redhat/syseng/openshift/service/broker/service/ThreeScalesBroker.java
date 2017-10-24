package com.redhat.syseng.openshift.service.broker.service;

import com.google.gson.Gson;
import com.redhat.syseng.openshift.service.broker.model.catalog.ApplicationName;
import com.redhat.syseng.openshift.service.broker.model.catalog.Create;
import com.redhat.syseng.openshift.service.broker.model.catalog.Description;
import com.redhat.syseng.openshift.service.broker.model.catalog.Parameters;
import com.redhat.syseng.openshift.service.broker.model.catalog.Plan;
import com.redhat.syseng.openshift.service.broker.model.catalog.Properties;
import com.redhat.syseng.openshift.service.broker.model.catalog.Schemas;
import com.redhat.syseng.openshift.service.broker.model.catalog.Service_binding;
import com.redhat.syseng.openshift.service.broker.model.catalog.Service_instance;
import static com.redhat.syseng.openshift.service.broker.service.broker.util.BrokerUtil.restWsCall;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v2")
public class ThreeScalesBroker {

    private Logger logger = Logger.getLogger(getClass().getName());

    private static final Random random = new Random();

    private void logInfo(String message) {
        logger.log(Level.INFO, message);
    }


    @GET
    @Path("/catalog")
    @Consumes({"*/*"})
    @Produces({MediaType.APPLICATION_JSON})
    public Response getCatalog() {
        String result = new SecuredMarket().getCatalog();

        //now read the catalog for secure service, which is static
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/catalog.json")));
        String secureServiceCatalog = bufferedReader.lines().collect(Collectors.joining("\n"));
        logInfo("secure service catalog:\n\n" + secureServiceCatalog);
    
        int j = result.indexOf("[");
        result = result.substring(0,j+1) + secureServiceCatalog + result.substring(j+1,result.length());
        logInfo("secure service catalog:\n\n" + secureServiceCatalog);
        
        return Response.ok(result, MediaType.APPLICATION_JSON).build();
    }


    @PUT
    @Path("/service_instances/{instance_id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public synchronized String synchronizedprovisioning(@PathParam("instance_id") String instance_id, String inputJsonString) {

        logInfo("!!!!!!!!!!provisioning /service_instances/{instance_id} : " + instance_id);
        logInfo("provision.inputJsonString : " + inputJsonString);
        Gson gson = new Gson();
        String result = "";
        if (inputJsonString.contains("input_url")) {
            com.redhat.syseng.openshift.service.broker.model.provision.secure.Provision provision = gson.fromJson(inputJsonString, com.redhat.syseng.openshift.service.broker.model.provision.secure.Provision.class);
            result = new ServiceSecurer().provisioningForSecureService(instance_id, provision);
        }else{
            com.redhat.syseng.openshift.service.broker.model.provision.market.Provision provision = gson.fromJson(inputJsonString, com.redhat.syseng.openshift.service.broker.model.provision.market.Provision.class);
            result = new SecuredMarket().provisioningForSecuredMarket(instance_id, provision);
        }

        logInfo("provision.result : " + result);
        return result;

    }
    

    @PUT
    @Path("/service_instances/{instance_id}/service_bindings/{binding_id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public synchronized String binding(@PathParam("instance_id") String instance_id, String inputStr) {
//This is a sample of binding inputStr
// {"app_guid":"23d69a55-a40f-11e7-a9bf-beeffeed005d","plan_id":"102","service_id":"42","bind_resource":{"app_guid":"23d69a55-a40f-11e7-a9bf-beeffeed005d"}}
//don't see a way for me to differentiate which binding this request is calling for
//so just forward to SecuredMarket().binding for now. 
        
        String result = new SecuredMarket().binding(inputStr);
        logInfo("binding.result : " + result);
        return result;
    }    

    @DELETE
    @Path("/service_instances/{instance_id}")
    @Consumes({"*/*"})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    //public String deProvisioning(String inputStr)
    public synchronized String deProvisioning(@PathParam("instance_id") String instance_id) {
        //logInfo( "!!!!!!!!!!!!!deProvisioning /service_instances/{instance_id}: " + instance_id );

        String responseStr = System.getenv("RESPONSE_STRING");
        logInfo("deProvisioning instance_id: " + instance_id);
        logInfo("deProvisioning responseStr 2: " + responseStr);
        String result = responseStr;
        return result;
    }


    @DELETE
    @Path("/service_instances/{instance_id}/service_bindings/{binding_id}")
    @Consumes({"*/*"})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public String unBinding(@PathParam("instance_id") String instance_id, @PathParam("binding_id") String binding_id) {
        //  String result = "test";
        String result = "{}";
        logInfo("!!!!!!!!!!!!!!!!unBinding instance_id: " + instance_id);
        logInfo("unBinding binding_id: " + binding_id);
        return result;
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
