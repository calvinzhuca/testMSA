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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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

        String catalog = "{\"services\":[{\"name\":\"three-scales-service\",\"id\":\"serviceUUID\",\"description\":\"secure service 3scales broker implementation\",\"bindable\":true,\"metadata\":{\"displayName\":\"secure-service-3scales-broker\",\"documentationUrl\":\"https://access.qa.redhat.com/documentation/en-us/reference_architectures/2017/html-single/api_management_with_red_hat_3scale_api_management_platform\",\"longDescription\":\"A broker that secures input URL through 3scales-AMP\",\"parameters\":[{\"input_url\":{\"title\":\"input url\",\"type\":\"string\",\"default\":\"https://echo-api.3scale.net:443\"}},{\"username\":{\"title\":\"User Name\",\"type\":\"string\",\"default\":\"admin\"}},{\"password\":{\"title\":\"password\",\"type\":\"string\"}}]},\"plans\":[{\"id\":\"gold-plan-id\",\"name\":\"test-plan\",\"description\":\"3scale plan description ...\",\"free\":true,\"schemas\":{\"service_instance\":{\"create\":{\"parameters\":{\"$schema\":\"http://json-schema.org/draft-04/schema\",\"additionalProperties\":false,\"properties\":{\"amp_admin_user\":{\"default\":\"admin\",\"title\":\"3scale AMP User\",\"type\":\"string\"},\"amp_admin_pass\":{\"default\":\"password1\",\"title\":\"3scale AMP Password\",\"type\":\"string\"},\"amp_url\":{\"title\":\"input url for secure\",\"type\":\"string\",\"default\":\"https://echo-api.3scale.net:443\"}},\"required\":[\"amp_admin_user\",\"amp_admin_pass\",\"amp_url\"],\"type\":\"object\"}},\"update\":{}},\"service_binding\":{\"create\":{\"parameters\":{\"$schema\":\"http://json-schema.org/draft-04/schema\",\"additionalProperties\":false,\"properties\":{\"properties\":{\"amp_admin_user\":{\"default\":\"admin\",\"title\":\"3scale AMP User\",\"type\":\"string\"},\"amp_admin_pass\":{\"default\":\"password1\",\"title\":\"3scale AMP User Password\",\"type\":\"string\"},\"amp_url\":{\"default\":\"amp\",\"title\":\"input url for secure\",\"type\":\"string\"}}},\"type\":\"object\"}}}}}]}]}";
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
    public String provisioning(@PathParam("instance_id") String instance_id, Provision provision) {
        //public String provisioning( String testString) {
        logInfo("!!!!!!!!!!provisioning Transaction123 /service_instances/{instance_id} : " + instance_id);
        logInfo("provision.getService_id() : " + provision.getService_id());
        logInfo("provision.getOrganization_guid() : " + provision.getOrganization_guid());
        logInfo("provision.getParameters().getAmp_url() : " + provision.getParameters().getAmp_url());
        logInfo("provision.getParameters().getAmp_admin_user() : " + provision.getParameters().getAmp_admin_user());
        logInfo("provision.getParameters().getAmp_admin_pass() : " + provision.getParameters().getAmp_admin_pass());
        try {
            
            String servicesList = ampSearchService(provision.getParameters().getAmp_url());
           logInfo("servicesList : " + servicesList);
             
            servicesList = ampCreateService(provision.getParameters().getAmp_url());
            logInfo("servicesList : " + servicesList);
        } catch (IOException ex) {
            Logger.getLogger(ThreeScalesBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(ThreeScalesBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(ThreeScalesBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (HttpErrorException ex) {
            Logger.getLogger(ThreeScalesBroker.class.getName()).log(Level.SEVERE, null, ex);
        }
        String result = "{\"dashboard_url\":\"http://secured.url/test-string\",\"operation\":\"task_10\"}";
//	     String result = "{\"kind\":\"ServiceInstanceList\",\"apiVersion\":\"sdkbroker.broker.k8s.io/v1alpha1\",\"metadata\":{\"selfLink\":\"/apis/sdkbroker.broker.k8s.io/v1alpha1/namespaces/brokersdk/serviceinstances\",\"resourceVersion\":\"473\"},\"items\":[]}";
        logInfo("provisioning result" + result);
        return result;
    }

    @PUT
    @Path("/service_instances2/{instance_id}")
    @Consumes({"*/*"})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public String provisioning2(@QueryParam("instance_id") String instance_id, final Transaction transaction) {
        logInfo("!!!!!!!!!!provisioning2 /service_instances2 instance_id: " + instance_id);
        //  String result = "test";
        String result = "{\"kind\":\"ServiceInstanceList\",\"apiVersion\":\"sdkbroker.broker.k8s.io/v1alpha1\",\"metadata\":{\"selfLink\":\"/apis/sdkbroker.broker.k8s.io/v1alpha1/namespaces/brokersdk/serviceinstances\",\"resourceVersion\":\"473\"},\"items\":[]}";
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

    private String ampCreateService(String inputURL) throws IOException, JSONException, URISyntaxException, HttpErrorException {
        HttpClient client = createHttpClient_AcceptsUntrustedCerts();
        URIBuilder uriBuilder = getUriBuilder("/admin/api/services.xml");

        //TODO? will the name be another parameter? 
        //uriBuilder.addParameter("name", "testApi");
        //uriBuilder.addParameter("system_name", "testApi");

        ArrayList<NameValuePair> postParameters;
        postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("name", "testApi"));
        postParameters.add(new BasicNameValuePair("system_name", "testApi"));

        //HttpGet get = new HttpGet(uriBuilder.build());
        HttpPost request = new HttpPost(uriBuilder.build());
        request.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
        logInfo("Executing ampCreateService " + request);
        logInfo("Secure this URL " + inputURL);
        HttpResponse response = client.execute(request);
        if (isError(response)) {
            throw new HttpErrorException(response);
        } else {
            String responseString = EntityUtils.toString(response.getEntity());
            //JSONArray jsonArray = new JSONArray(responseString);
            //List<Map<String, Object>> products = getList(jsonArray);
            return responseString;
        }
    }
    
    private String ampSearchService(String inputURL) throws IOException, JSONException, URISyntaxException, HttpErrorException {
        HttpClient client = createHttpClient_AcceptsUntrustedCerts();
        URIBuilder uriBuilder = getUriBuilder("/admin/api/services.xml");

        //TODO? will the name be another parameter? 
        //uriBuilder.addParameter("name", "testApi");
        //uriBuilder.addParameter("system_name", "testApi");


        HttpGet request = new HttpGet(uriBuilder.build());
        logInfo("Executing ampSearchService " + request);
        logInfo("Search this URL " + inputURL);
        HttpResponse response = client.execute(request);
        if (isError(response)) {
            throw new HttpErrorException(response);
        } else {
            String responseString = EntityUtils.toString(response.getEntity());
            //JSONArray jsonArray = new JSONArray(responseString);
            //List<Map<String, Object>> products = getList(jsonArray);
            return responseString;
        }
    }    

}
