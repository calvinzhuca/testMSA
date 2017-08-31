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
public class BillingService {

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
	@Consumes({"*/*"})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getCatalog() {
	  //  String catalog = "test";
	  /*
	     String catalog = "{\"services\":[{\"name\":\"fake-service\",\"id\":\"acb56d7c-AAAA-AAAA-AAAA-feb140a59a66\",\"description\":\"fake service\",\"tags\":[\"no-sql\",\"relational\"],\"requires\":[\"route_forwarding\"],\"bindable\":true,\"metadata\":{\"provider\":{\"name\":\"The name\"},\"listing\":{\"imageUrl\":\"http://example.com/cat.gif\",\"blurb\":\"Add a blurb here\",\"longDescription\":\"A long time ago, in a galaxy far far away...\"},\"displayName\":\"The Fake Broker\"},\"dashboard_client\":{\"id\":\"398e2f8e-AAAA-AAAA-AAAA-19a71ecbcf64\",\"secret\":\"277cabb0-AAAA-AAAA-AAAA-7822c0a90e5d\",\"redirect_uri\":\"http://localhost:1234\"},\"plan_updateable\":true,\"plans\":[{\"name\":\"fake-plan-1\",\"id\":\"d3031751-AAAA-AAAA-AAAA-a42377d3320e\",\"description\":\"Shared fake Server, 5tb persistent disk, 40 max concurrent connections\",\"free\":false,\"metadata\":{\"max_storage_tb\":5,\"costs\":[{\"amount\":{\"usd\":99},\"unit\":\"MONTHLY\"},{\"amount\":{\"usd\":0.99},\"unit\":\"1GB of messages over 20GB\"}],\"bullets\":[\"Shared fake server\",\"5 TB storage\",\"40 concurrent connections\"]}},{\"name\":\"fake-plan-2\",\"id\":\"0f4008b5-AAAA-AAAA-AAAA-dace631cd648\",\"description\":\"Shared fake Server, 5tb persistent disk, 40 max concurrent connections. 100 async\",\"free\":false,\"metadata\":{\"max_storage_tb\":5,\"costs\":[{\"amount\":{\"usd\":199},\"unit\":\"MONTHLY\"},{\"amount\":{\"usd\":0.99},\"unit\":\"1GB of messages over 20GB\"}],\"bullets\":[\"40 concurrent connections\"]}}]}]}";
	     */
	     String catalog = "{\r\n  \"services\": [\r\n   {\r\n    \"name\": \"service-name\",\r\n    \"id\": \"serviceUUID\",\r\n    \"description\": \"service description\",\r\n    \"tags\": [\r\n     \"tag1\",\r\n     \"tag2\"\r\n    ],\r\n    \"bindable\": true,\r\n    \"metadata\": {\r\n     \"metadata_key1\": \"metadata_value1\"\r\n    },\r\n    \"plans\": [\r\n     {\r\n      \"id\": \"gold-plan-id\",\r\n      \"name\": \"gold-plan\",\r\n      \"description\": \"gold plan description\",\r\n      \"free\": true,\r\n      \"bindable\": true\r\n     }\r\n    ]\r\n   }\r\n  ]\r\n }";
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
	public String provisioning(@PathParam("instance_id") String instance_id) {
	  //  String result = "test";
	     String result = "{\"resut\":\"provisioning success\"}";
		logInfo( "provisioning instance_id: " + instance_id );
		return result;
	}
	
	@DELETE
	@Path("/service_instances/{instance_id}")
	@Consumes({"*/*"})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public String deProvisioning(@PathParam("instance_id") String instance_id) {
	  //  String result = "test";
	     String result = "{\"resut\":\"deProvisioning success\"}";
		logInfo( "deProvisioning instance_id: " + instance_id );
		return result;
	}

	@PUT
	@Path("/service_instances/{instance_id}/service_bindings/{binding_id}")
	@Consumes({"*/*"})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public String binding(@PathParam("instance_id") String instance_id, @PathParam("binding_id") String binding_id) {
	  //  String result = "test";
	     String result = "{\"resut\":\"binding success\"}";
		logInfo( "binding instance_id: " + instance_id );
		logInfo( "binding binding_id: " + binding_id );
		return result;
	}

	@DELETE
	@Path("/service_instances/{instance_id}/service_bindings/{binding_id}")
	@Consumes({"*/*"})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public String unBinding(@PathParam("instance_id") String instance_id, @PathParam("binding_id") String binding_id) {
	  //  String result = "test";
	     String result = "{\"resut\":\"unBinding success\"}";
		logInfo( "unBinding instance_id: " + instance_id );
		logInfo( "unBinding binding_id: " + binding_id );
		return result;
	}

}
