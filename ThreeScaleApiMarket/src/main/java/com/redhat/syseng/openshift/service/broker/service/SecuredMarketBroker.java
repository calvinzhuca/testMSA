package com.redhat.syseng.openshift.service.broker.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.syseng.openshift.service.broker.model.catalog.Catalog;
import com.redhat.syseng.openshift.service.broker.model.catalog.Create;
import com.redhat.syseng.openshift.service.broker.model.catalog.Metadata;
import com.redhat.syseng.openshift.service.broker.model.catalog.Parameters;
import com.redhat.syseng.openshift.service.broker.model.catalog.Description;
import com.redhat.syseng.openshift.service.broker.model.catalog.Plan;
import com.redhat.syseng.openshift.service.broker.model.catalog.Properties;
import com.redhat.syseng.openshift.service.broker.model.catalog.Schemas;
import com.redhat.syseng.openshift.service.broker.model.catalog.Service_binding;
import com.redhat.syseng.openshift.service.broker.model.catalog.Service;
import com.redhat.syseng.openshift.service.broker.model.catalog.Service_instance;
import com.redhat.syseng.openshift.service.broker.model.catalog.ApplicationName;
import com.redhat.syseng.openshift.service.broker.model.provision.Provision;
import java.io.BufferedReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v2")
public class SecuredMarketBroker {

    private Logger logger = Logger.getLogger(getClass().getName());

    private static final Random random = new Random();

    private void logInfo(String message) {
        logger.log(Level.INFO, message);
    }

    public static void main(String[] args) {
        System.out.println("===============================");

        Service svc = new Service();
        svc.setName("three-scale-service");
        svc.setId("serviceUUID");
        svc.setDescription("secure service 3scale broker implementation");
        svc.setBindable(true);

        Metadata mt = new Metadata();
        mt.setDisplayName("secure-service-3scale-broker");
        mt.setDocumentationUrl("https://access.qa.redhat.com/documentation/en-us/reference_architectures/2017/html/api_management_with_red_hat_3scale_api_management_platform");
        mt.setLongDescription("A broker that secures input URL through 3scale-AMP");
        svc.setMetadata(mt);

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

        Create update = new Create();
        si.setUpdate(update);

        Service_binding sb = new Service_binding();

        Schemas schemas = new Schemas();
        schemas.setService_binding(sb);
        schemas.setService_instance(si);

        Plan plan = new Plan();
        plan.setDescription("3scale plan descriptions ...");
        plan.setFree(true);
        plan.setName("test-plan");
        plan.setId("gold-plan-id");
        plan.setSchemas(schemas);

        Plan[] plans = new Plan[1];
        plans[0] = plan;

        svc.setPlans(plans);

        Service[] svcs = new Service[1];
        svcs[0] = svc;
        Catalog cat = new Catalog();
        cat.setServices(svcs);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("Json from gson: " + gson.toJson(cat));

    }

