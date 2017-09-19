package com.redhat.refarch.microservices.broker.service;

import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.Executor;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.redhat.refarch.microservices.broker.model.Result;
import com.redhat.refarch.microservices.broker.model.Result.Status;
import com.redhat.refarch.microservices.broker.model.Transaction;
import com.redhat.refarch.microservices.broker.model.Provision;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import org.apache.http.config.Registry;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;

@Path("/")
public class ThreeScalesBroker {

    private ManagedExecutorService executorService;

    private Logger logger = Logger.getLogger(getClass().getName());

    private static final Random random = new Random();

    @POST
    @Path("/process")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void process(final Transaction transaction, final @Suspended AsyncResponse asyncResponse) {

        Runnable runnable = () -> {
            try {
                final long sleep = 5000;
                logInfo("Will simulate credit card processing for " + sleep + " milliseconds");
                Thread.sleep(sleep);
                Result result = processSync(transaction);
                asyncResponse.resume(result);
            } catch (Exception e) {
                asyncResponse.resume(e);
            }
        };
        getExecutorService().execute(runnable);
    }

    private Executor getExecutorService() {
        if (executorService == null) {
            try {
                executorService = InitialContext.doLookup("java:comp/DefaultManagedExecutorService");
            } catch (NamingException e) {
                throw new WebApplicationException(e);
            }
        }
        return executorService;
    }

    private Result processSync(Transaction transaction) {
        Result result = new Result();
        result.setName(transaction.getCustomerName());
        result.setOrderNumber(transaction.getOrderNumber());
        logInfo("Asked to process credit card transaction: " + transaction);
        Calendar now = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(transaction.getExpYear(), transaction.getExpMonth(), 1);
        if (calendar.after(now)) {
            result.setTransactionNumber((long) (random.nextInt(9000000) + 1000000));
            result.setTransactionDate(now.getTime());
            result.setStatus(Status.SUCCESS);
        } else {
            result.setStatus(Status.FAILURE);
        }
        return result;
    }

    @POST
    @Path("/refund/{transactionNumber}")
    @Consumes({"*/*"})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void refund(@PathParam("transactionNumber") long transactionNumber) {
        logInfo("Asked to refund credit card transaction: " + transactionNumber);
    }

    private void logInfo(String message) {
        logger.log(Level.INFO, message);
    }

