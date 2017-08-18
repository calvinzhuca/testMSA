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
	public String getCatalog() {
	  //  String catalog = "test";
	     String catalog = "{\"services\":[{\"name\":\"3scales-amp\",\"id\":\"3101b971-1044-4816-a7ac-9ded2e028079\",\"description\":\"3scales-amp service for secure RESTful services\",\"tags\":[\"3scales\"],\"metadata\":{\"provider\":{\"name\":null},\"listing\":{\"imageUrl\":null,\"blurb\":\"3scales-amp service for secure RESTful services\"}},\"plans\":[{\"name\":\"testPlan\",\"id\":\"2451fa22-df16-4c10-ba6e-1f682d3dcdc9\",\"description\":\"testPlan desc\",\"metadata\":{\"cost\":0.0,\"bullets\":[{\"content\":\"test content 1\"},{\"content\":\"test content 2\"},{\"content\":\"test content 3\"}]}}]}]}";
		logInfo( "getCatalog: " + catalog );
		return catalog;
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
