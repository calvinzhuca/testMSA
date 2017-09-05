package com.redhat.refarch.microservices.billing.service;

import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.redhat.refarch.microservices.billing.model.Result;
import com.redhat.refarch.microservices.billing.model.Result.Status;
import com.redhat.refarch.microservices.billing.model.Transaction;

@Path("/")
public class ThreeScalesBroker {

	private ManagedExecutorService executorService;

	private Logger logger = Logger.getLogger( getClass().getName() );

	private static final Random random = new Random();

	@POST
	@Path("/process")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public void process(final Transaction transaction, final @Suspended AsyncResponse asyncResponse) {

		Runnable runnable = () -> {
			try {
				final long sleep = 5000;
				logInfo( "Will simulate credit card processing for " + sleep + " milliseconds" );
				Thread.sleep( sleep );
				Result result = processSync( transaction );
				asyncResponse.resume( result );
			} catch (Exception e) {
				asyncResponse.resume( e );
			}
		};
		getExecutorService().execute( runnable );
	}
	
	private Executor getExecutorService() {
		if( executorService == null ) {
			try {
				executorService = InitialContext.doLookup( "java:comp/DefaultManagedExecutorService" );
			} catch (NamingException e) {
				throw new WebApplicationException( e );
			}
		}
		return executorService;
	}

	private Result processSync(Transaction transaction) {
		Result result = new Result();
		result.setName( transaction.getCustomerName() );
		result.setOrderNumber( transaction.getOrderNumber() );
		logInfo( "Asked to process credit card transaction: " + transaction );
		Calendar now = Calendar.getInstance();
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set( transaction.getExpYear(), transaction.getExpMonth(), 1 );
		if( calendar.after( now ) ) {
			result.setTransactionNumber( (long)(random.nextInt( 9000000 ) + 1000000) );
			result.setTransactionDate( now.getTime() );
			result.setStatus( Status.SUCCESS );
		} else {
			result.setStatus( Status.FAILURE );
		}
		return result;
	}


	@POST
	@Path("/refund/{transactionNumber}")
	@Consumes({"*/*"})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public void refund(@PathParam("transactionNumber") long transactionNumber) {
		logInfo( "Asked to refund credit card transaction: " + transactionNumber );
	}

	private void logInfo(String message) {
		logger.log( Level.INFO, message );
	}
	
	
	@GET
	@Path("/catalog")
	//@Consumes({"*/*"})
	@Consumes("application/x-www-form-urlencoded")
	@Produces({MediaType.APPLICATION_JSON})
	public Response getCatalog() {
	  //  String catalog = "test";

	     String catalog = "{\"services\":[{\"name\":\"three-scales-service\",\"id\":\"serviceUUID\",\"description\":\"secure service 3scales broker implementation\",\"bindable\":true,\"metadata\":{\"displayName\":\"secure-service-3scales-broker\",\"documentationUrl\":\"https://access.qa.redhat.com/documentation/en-us/reference_architectures/2017/html-single/api_management_with_red_hat_3scale_api_management_platform\",\"longDescription\":\"A broker that secures input URL through 3scales-AMP\",\"parameters\":[{\"input_url\":{\"title\":\"input url\",\"type\":\"string\",\"default\":\"https://echo-api.3scale.net:443\"}},{\"username\":{\"title\":\"User Name\",\"type\":\"string\",\"default\":\"admin\"}},{\"password\":{\"title\":\"password\",\"type\":\"string\"}}]},\"plans\":[{\"id\":\"gold-plan-id\",\"name\":\"test-plan\",\"description\":\"3scale plan description ...\",\"free\":true,\"schemas\":{\"service_instance\":{\"create\":{\"parameters\":{\"$schema\":\"http://json-schema.org/draft-04/schema\",\"additionalProperties\":false,\"properties\":{\"amp_admin_user\":{\"default\":\"admin\",\"title\":\"3scale AMP User\",\"type\":\"string\"},\"amp_admin_pass\":{\"default\":\"password1\",\"title\":\"3scale AMP Password\",\"type\":\"string\"},\"amp_url\":{\"title\":\"input url for secure\",\"type\":\"string\",\"default\":\"https://echo-api.3scale.net:443\"}},\"required\":[\"amp_admin_user\",\"amp_admin_pass\",\"amp_url\"],\"type\":\"object\"}},\"update\":{}},\"service_binding\":{\"create\":{\"parameters\":{\"$schema\":\"http://json-schema.org/draft-04/schema\",\"additionalProperties\":false,\"properties\":{\"properties\":{\"amp_admin_user\":{\"default\":\"admin\",\"title\":\"3scale AMP User\",\"type\":\"string\"},\"amp_admin_pass\":{\"default\":\"password1\",\"title\":\"3scale AMP User Password\",\"type\":\"string\"},\"amp_url\":{\"default\":\"amp\",\"title\":\"input url for secure\",\"type\":\"string\"}}},\"type\":\"object\"}}}}}]}]}";
		logInfo( "getCatalog: " + catalog );
	
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

		
		return Response.status(Response.Status.NOT_FOUND).entity("Entity not found example" ).build();
	}