    @GET
    @Path("/catalog")
    @Consumes({"*/*"})
    @Produces({MediaType.APPLICATION_JSON})
    public Response getCatalog() {
        //  String catalog = "test";

//        String catalog = "{\"services\":[{\"name\":\"three-scales-service\",\"id\":\"serviceUUID\",\"description\":\"secure service 3scales broker implementation\",\"bindable\":true,\"metadata\":{\"displayName\":\"secure-service-3scales-broker\",\"documentationUrl\":\"https://access.qa.redhat.com/documentation/en-us/reference_architectures/2017/html-single/api_management_with_red_hat_3scale_api_management_platform\",\"longDescription\":\"A broker that secures input URL through 3scales-AMP\",\"parameters\":[{\"input_url\":{\"title\":\"input url\",\"type\":\"string\",\"default\":\"https://echo-api.3scale.net:443\"}},{\"username\":{\"title\":\"User Name\",\"type\":\"string\",\"default\":\"admin\"}},{\"password\":{\"title\":\"password\",\"type\":\"string\"}}]},\"plans\":[{\"id\":\"gold-plan-id\",\"name\":\"test-plan\",\"description\":\"3scale plan description ...\",\"free\":true,\"schemas\":{\"service_instance\":{\"create\":{\"parameters\":{\"$schema\":\"http://json-schema.org/draft-04/schema\",\"additionalProperties\":false,\"properties\":{\"amp_admin_user\":{\"default\":\"admin\",\"title\":\"3scale AMP User\",\"type\":\"string\"},\"amp_admin_pass\":{\"default\":\"password1\",\"title\":\"3scale AMP Password\",\"type\":\"string\"},\"amp_url\":{\"title\":\"input url for secure\",\"type\":\"string\",\"default\":\"https://echo-api.3scale.net:443\"}},\"required\":[\"amp_admin_user\",\"amp_admin_pass\",\"amp_url\"],\"type\":\"object\"}},\"update\":{}},\"service_binding\":{\"create\":{\"parameters\":{\"$schema\":\"http://json-schema.org/draft-04/schema\",\"additionalProperties\":false,\"properties\":{\"properties\":{\"amp_admin_user\":{\"default\":\"admin\",\"title\":\"3scale AMP User\",\"type\":\"string\"},\"amp_admin_pass\":{\"default\":\"password1\",\"title\":\"3scale AMP User Password\",\"type\":\"string\"},\"amp_url\":{\"default\":\"amp\",\"title\":\"input url for secure\",\"type\":\"string\"}}},\"type\":\"object\"}}}}}]}]}";
        String catalog = "{\"services\":[{\"name\":\"three-scales-service\",\"id\":\"serviceUUID\",\"description\":\"secure service 3scales broker implementation\",\"bindable\":true,\"metadata\":{\"displayName\":\"secure-service-3scales-broker\",\"documentationUrl\":\"https://access.qa.redhat.com/documentation/en-us/reference_architectures/2017/html-single/api_management_with_red_hat_3scale_api_management_platform\",\"longDescription\":\"A broker that secures input URL through 3scales-AMP\",\"parameters\":[{\"input_url\":{\"title\":\"input url\",\"type\":\"string\",\"default\":\"https://echo-api.3scale.net:443\"}},{\"username\":{\"title\":\"User Name\",\"type\":\"string\",\"default\":\"admin\"}},{\"password\":{\"title\":\"password\",\"type\":\"string\"}}]},\"plans\":[{\"id\":\"gold-plan-id\",\"name\":\"test-plan\",\"description\":\"3scale plan description ...\",\"free\":true,\"schemas\":{\"service_instance\":{\"create\":{\"parameters\":{\"$schema\":\"http://json-schema.org/draft-04/schema\",\"additionalProperties\":false,\"properties\":{\"service_name\":{\"default\":\"testapi\",\"title\":\"3scale service name\",\"type\":\"string\"},\"application_plan\":{\"default\":\"plan1\",\"title\":\"3scale application plan name\",\"type\":\"string\"},\"input_url\":{\"title\":\"input url for secure\",\"type\":\"string\",\"default\":\"https://echo-api.3scale.net:443\"},\"application_name\":{\"default\":\"testApp1\",\"title\":\"3scale application name under brokerGroup\",\"type\":\"string\"}},\"required\":[\"service_name\",\"application_plan\",\"input_url\",\"application_name\"],\"type\":\"object\"}},\"update\":{}},\"service_binding\":{\"create\":{\"parameters\":{\"$schema\":\"http://json-schema.org/draft-04/schema\",\"additionalProperties\":false,\"properties\":{\"properties\":{\"amp_admin_user\":{\"default\":\"admin\",\"title\":\"3scale AMP User\",\"type\":\"string\"},\"amp_admin_pass\":{\"default\":\"password1\",\"title\":\"3scale AMP User Password\",\"type\":\"string\"},\"amp_url\":{\"default\":\"amp\",\"title\":\"input url for secure\",\"type\":\"string\"}}},\"type\":\"object\"}}}}}]}]}";
        logInfo("getCatalog: " + catalog);

        return Response.ok(catalog, MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/catalog2")
    @Consumes({"*/*"})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCatalog2() {

        //return Response.status(Response.Status.NOT_FOUND).entity("Entity not found" ).build();
        return Response.serverError().entity("something is wrong example").build();
    }

    @GET
    @Path("/catalog3")
    @Consumes({"*/*"})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCatalog3() {

        return Response.status(Response.Status.NOT_FOUND).entity("Entity not found example").build();
    }

    @PUT
    @Path("/service_instances/{instance_id}")
    //@Consumes("application/x-www-form-urlencoded")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public synchronized String provisioning(@PathParam("instance_id") String instance_id, Provision provision) {
        //public String provisioning( String testString) {
        logInfo("!!!!!!!!!!provisioning /service_instances/{instance_id} : " + instance_id);
        logInfo("provision.getService_id() : " + provision.getService_id());
        logInfo("provision.getOrganization_guid() : " + provision.getOrganization_guid());
        logInfo("provision.getParameters().getService_name() : " + provision.getParameters().getService_name());
        logInfo("provision.getParameters().getApplication_plan() : " + provision.getParameters().getApplication_plan());
        logInfo("provision.getParameters().getInput_url() : " + provision.getParameters().getInput_url());
        logInfo("provision.getParameters().getApplication_name() : " + provision.getParameters().getApplication_name());

        String result = "{\"dashboard_url\":\"https://testapi-3scale-apicast-staging.middleware.ocp.cloud.lab.eng.bos.redhat.com:443/?user_key=2491bd25351aeb458fea55381b3d4560\",\"operation\":\"task_10\"}";
        String url = "";

        try {

            //looks like I need to have an account ready first, and I don't see a REST api for create account, so I manually create one "brokerGroup", id is "5"
            int account_id = 5;
            url = searchServiceInstance(provision.getParameters().getService_name(), account_id);
            //no existing service, need to create one
            if ("".equals(url)) {

                ArrayList<NameValuePair> postParameters;
                postParameters = new ArrayList();
                postParameters.add(new BasicNameValuePair("name", provision.getParameters().getService_name()));
                postParameters.add(new BasicNameValuePair("system_name", provision.getParameters().getService_name()));

                String ampUrl = "/admin/api/services.xml";
                result = restWsCall(ampUrl, postParameters, "POST");
                logInfo("---------------------services is created : " + result);
                String serviceID = result.substring(result.indexOf("<id>") + "<id>".length(),
                        result.indexOf("</id>"));
                logInfo("serviceID : " + serviceID);

                //create applicaiton plan
                ampUrl = "/admin/api/services/" + serviceID + "/application_plans.xml";
                postParameters = new ArrayList();
                postParameters.add(new BasicNameValuePair("name", provision.getParameters().getApplication_plan()));
                postParameters.add(new BasicNameValuePair("system_name", provision.getParameters().getApplication_plan()));
                result = restWsCall(ampUrl, postParameters, "POST");
                logInfo("---------------------application plan is created: " + result);
                String planID = result.substring(result.indexOf("<id>") + "<id>".length(),
                        result.indexOf("</id>"));
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
                String user_key = result.substring(result.indexOf("<user_key>") + "<user_key>".length(),
                        result.indexOf("</user_key>"));
                logInfo("user_key : " + user_key);

                String domain = "-3scale-apicast-staging.middleware.ocp.cloud.lab.eng.bos.redhat.com:443";
                url = "https://" + provision.getParameters().getService_name() + domain + "/?user_key=" + user_key;
                result = "{\"dashboard_url\":" + url + ",\"operation\":\"task_10\"}";

            }

        } catch (IOException ex) {
            Logger.getLogger(ThreeScalesBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(ThreeScalesBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(ThreeScalesBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (HttpErrorException ex) {
            Logger.getLogger(ThreeScalesBroker.class.getName()).log(Level.SEVERE, null, ex);
        }

        result = "{\"dashboard_url\":\"" + url + "\",\"operation\":\"task_10\"}";
        logInfo("provisioning result" + result);
        return result;
    }

    @PUT
    @Path("/create_user/{instance_id}")
    //@Consumes("application/x-www-form-urlencoded")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public String createUser(@PathParam("instance_id") String instance_id, Provision provision) {
        String result = "{\"dashboard_url\":\"http://secured.url/test-string\",\"operation\":\"task_10\"}";

        try {
            ArrayList<NameValuePair> postParameters;
            postParameters = new ArrayList();
            postParameters.add(new BasicNameValuePair("username", "tester2"));
            postParameters.add(new BasicNameValuePair("password", "password1"));
            postParameters.add(new BasicNameValuePair("email", "tester2@test.com"));

            //looks like I need to have an account ready first, and I don't see a REST api for create account, so I manually create one "brokerGroup", id is "5"
            int account_id = 5;
            String ampUrl = "/admin/api/accounts/" + account_id + "/users.xml";
            result = restWsCall(ampUrl, postParameters, "POST");
            logInfo("user is created : " + result);
            String tmpID = result.substring(result.indexOf("<id>") + "<id>".length(),
                    result.indexOf("</id>"));
            logInfo("user ID : " + tmpID);

            //now activate the new user, the default state is "pending"
            ampUrl = "/admin/api/accounts/" + account_id + "/users/" + tmpID + "/activate.xml";
            postParameters = new ArrayList();
            result = restWsCall(ampUrl, postParameters, "PUT");
            logInfo("user is activated : " + result);

        } catch (IOException ex) {
            Logger.getLogger(ThreeScalesBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(ThreeScalesBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(ThreeScalesBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (HttpErrorException ex) {
            Logger.getLogger(ThreeScalesBroker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
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
            String user_key = result.substring(result.indexOf("<user_key>") + "<user_key>".length(),
                    result.indexOf("</user_key>"));
            logInfo("user_key : " + user_key);

        } catch (IOException ex) {
            Logger.getLogger(ThreeScalesBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(ThreeScalesBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(ThreeScalesBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (HttpErrorException ex) {
            Logger.getLogger(ThreeScalesBroker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @DELETE
    @Path("/service_instances/{instance_id}")
    @Consumes({"*/*"})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public String deProvisioning(@PathParam("instance_id") String instance_id) {
        logInfo("!!!!!!!!!!!!!deProvisioning /service_instances/{instance_id}: " + instance_id);
        String result = "{}";
        logInfo("deProvisioning /service_instances/{instance_id} result: " + instance_id);
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
    @Consumes({"*/*"})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public String binding(@PathParam("instance_id") String instance_id, @PathParam("binding_id") String binding_id) {
        //  String result = "test";
        String result = "{}";
        logInfo("!!!!!!!!!!!!!!!binding instance_id: " + instance_id);
        logInfo("binding binding_id: " + binding_id);
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
                @Override
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
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslSocketFactory)
                    .build();

            // now, we create connection-manager using our Registry.
            //      -- allows multi-threaded use
            PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            b.setConnectionManager(connMgr);

            // finally, build the HttpClient;
            //      -- done!
            HttpClient client = b.build();
            return client;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ThreeScalesBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException ex) {
            Logger.getLogger(ThreeScalesBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyManagementException ex) {
            Logger.getLogger(ThreeScalesBroker.class.getName()).log(Level.SEVERE, null, ex);
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

    private String restWsCall(String inputURL, ArrayList<NameValuePair> postParameters, String httpMethod) throws IOException, JSONException, URISyntaxException, HttpErrorException {
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

    private void createMappingRules(String serviceID) throws IOException, JSONException, URISyntaxException, HttpErrorException {
        //create mapping rule, first need to get the "hit" metric id. 
        String ampUrl = "/admin/api/services/" + serviceID + "/metrics.xml";
        String result = restWsCall(ampUrl, null, "GET");
        logInfo("get metric result : " + result);
        String metricID = result.substring(result.indexOf("<id>") + "<id>".length(),
                result.indexOf("</id>"));
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

    /*
    private boolean searchServiceInstance(String inputServiceSystemName) throws IOException, URISyntaxException, JSONException, HttpErrorException {
        ArrayList<NameValuePair> postParameters;
        postParameters = new ArrayList();

        String ampUrl = "/admin/api/services.xml";
        String result = restWsCall(ampUrl, postParameters, "GET");
        logInfo("services are listed : " + result);

        int i = result.indexOf("<system_name>");
        boolean found = false;
        while (i != -1) {
            String system_name = result.substring(result.indexOf("<system_name>",i) + "<system_name>".length(),
                    result.indexOf("</system_name>",i));
            logInfo("system_name : " + system_name);

            if (system_name.equals(inputServiceSystemName)) {
                logInfo("found same system_name service : " + system_name);
                found = true;
                i = -1;
            } else {
                int j = result.indexOf("</system_name>", i) + "</system_name>".length();
                logInfo("j : " + j);
                i = result.indexOf("<system_name>", j);
                logInfo("i : " + i);
            }
        }

        return found;
    }
     */
    private String searchServiceInstance(String inputServiceSystemName, int account_id) throws IOException, URISyntaxException, JSONException, HttpErrorException {
        ArrayList<NameValuePair> postParameters;
        postParameters = new ArrayList();

        String ampUrl = "/admin/api/services.xml";
        String result = restWsCall(ampUrl, postParameters, "GET");
        logInfo("services are listed : " + result);

        int i = result.indexOf("<system_name>" + inputServiceSystemName + "</system_name>");

        if (i > -1) {
            String serviceId = result.substring(result.lastIndexOf("<service><id>", i) + "<service><id>".length(),
                    result.lastIndexOf("</id>", i));
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

    private String searchUserKeyBasedOnServiceId(String serviceId, int accountId) throws IOException, URISyntaxException, JSONException, HttpErrorException {
        ArrayList<NameValuePair> postParameters;
        postParameters = new ArrayList();

        //String ampUrl = "/admin/api/accounts/" + accountId + "/applications.xml ";
        String ampUrl = "/admin/api/applications.xml";
        String result = restWsCall(ampUrl, postParameters, "GET");
        logInfo("application is listed : " + result);

        int i = result.indexOf("<service_id>" + serviceId + "</service_id>");
        String user_key = "";
        if (i > -1) {
            user_key = result.substring(result.indexOf("<user_key>", i) + "<user_key>".length(),
                    result.indexOf("</user_key>", i));
            logInfo("---------------------found user_key for this service id : " + user_key);

        } else {
            logInfo("---------------------didn't found same service id in this application: " + serviceId);
        }
        return user_key;

    }

    private String searchEndPointBasedOnServiceId(String serviceId) throws IOException, URISyntaxException, JSONException, HttpErrorException {
        ArrayList<NameValuePair> postParameters;
        postParameters = new ArrayList();

        String ampUrl = "/admin/api/services/" + serviceId + "/proxy.xml";
        String result = restWsCall(ampUrl, postParameters, "GET");
        logInfo("proxy is read: " + result);

        String endpoint = result.substring(result.indexOf("<endpoint>") + "<endpoint>".length(),
                result.indexOf("</endpoint>"));
        logInfo("---------------------found endpoint for this service id : " + endpoint);

        return endpoint;

    }

}
