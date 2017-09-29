package com.redhat.syseng.openshift.service.broker.service;

import com.redhat.syseng.openshift.service.broker.model.Provision;

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

import java.io.BufferedReader;
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
import java.util.stream.Stream;

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

@Path( "/v2" )
public class ThreeScalesBroker
{

	private Logger logger = Logger.getLogger( getClass().getName() );

	private static final Random random = new Random();

	private void logInfo(String message)
	{
		logger.log( Level.INFO, message );
	}

	@GET
	@Path( "/catalog" )
	@Consumes( {"*/*"} )
	@Produces( {MediaType.APPLICATION_JSON} )
	public Response getCatalog()
	{
		BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( getClass().getResourceAsStream( "/catalog.json" ) ) );
		String catalog = bufferedReader.lines().collect( Collectors.joining( "\n" ) );
		logInfo( "catalog:\n\n" + catalog );
		return Response.ok( catalog, MediaType.APPLICATION_JSON ).build();
	}

	@PUT
	@Path( "/service_instances/{instance_id}" )
	@Consumes( {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML} )
	@Produces( {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML} )
	public synchronized String provisioning(@PathParam( "instance_id" ) String instance_id, Provision provision)
	{
		//public String provisioning( String testString) {
		logInfo( "!!!!!!!!!!provisioning /service_instances/{instance_id} : " + instance_id );
		logInfo( "provision.getService_id() : " + provision.getService_id() );
		logInfo( "provision.getOrganization_guid() : " + provision.getOrganization_guid() );
		logInfo( "provision.getParameters().getService_name() : " + provision.getParameters().getService_name() );
		logInfo( "provision.getParameters().getApplication_plan() : " + provision.getParameters().getApplication_plan() );
		logInfo( "provision.getParameters().getInput_url() : " + provision.getParameters().getInput_url() );
		logInfo( "provision.getParameters().getApplication_name() : " + provision.getParameters().getApplication_name() );

		String result = "{\"dashboard_url\":\"https://testapi-3scale-apicast-staging.middleware.ocp.cloud.lab.eng.bos.redhat.com:443/?user_key=2491bd25351aeb458fea55381b3d4560\",\"operation\":\"task_10\"}";
		String url = "";

		try
		{

			//looks like I need to have an account ready first, and I don't see a REST api for create account, so I manually create one "brokerGroup", id is "5"
			int account_id = 5;
			url = searchServiceInstance( provision.getParameters().getService_name(), account_id );
			//no existing service, need to create one
			if( "".equals( url ) )
			{

				ArrayList<NameValuePair> postParameters;
				postParameters = new ArrayList();
				postParameters.add( new BasicNameValuePair( "name", provision.getParameters().getService_name() ) );
				postParameters.add( new BasicNameValuePair( "system_name", provision.getParameters().getService_name() ) );

				String ampUrl = "/admin/api/services.xml";
				result = restWsCall( ampUrl, postParameters, "POST" );
				logInfo( "---------------------services is created : " + result );
				String serviceID = result.substring( result.indexOf( "<id>" ) + "<id>".length(), result.indexOf( "</id>" ) );
				logInfo( "serviceID : " + serviceID );

				//create applicaiton plan
				ampUrl = "/admin/api/services/" + serviceID + "/application_plans.xml";
				postParameters = new ArrayList();
				postParameters.add( new BasicNameValuePair( "name", provision.getParameters().getApplication_plan() ) );
				postParameters.add( new BasicNameValuePair( "system_name", provision.getParameters().getApplication_plan() ) );
				result = restWsCall( ampUrl, postParameters, "POST" );
				logInfo( "---------------------application plan is created: " + result );
				String planID = result.substring( result.indexOf( "<id>" ) + "<id>".length(), result.indexOf( "</id>" ) );
				logInfo( "planID : " + planID );

				createMappingRules( serviceID );

				//API integration
				ampUrl = "/admin/api/services/" + serviceID + "/proxy.xml";
				postParameters = new ArrayList();
				postParameters.add( new BasicNameValuePair( "service_id", serviceID ) );
				postParameters.add( new BasicNameValuePair( "api_backend", provision.getParameters().getInput_url() ) );
				result = restWsCall( ampUrl, postParameters, "PATCH" );
				logInfo( "---------------------integration result : " + result );

				//create Application to use the Plan, which will generate a valid user_key
				postParameters = new ArrayList();
				postParameters.add( new BasicNameValuePair( "name", provision.getParameters().getApplication_name() ) );
				postParameters.add( new BasicNameValuePair( "description", provision.getParameters().getApplication_name() ) );
				postParameters.add( new BasicNameValuePair( "plan_id", planID ) );

				ampUrl = "/admin/api/accounts/" + account_id + "/applications.xml";

				//after this step, in the API Integration page, the user_key will automatically replaced with the new one created below
				result = restWsCall( ampUrl, postParameters, "POST" );
				logInfo( "---------------------application is created : " + result );
				String user_key = result.substring( result.indexOf( "<user_key>" ) + "<user_key>".length(), result.indexOf( "</user_key>" ) );
				logInfo( "user_key : " + user_key );

				String domain = "-3scale-apicast-staging.middleware.ocp.cloud.lab.eng.bos.redhat.com:443";
				url = "https://" + provision.getParameters().getService_name() + domain + "/?user_key=" + user_key;
				result = "{\"dashboard_url\":" + url + ",\"operation\":\"task_10\"}";

			}

		}
		catch( IOException ex )
		{
			Logger.getLogger( ThreeScalesBroker.class.getName() ).log( Level.SEVERE, null, ex );
		}
		catch( JSONException ex )
		{
			Logger.getLogger( ThreeScalesBroker.class.getName() ).log( Level.SEVERE, null, ex );
		}
		catch( URISyntaxException ex )
		{
			Logger.getLogger( ThreeScalesBroker.class.getName() ).log( Level.SEVERE, null, ex );
		}

		result = "{\"dashboard_url\":\"" + url + "\",\"operation\":\"task_10\"}";
		logInfo( "provisioning result" + result );
		return result;
	}

	@PUT
	@Path( "/create_user/{instance_id}" )
	//@Consumes("application/x-www-form-urlencoded")
	@Consumes( {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML} )
	@Produces( {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML} )
	public String createUser(@PathParam( "instance_id" ) String instance_id, Provision provision)
	{
		String result = "{\"dashboard_url\":\"http://secured.url/test-string\",\"operation\":\"task_10\"}";

		try
		{
			ArrayList<NameValuePair> postParameters;
			postParameters = new ArrayList();
			postParameters.add( new BasicNameValuePair( "username", "tester2" ) );
			postParameters.add( new BasicNameValuePair( "password", "password1" ) );
			postParameters.add( new BasicNameValuePair( "email", "tester2@test.com" ) );

			//looks like I need to have an account ready first, and I don't see a REST api for create account, so I manually create one "brokerGroup", id is "5"
			int account_id = 5;
			String ampUrl = "/admin/api/accounts/" + account_id + "/users.xml";
			result = restWsCall( ampUrl, postParameters, "POST" );
			logInfo( "user is created : " + result );
			String tmpID = result.substring( result.indexOf( "<id>" ) + "<id>".length(), result.indexOf( "</id>" ) );
			logInfo( "user ID : " + tmpID );

			//now activate the new user, the default state is "pending"
			ampUrl = "/admin/api/accounts/" + account_id + "/users/" + tmpID + "/activate.xml";
			postParameters = new ArrayList();
			result = restWsCall( ampUrl, postParameters, "PUT" );
			logInfo( "user is activated : " + result );

		}
		catch( IOException ex )
		{
			Logger.getLogger( ThreeScalesBroker.class.getName() ).log( Level.SEVERE, null, ex );
		}
		catch( JSONException ex )
		{
			Logger.getLogger( ThreeScalesBroker.class.getName() ).log( Level.SEVERE, null, ex );
		}
		catch( URISyntaxException ex )
		{
			Logger.getLogger( ThreeScalesBroker.class.getName() ).log( Level.SEVERE, null, ex );
		}
		return result;
	}

	@PUT
	@Path( "/create_application/{instance_id}" )
	//@Consumes("application/x-www-form-urlencoded")
	@Consumes( {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML} )
	@Produces( {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML} )
	public String createApplication(@PathParam( "instance_id" ) String instance_id, Provision provision)
	{
		String result = "{\"dashboard_url\":\"http://secured.url/test-string\",\"operation\":\"task_10\"}";

		try
		{
			ArrayList<NameValuePair> postParameters;
			postParameters = new ArrayList();
			postParameters.add( new BasicNameValuePair( "name", "testApp1" ) );
			postParameters.add( new BasicNameValuePair( "description", "testApp1" ) );
			postParameters.add( new BasicNameValuePair( "plan_id", instance_id ) );

			//looks like I need to have an account ready first, and I don't see a REST api for create account, so I manually create one "brokerGroup", id is "5"
			int account_id = 5;
			String ampUrl = "/admin/api/accounts/" + account_id + "/applications.xml";

			result = restWsCall( ampUrl, postParameters, "POST" );
			logInfo( "user is created : " + result );
			String user_key = result.substring( result.indexOf( "<user_key>" ) + "<user_key>".length(), result.indexOf( "</user_key>" ) );
			logInfo( "user_key : " + user_key );

		}
		catch( IOException ex )
		{
			Logger.getLogger( ThreeScalesBroker.class.getName() ).log( Level.SEVERE, null, ex );
		}
		catch( JSONException ex )
		{
			Logger.getLogger( ThreeScalesBroker.class.getName() ).log( Level.SEVERE, null, ex );
		}
		catch( URISyntaxException ex )
		{
			Logger.getLogger( ThreeScalesBroker.class.getName() ).log( Level.SEVERE, null, ex );
		}
		return result;
	}

	@DELETE
	@Path( "/service_instances/{instance_id}" )
	@Consumes( {"*/*"} )
	@Produces( {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML} )
	public String deProvisioning(@PathParam( "instance_id" ) String instance_id)
	{
		logInfo( "!!!!!!!!!!!!!deProvisioning /service_instances/{instance_id}: " + instance_id );
		String result = "{}";
		logInfo( "deProvisioning /service_instances/{instance_id} result: " + instance_id );
		return result;
	}

	@DELETE
	@Path( "/service_instances" )
	@Consumes( {"*/*"} )
	@Produces( {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML} )
	public String deProvisioning2(@QueryParam( "instance_id" ) String instance_id)
	{
		logInfo( "deProvisioning2 /service_instances: " + instance_id );
		String result = "{}";
		return result;
	}

    @PUT
    @Path("/service_instances/{instance_id}/service_bindings/{binding_id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public synchronized String binding( String testString) {
    //public String binding(@PathParam("instance_id") String instance_id, @PathParam("binding_id") String binding_id) {
        //  String result = "test";
        String result = "";
        logInfo("binding testString 2: " + testString);
        //logInfo("!!!!!!!!!!!!!!!binding instance_id: " + instance_id);
        //logInfo("binding binding_id: " + binding_id);
        //result = "{/\"credentials/\":{/\"uri/\":/\"mysql://mysqluser:pass@mysqlhost:3306/dbname/\",/\"username/\":/\"mysqluser/\",/\"password/\":/\"pass/\",/\"host/\":/\"mysqlhost/\",/\"port/\":3306,/\"database/\":/\"dbname/\"}}";
        result = "{/\"credentials/\":{/\"ttt/\":/\"12345678901111111111111111111111111111111111/\",/\"username/\":/\"mysqluser/\",/\"password/\":/\"pass/\",/\"hhhh/\":/\"222222222/\",/\"port/\":3306,/\"database/\":/\"dbname/\"}}";
        //result = "{\"dashboard_url\":\"\",\"operation\":\"task_10\"}";
        //result = "{\"test\":\"111111111111111\",\"test2\":\"task_10\"}";
        return result;
    } 

	@DELETE
	@Path( "/service_instances/{instance_id}/service_bindings/{binding_id}" )
	@Consumes( {"*/*"} )
	@Produces( {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML} )
	public String unBinding(@PathParam( "instance_id" ) String instance_id, @PathParam( "binding_id" ) String binding_id)
	{
		//  String result = "test";
		String result = "{}";
		logInfo( "!!!!!!!!!!!!!!!!unBinding instance_id: " + instance_id );
		logInfo( "unBinding binding_id: " + binding_id );
		return result;
	}

	public static HttpClient createHttpClient_AcceptsUntrustedCerts()
	{
		try
		{

			HttpClientBuilder b = HttpClientBuilder.create();

			// setup a Trust Strategy that allows all certificates.
			//
			SSLContext sslContext;
			sslContext = new SSLContextBuilder().loadTrustMaterial( null, new TrustStrategy()
			{
				public boolean isTrusted(X509Certificate[] arg0, String arg1)
				{
					return true;
				}
			} ).build();
			b.setSslcontext( sslContext );

			// don't check Hostnames, either.
			//      -- use SSLConnectionSocketFactory.getDefaultHostnameVerifier(), if you don't want to weaken
			HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

			// here's the special part:
			//      -- need to create an SSL Socket Factory, to use our weakened "trust strategy";
			//      -- and create a Registry, to register it.
			//
			SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory( sslContext, hostnameVerifier );
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register( "http", PlainConnectionSocketFactory.getSocketFactory() ).register( "https", sslSocketFactory ).build();

			// now, we create connection-manager using our Registry.
			//      -- allows multi-threaded use
			PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager( socketFactoryRegistry );
			b.setConnectionManager( connMgr );

			// finally, build the HttpClient;
			//      -- done!
			HttpClient client = b.build();
			return client;
		}
		catch( NoSuchAlgorithmException ex )
		{
			Logger.getLogger( ThreeScalesBroker.class.getName() ).log( Level.SEVERE, null, ex );
		}
		catch( KeyStoreException ex )
		{
			Logger.getLogger( ThreeScalesBroker.class.getName() ).log( Level.SEVERE, null, ex );
		}
		catch( KeyManagementException ex )
		{
			Logger.getLogger( ThreeScalesBroker.class.getName() ).log( Level.SEVERE, null, ex );
		}
		return null;
	}

	private URIBuilder getUriBuilder(Object... path)
	{
		String accessToken = System.getenv( "ACCESS_TOKEN" );
		String ampAddress = System.getenv( "AMP_ADDRESS" );
		logInfo( "accessToken: " + accessToken );
		logInfo( "ampAddress: " + ampAddress );

		URIBuilder uriBuilder = new URIBuilder();
		uriBuilder.setScheme( "https" );
		uriBuilder.setHost( ampAddress );

		uriBuilder.setPort( 443 );
		uriBuilder.addParameter( "access_token", accessToken );

		StringWriter stringWriter = new StringWriter();
		for( Object part : path )
		{
			stringWriter.append( '/' ).append( String.valueOf( part ) );
		}
		uriBuilder.setPath( stringWriter.toString() );
		return uriBuilder;
	}

	private static boolean isError(HttpResponse response)
	{
		if( response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public static List<Map<String, Object>> getList(JSONArray jsonArray) throws JSONException
	{
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for( int index = 0; index < jsonArray.length(); index++ )
		{
			Map<String, Object> map = new HashMap<String, Object>();
			JSONObject jsonObject = jsonArray.getJSONObject( index );
			for( Iterator<?> jsonIterator = jsonObject.keys(); jsonIterator.hasNext(); )
			{
				String jsonKey = (String)jsonIterator.next();
				map.put( jsonKey, jsonObject.get( jsonKey ) );
			}
			list.add( map );
		}
		return list;
	}

	private String restWsCall(String inputURL, ArrayList<NameValuePair> postParameters, String httpMethod) throws IOException, JSONException, URISyntaxException
	{
		HttpClient client = createHttpClient_AcceptsUntrustedCerts();
		URIBuilder uriBuilder = getUriBuilder( inputURL );

		HttpRequestBase request;
		if( "PATCH".equals( httpMethod ) )
		{
			request = new HttpPatch( uriBuilder.build() );
			((HttpEntityEnclosingRequestBase)request).setEntity( new UrlEncodedFormEntity( postParameters, "UTF-8" ) );
		}
		else if( "PUT".equals( httpMethod ) )
		{
			request = new HttpPut( uriBuilder.build() );
			((HttpEntityEnclosingRequestBase)request).setEntity( new UrlEncodedFormEntity( postParameters, "UTF-8" ) );
		}
		else if( "POST".equals( httpMethod ) )
		{
			request = new HttpPost( uriBuilder.build() );
			((HttpEntityEnclosingRequestBase)request).setEntity( new UrlEncodedFormEntity( postParameters, "UTF-8" ) );
		}
		else
		{
			//else treat it as "GET" then
			request = new HttpGet( uriBuilder.build() );
		}

		logInfo( "Executing REST:  " + request );
		HttpResponse response = client.execute( request );
		if( isError( response ) )
		{
			logInfo( "!!!!Error status code: " + response.getStatusLine().getStatusCode() );
		}
		String responseString = EntityUtils.toString( response.getEntity() );
		return responseString;

	}

	private void createMappingRules(String serviceID) throws IOException, JSONException, URISyntaxException
	{
		//create mapping rule, first need to get the "hit" metric id.
		String ampUrl = "/admin/api/services/" + serviceID + "/metrics.xml";
		String result = restWsCall( ampUrl, null, "GET" );
		logInfo( "get metric result : " + result );
		String metricID = result.substring( result.indexOf( "<id>" ) + "<id>".length(), result.indexOf( "</id>" ) );
		logInfo( "metricID : " + metricID );

		ampUrl = "/admin/api/services/" + serviceID + "/proxy/mapping_rules.xml";
		ArrayList<NameValuePair> postParameters;
		postParameters = new ArrayList();

		//now create mapping rule for POST under metric "hit"
		postParameters.add( new BasicNameValuePair( "pattern", "/" ) );
		postParameters.add( new BasicNameValuePair( "delta", "1" ) );
		postParameters.add( new BasicNameValuePair( "metric_id", metricID ) );
		postParameters.add( 3, new BasicNameValuePair( "http_method", "POST" ) );
		result = restWsCall( ampUrl, postParameters, "POST" );
		logInfo( "creating mapping result : " + result );

		//now create mapping rule for PUT under metric "hit"
		postParameters.remove( 3 );
		postParameters.add( 3, new BasicNameValuePair( "http_method", "PUT" ) );
		result = restWsCall( ampUrl, postParameters, "POST" );
		logInfo( "creating mapping result : " + result );

		//now create mapping rule for PATCH under metric "hit"
		postParameters.remove( 3 );
		postParameters.add( 3, new BasicNameValuePair( "http_method", "PATCH" ) );
		result = restWsCall( ampUrl, postParameters, "POST" );
		logInfo( "creating mapping result : " + result );

		//now create mapping rule for DELETE under metric "hit"
		postParameters.remove( 3 );
		postParameters.add( 3, new BasicNameValuePair( "http_method", "DELETE" ) );
		result = restWsCall( ampUrl, postParameters, "POST" );
		logInfo( "creating mapping result : " + result );

	}

	/*
	private boolean searchServiceInstance(String inputServiceSystemName) throws IOException, URISyntaxException, JSONException {
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
	private String searchServiceInstance(String inputServiceSystemName, int account_id) throws IOException, URISyntaxException, JSONException
	{
		ArrayList<NameValuePair> postParameters;
		postParameters = new ArrayList();

		String ampUrl = "/admin/api/services.xml";
		String result = restWsCall( ampUrl, postParameters, "GET" );
		logInfo( "services are listed : " + result );

		int i = result.indexOf( "<system_name>" + inputServiceSystemName + "</system_name>" );

		if( i > -1 )
		{
			String serviceId = result.substring( result.lastIndexOf( "<service><id>", i ) + "<service><id>".length(), result.lastIndexOf( "</id>", i ) );
			logInfo( "---------------------found same system_name service, id : " + serviceId );
			String user_key = searchUserKeyBasedOnServiceId( serviceId, account_id );
			String endpoint = searchEndPointBasedOnServiceId( serviceId );

			String url = endpoint + "/?user_key=" + user_key;

			return url;

		}
		else
		{
			logInfo( "---------------------didn't found same system_name service : " + inputServiceSystemName );
			return "";
		}

	}

	private String searchUserKeyBasedOnServiceId(String serviceId, int accountId) throws IOException, URISyntaxException, JSONException
	{
		ArrayList<NameValuePair> postParameters;
		postParameters = new ArrayList();

		//String ampUrl = "/admin/api/accounts/" + accountId + "/applications.xml ";
		String ampUrl = "/admin/api/applications.xml";
		String result = restWsCall( ampUrl, postParameters, "GET" );
		logInfo( "application is listed : " + result );

		int i = result.indexOf( "<service_id>" + serviceId + "</service_id>" );
		String user_key = "";
		if( i > -1 )
		{
			user_key = result.substring( result.indexOf( "<user_key>", i ) + "<user_key>".length(), result.indexOf( "</user_key>", i ) );
			logInfo( "---------------------found user_key for this service id : " + user_key );

		}
		else
		{
			logInfo( "---------------------didn't found same service id in this application: " + serviceId );
		}
		return user_key;

	}

	private String searchEndPointBasedOnServiceId(String serviceId) throws IOException, URISyntaxException, JSONException
	{
		ArrayList<NameValuePair> postParameters;
		postParameters = new ArrayList();

		String ampUrl = "/admin/api/services/" + serviceId + "/proxy.xml";
		String result = restWsCall( ampUrl, postParameters, "GET" );
		logInfo( "proxy is read: " + result );

		String endpoint = result.substring( result.indexOf( "<endpoint>" ) + "<endpoint>".length(), result.indexOf( "</endpoint>" ) );
		logInfo( "---------------------found endpoint for this service id : " + endpoint );

		return endpoint;

	}
}