	@PUT
	@Path("/service_instances/{instance_id}")
	@Consumes({"*/*"})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public String provisioning(@PathParam("instance_id") String instance_id, @FormParam("amp_admin_pass") String amp_admin_pass, @QueryParam("amp_admin_user") String amp_admin_user) {
		logInfo( "!!!!!!!!!!provisioning Transaction1 /service_instances/{instance_id} : " + instance_id );
		logInfo( "!!!!!!!!!!amp_admin_user: " + amp_admin_user );
		logInfo( "!!!!!!!!!!amp_admin_pass: " + amp_admin_pass );
	     String result = "{\"kind\":\"ServiceInstanceList\",\"apiVersion\":\"sdkbroker.broker.k8s.io/v1alpha1\",\"metadata\":{\"selfLink\":\"/apis/sdkbroker.broker.k8s.io/v1alpha1/namespaces/brokersdk/serviceinstances\",\"resourceVersion\":\"473\"},\"items\":[]}";
		logInfo( "!!!!!!!!!!provisioning /service_instances/{instance_id} : result" + result );
		return result;
	}
	
	@PUT
	@Path("/service_instances2/{instance_id}")
	@Consumes({"*/*"})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public String provisioning2(@QueryParam("instance_id") String instance_id, final Transaction transaction) {
		logInfo( "!!!!!!!!!!provisioning2 /service_instances2 instance_id: " + instance_id );
	  //  String result = "test";
	     String result = "{\"kind\":\"ServiceInstanceList\",\"apiVersion\":\"sdkbroker.broker.k8s.io/v1alpha1\",\"metadata\":{\"selfLink\":\"/apis/sdkbroker.broker.k8s.io/v1alpha1/namespaces/brokersdk/serviceinstances\",\"resourceVersion\":\"473\"},\"items\":[]}";
		return result;
	}
		
	@DELETE
	@Path("/service_instances/{instance_id}")
	@Consumes({"*/*"})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public String deProvisioning(@PathParam("instance_id") String instance_id) {
		logInfo( "!!!!!!!!!!!!!deProvisioning /service_instances/{instance_id}: " + instance_id );
	     String result = "{}";
		logInfo( "deProvisioning /service_instances/{instance_id} result: " + instance_id );
		return result;
	}

	@DELETE
	@Path("/service_instances")
	@Consumes({"*/*"})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public String deProvisioning2(@QueryParam("instance_id") String instance_id) {
		logInfo( "deProvisioning2 /service_instances: " + instance_id );
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
		logInfo( "!!!!!!!!!!!!!!!binding instance_id: " + instance_id );
		logInfo( "binding binding_id: " + binding_id );
		return result;
	}

	@DELETE
	@Path("/service_instances/{instance_id}/service_bindings/{binding_id}")
	@Consumes({"*/*"})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public String unBinding(@PathParam("instance_id") String instance_id, @PathParam("binding_id") String binding_id) {
	  //  String result = "test";
	     String result = "{}";
		logInfo( "!!!!!!!!!!!!!!!!unBinding instance_id: " + instance_id );
		logInfo( "unBinding binding_id: " + binding_id );
		return result;
	}

}
