/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.syseng.openshift.service.broker.service.broker.util;

import com.redhat.syseng.openshift.service.broker.service.ServiceSecurer;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
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
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author czhu
 */
public class BrokerUtil {

    private static Logger logger = Logger.getLogger(BrokerUtil.class.getName());

    private static void logInfo(String message) {
        logger.log(Level.INFO, message);
    }

    private static HttpClient createHttpClient_AcceptsUntrustedCerts() {
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
            Logger.getLogger(ServiceSecurer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException ex) {
            Logger.getLogger(ServiceSecurer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyManagementException ex) {
            Logger.getLogger(ServiceSecurer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static URIBuilder getUriBuilder(Object... path) {
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

    public static String restWsCall(String inputURL, ArrayList<NameValuePair> postParameters, String httpMethod) {
        HttpClient client = createHttpClient_AcceptsUntrustedCerts();
        URIBuilder uriBuilder = getUriBuilder(inputURL);

        HttpRequestBase request;
        String responseString = "";
        try {
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
            responseString = EntityUtils.toString(response.getEntity());

        } catch (IOException ex) {
            Logger.getLogger(BrokerUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(BrokerUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return responseString;

    }

    public static String searchEndPointBasedOnServiceId(String serviceId) {
        ArrayList<NameValuePair> postParameters;
        postParameters = new ArrayList();

        String ampUrl = "/admin/api/services/" + serviceId + "/proxy.xml";
        String result = restWsCall(ampUrl, postParameters, "GET");
        logInfo("proxy is read: " + result);

        String endpoint = result.substring(result.indexOf("<endpoint>") + "<endpoint>".length(), result.indexOf("</endpoint>"));
        logInfo("---------------------found endpoint for this service id : " + endpoint);

        return endpoint;

    }

    public static String searchAnyUserKeyBasedOnServiceId(String serviceId, int accountId) {
        ArrayList<NameValuePair> postParameters;
        postParameters = new ArrayList();

        //String ampUrl = "/admin/api/accounts/" + accountId + "/applications.xml";
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

    public static String searchExistingApplicationBaseOnName(String applicationName, int accountId) {
        String ampUrl = "/admin/api/accounts/" + accountId + "/applications.xml";
        String result = restWsCall(ampUrl, null, "GET");
        logInfo("application is listed : " + result);
        int i =  result.indexOf("<name>" + applicationName + "</name>");
        String user_key = "";
        
        if (i > -1){
            user_key = result.substring(result.lastIndexOf("<user_key>", i) + "<user_key>".length(), result.lastIndexOf("</user_key>", i));
            logInfo("---------------------found existing application, user_key is : " + user_key);
            
        }
         
         return user_key;
    }
    
        public static String searchUserKeyBasedOnServiceAndPlanId(String serviceId, String planId, int accountId) {
        ArrayList<NameValuePair> postParameters;
        postParameters = new ArrayList();

        String ampUrl = "/admin/api/accounts/" + accountId + "/applications.xml";
        String result = restWsCall(ampUrl, postParameters, "GET");
        logInfo("application is listed : " + result);

        String userKey = "";
        int i = result.indexOf("<service_id>" + serviceId +"</service_id>");
        if (i > -1){
            int j = result.indexOf("<id>" + planId + "</id>", i);
            if (j > -1){
                userKey = result.substring(result.indexOf("<user_key>", i) + "<user_key>".length(), result.indexOf("</user_key>", i));
            }
        }
        if (!userKey.equals("")) {
            logInfo("---------------------found user_key for this service id : " + userKey);

        } else {
            logInfo("---------------------didn't found user_key for this serviceId: " + serviceId + " and planId: " + planId);
        }
        return userKey;

    }

    
}