    @GET
    @Path("/catalog2")
    @Consumes({"*/*"})
    @Produces({MediaType.APPLICATION_JSON})
    public Response getCatalog2() {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/catalog.json")));
        String catalog = bufferedReader.lines().collect(Collectors.joining("\n"));
        logInfo("catalog:\n\n" + catalog);
        return Response.ok(catalog, MediaType.APPLICATION_JSON).build();
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
        try {
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
        } catch (IOException ex) {
            Logger.getLogger(SecuredMarketBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(SecuredMarketBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(SecuredMarketBroker.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Response.ok(result, MediaType.APPLICATION_JSON).build();
    }

    private Plan[] readPlansForOneService(String serviceId) throws JSONException, IOException, URISyntaxException {
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

    /*
    @PUT
    @Path("/service_instances/{instance_id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    //public synchronized String provisioning(@PathParam("instance_id") String instance_id, Provision provision) 
    public synchronized String provisioning(String inputStr) 
    {
        logInfo("provision.inputStr : " + inputStr);
        return "";
    }    
     */
    @PUT
    @Path("/service_instances/{instance_id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public synchronized String provisioning(@PathParam("instance_id") String instance_id, Provision provision) //public String provisioning( String testString) {
    {
        logInfo("!!!!!!!!!!provisioning /service_instances/{instance_id} : " + instance_id);
        logInfo("provision.getOrganization_guid() : " + provision.getOrganization_guid());
        logInfo("provision.getService_id() : " + provision.getService_id());
        logInfo("provision.getPlan_id() : " + provision.getPlan_id());
        logInfo("provision.getParameters().getApplicationName() : " + provision.getParameters().getApplicationName());
        logInfo("provision.getParameters().getDescription() : " + provision.getParameters().getDescription());

        String result = "{\"dashboard_url\":\"https://testapi-3scale-apicast-staging.middleware.ocp.cloud.lab.eng.bos.redhat.com:443/?user_key=2491bd25351aeb458fea55381b3d4560\",\"operation\":\"task_10\"}";
        String url = "";

        try {

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
            result = restWsCall(ampUrl, postParameters, "POST");
            logInfo("---------------------application is created : " + result);

            String user_key = result.substring(result.indexOf("<user_key>") + "<user_key>".length(), result.indexOf("</user_key>"));
            logInfo("user_key : " + user_key);
            String endpoint = searchEndPointBasedOnServiceId(provision.getService_id());
            url = endpoint + "/?user_key=" + user_key;
            result = "{\"dashboard_url\":" + url + ",\"operation\":\"task_10\"}";

        } catch (IOException ex) {
            Logger.getLogger(SecuredMarketBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(SecuredMarketBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(SecuredMarketBroker.class.getName()).log(Level.SEVERE, null, ex);
        }

        result = "{\"dashboard_url\":\"" + url + "\",\"operation\":\"task_10\"}";
        logInfo("provisioning result" + result);
        return result;
    }

    private void createUser(String userName, String password, String email) {

        try {
            ArrayList<NameValuePair> postParameters;
            postParameters = new ArrayList();
            postParameters.add(new BasicNameValuePair("username", userName));
            postParameters.add(new BasicNameValuePair("password", password));
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

        } catch (IOException ex) {
            Logger.getLogger(SecuredMarketBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(SecuredMarketBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(SecuredMarketBroker.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @PUT
    @Path("/create_application/{instance_id}")
    //@Consumes("application/x-www-form-urlencoded")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public String createApplication(@PathParam("instance_id") String instance_id, Provision provision) {
        String result = "{\"dashboard_url\":\"http://secured.url/test-string\",\"operation\":\"task_10\"}";

        try {
            ArrayList<NameValuePair> postParameters;
            postParameters = new ArrayList();
            postParameters.add(new BasicNameValuePair("name", "testApp1"));
            postParameters.add(new BasicNameValuePair("description", "testApp1"));
            postParameters.add(new BasicNameValuePair("plan_id", instance_id));

            //looks like I need to have an account ready first, and I don't see a REST api for create account, so I manually create one "brokerGroup", id is "5"
            int account_id = 5;
            String ampUrl = "/admin/api/accounts/" + account_id + "/applications.xml";

            result = restWsCall(ampUrl, postParameters, "POST");
            logInfo("user is created : " + result);
            String user_key = result.substring(result.indexOf("<user_key>") + "<user_key>".length(), result.indexOf("</user_key>"));
            logInfo("user_key : " + user_key);

        } catch (IOException ex) {
            Logger.getLogger(SecuredMarketBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(SecuredMarketBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(SecuredMarketBroker.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        //logInfo("!!!!!!!!!!!!!!!binding instance_id: " + instance_id);
        //logInfo("binding binding_id: " + binding_id);
        //result = "{/\"credentials/\":{/\"uri/\":/\"mysql://mysqluser:pass@mysqlhost:3306/dbname/\",/\"username/\":/\"mysqluser/\",/\"password/\":/\"pass/\",/\"host/\":/\"mysqlhost/\",/\"port/\":3306,/\"database/\":/\"dbname/\"}}";
        //result = "{/\"credentials/\":{/\"ttt/\":/\"12345678901111111111111111111111111111111111/\",/\"username/\":/\"mysqluser/\",/\"password/\":/\"pass/\",/\"hhhh/\":/\"222222222/\",/\"port/\":3306,/\"database/\":/\"dbname/\"}}";
        //result = "{\"dashboard_url\":\"\",\"operation\":\"task_10\"}";
        //result = "{\"test\":\"111111111111111\",\"test2\":\"task_10\"}";
        String result = responseStr;
        return result;
    }

    @DELETE
    @Path("/service_instances")
    @Consumes({"*/*"})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public String deProvisioning2(@QueryParam("instance_id") String instance_id) {
        logInfo("deProvisioning2 /service_instances: " + instance_id);
        String result = "{}";
        return result;
    }

    @PUT
    @Path("/service_instances/{instance_id}/service_bindings/{binding_id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public synchronized String binding(String inputStr) {
        //public String binding(@PathParam("instance_id") String instance_id, @PathParam("binding_id") String binding_id) {
        logInfo("binding inputStr: " + inputStr);
        String guid = inputStr.substring(inputStr.indexOf("app_guid\":\"") + "app_guid\":\"".length(), inputStr.indexOf("\",\"plan_id"));
        logInfo("binding guid: " + guid);
        String planId = inputStr.substring(inputStr.indexOf("plan_id\":\"") + "plan_id\":\"".length(), inputStr.indexOf("\",\"service_id"));
        logInfo("binding planId: " + planId);
        String serviceId = inputStr.substring(inputStr.indexOf("service_id\":\"") + "service_id\":\"".length(), inputStr.indexOf("\",\"bind_resource"));
        logInfo("binding serviceId: " + serviceId);

        //looks like I need to have an account ready first, and I don't see a REST api for create account, so I manually create one "brokerGroup", id is "5"
        int account_id = 5;
        String user_key;
        String result = "{}";
        try {
            user_key = searchUserKeyBasedOnGUID(guid, account_id);
            String endpoint = searchEndPointBasedOnServiceId(serviceId);
            result = "{\"credentials\":{\"url\":\"" + endpoint + "\",\"user_key\":\"" + user_key + "\"}}";
            logInfo("binding result: " + result);
        } catch (IOException ex) {
            Logger.getLogger(SecuredMarketBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(SecuredMarketBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(SecuredMarketBroker.class.getName()).log(Level.SEVERE, null, ex);
        }

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

    public static HttpClient createHttpClient_AcceptsUntrustedCerts() {
        try {

            HttpClientBuilder b = HttpClientBuilder.create();

            // setup a Trust Strategy that allows all certificates.
            //
            SSLContext sslContext;
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] arg0, String arg1) {
                    return true;
                }
            }).build();
            b.setSslcontext(sslContext);

            // don't check Hostnames, either.
            //      -- use SSLConnectionSocketFactory.getDefaultHostnameVerifier(), if you don't want to weaken
            HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

            // here's the special part:
            //      -- need to create an SSL Socket Factory, to use our weakened "trust strategy";
            //      -- and create a Registry, to register it.
            //
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", sslSocketFactory).build();

            // now, we create connection-manager using our Registry.
            //      -- allows multi-threaded use
            PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            b.setConnectionManager(connMgr);

            // finally, build the HttpClient;
            //      -- done!
            HttpClient client = b.build();
            return client;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(SecuredMarketBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException ex) {
            Logger.getLogger(SecuredMarketBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyManagementException ex) {
            Logger.getLogger(SecuredMarketBroker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private URIBuilder getUriBuilder(Object... path) {
        String accessToken = System.getenv("ACCESS_TOKEN");
        String ampAddress = System.getenv("AMP_ADDRESS");
        logInfo("accessToken: " + accessToken);
        logInfo("ampAddress: " + ampAddress);

        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("https");
        uriBuilder.setHost(ampAddress);

        uriBuilder.setPort(443);
        uriBuilder.addParameter("access_token", accessToken);

        StringWriter stringWriter = new StringWriter();
        for (Object part : path) {
            stringWriter.append('/').append(String.valueOf(part));
        }
        uriBuilder.setPath(stringWriter.toString());
        return uriBuilder;
    }

    private static boolean isError(HttpResponse response) {
        if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
            return true;
        } else {
            return false;
        }
    }

    public static List<Map<String, Object>> getList(JSONArray jsonArray) throws JSONException {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int index = 0; index < jsonArray.length(); index++) {
            Map<String, Object> map = new HashMap<String, Object>();
            JSONObject jsonObject = jsonArray.getJSONObject(index);
            for (Iterator<?> jsonIterator = jsonObject.keys(); jsonIterator.hasNext();) {
                String jsonKey = (String) jsonIterator.next();
                map.put(jsonKey, jsonObject.get(jsonKey));
            }
            list.add(map);
        }
        return list;
    }

    private String restWsCall(String inputURL, ArrayList<NameValuePair> postParameters, String httpMethod) throws IOException, JSONException, URISyntaxException {
        HttpClient client = createHttpClient_AcceptsUntrustedCerts();
        URIBuilder uriBuilder = getUriBuilder(inputURL);

        HttpRequestBase request;
        if ("PATCH".equals(httpMethod)) {
            request = new HttpPatch(uriBuilder.build());
            ((HttpEntityEnclosingRequestBase) request).setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
        } else if ("PUT".equals(httpMethod)) {
            request = new HttpPut(uriBuilder.build());
            ((HttpEntityEnclosingRequestBase) request).setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
        } else if ("POST".equals(httpMethod)) {
            request = new HttpPost(uriBuilder.build());
            ((HttpEntityEnclosingRequestBase) request).setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
        } else {
            //else treat it as "GET" then
            request = new HttpGet(uriBuilder.build());
        }

        logInfo("Executing REST:  " + request);
        HttpResponse response = client.execute(request);
        if (isError(response)) {
            logInfo("!!!!Error status code: " + response.getStatusLine().getStatusCode());
        }
        String responseString = EntityUtils.toString(response.getEntity());
        return responseString;

    }

    private void createMappingRules(String serviceID) throws IOException, JSONException, URISyntaxException {
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

    private String searchServiceInstance(String inputServiceSystemName, int account_id) throws IOException, URISyntaxException, JSONException {
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
            String endpoint = searchEndPointBasedOnServiceId(serviceId);

            String url = endpoint + "/?user_key=" + user_key;

            return url;

        } else {
            logInfo("---------------------didn't found same system_name service : " + inputServiceSystemName);
            return "";
        }

    }

    private String searchUserKeyBasedOnServiceId(String serviceId, int accountId) throws IOException, URISyntaxException, JSONException {
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
    
    private String searchUserKeyBasedOnGUID(String guid, int accountId) throws IOException, URISyntaxException, JSONException {
        ArrayList<NameValuePair> postParameters;
        postParameters = new ArrayList();

        //String ampUrl = "/admin/api/accounts/" + accountId + "/applications.xml ";
        String ampUrl = "/admin/api/applications.xml";
        String result = restWsCall(ampUrl, postParameters, "GET");
        logInfo("application is listed : " + result);

        int i = result.indexOf(guid);
        String user_key = "";
        if (i > -1) {
            user_key = result.substring(result.lastIndexOf("<user_key>", i) + "<user_key>".length(), result.lastIndexOf("</user_key>", i));
            logInfo("---------------------found user_key for this service id : " + user_key);

        } else {
            logInfo("---------------------didn't found same service id in this application: " + guid);
        }
        return user_key;

    }    

    private String searchEndPointBasedOnServiceId(String serviceId) throws IOException, URISyntaxException, JSONException {
        ArrayList<NameValuePair> postParameters;
        postParameters = new ArrayList();

        String ampUrl = "/admin/api/services/" + serviceId + "/proxy.xml";
        String result = restWsCall(ampUrl, postParameters, "GET");
        logInfo("proxy is read: " + result);

        String endpoint = result.substring(result.indexOf("<endpoint>") + "<endpoint>".length(), result.indexOf("</endpoint>"));
        logInfo("---------------------found endpoint for this service id : " + endpoint);

        return endpoint;

    }

}
