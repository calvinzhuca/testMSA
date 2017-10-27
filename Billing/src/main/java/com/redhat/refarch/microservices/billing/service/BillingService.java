package com.redhat.refarch.microservices.billing.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import javax.ws.rs.POST;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.ws.rs.core.Response;

@Path("/")
public class BillingService {

    private ManagedExecutorService executorService;

    private Logger logger = Logger.getLogger(getClass().getName());

    private static final Random random = new Random();

    @POST
    @Path("/process2")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void process2(final Transaction transaction, final @Suspended AsyncResponse asyncResponse) {

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
    @Path("/name")
    @Consumes({"*/*"})
    @Produces({MediaType.APPLICATION_JSON})
    public Response getName() {
        //  String catalog = "test";
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String name = "{\"name\":\"BillingService\", \"time\":\"" + dateFormat.format(date) + "\"}";
        logInfo("getName: " + name);

        return Response.ok(name, MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Path("/process")
    @Consumes({"*/*"})
    @Produces({MediaType.APPLICATION_JSON})
    public Response process(final Transaction transaction) {

        long sleep = new Long(System.getenv("TRANSCATION_IN_MILLISECONDS"));

        logInfo("Will simulate credit card processing for " + sleep + " milliseconds");
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException ex) {
            Logger.getLogger(BillingService.class.getName()).log(Level.SEVERE, null, ex);
        }
        Result result = processSync(transaction);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonStr = gson.toJson(result);
        logInfo("process result: " + jsonStr);

        return Response.ok(jsonStr, MediaType.APPLICATION_JSON).build();

    }

}
