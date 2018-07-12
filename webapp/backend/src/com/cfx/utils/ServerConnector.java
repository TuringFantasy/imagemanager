package com.cfx.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.research.ws.wadl.HTTPMethods;

public class ServerConnector {

    private static final ServerConnector _instance = new ServerConnector();
    private final static Logger logger = LoggerFactory.getLogger(ServerConnector.class);
    private String apiGatewayAppName;
    
    public final static String API_GATEWAY_SESSION_ID = "apiGatewaySessionId";
    public final static String USER_DETAILS = "user_details";
    public final static String API_GATEWAY_REQUEST_HEADER = "X-Auth-Token";

    public static ServerConnector getInstance() {
        return _instance;
    }

    private ServerConnector() {
        apiGatewayAppName = "api_gateway";
    }

    public String gatewayLogin(JsonObject loginDetails) throws CustomHttpClientException, IOException {
        String URL = "/" + apiGatewayAppName + "/login";
        return CommonUtils.executePost(URL, loginDetails.toString(), null, null);
    }

    public String gatewayForgotPassword(JsonObject forgotPasswordDetails)
            throws CustomHttpClientException, IOException {
        String URL = "/" + apiGatewayAppName + "/forgot_password";
        return CommonUtils.executePost(URL, forgotPasswordDetails.toString(), null, null);
    }

    public String gatewaySignUpUser(JsonObject signUpUserDetails) throws CustomHttpClientException, IOException {
        String URL = "/" + apiGatewayAppName + "/signup_user";
        return CommonUtils.executePost(URL, signUpUserDetails.toString(), null, null);
    }

    public String gatewayLogout(String token) throws CustomHttpClientException, IOException {
        String URL = "/" + apiGatewayAppName + "/logout";

        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put(API_GATEWAY_REQUEST_HEADER, token);

        return CommonUtils.executePost(URL, "{}", requestHeaders, null);
    }

    public String execute(String path, HTTPMethods methodType, String requestData, String token) throws ServiceExecutionException {
        return execute(path, methodType, requestData, true, token);
    }

    public String execute(String path, HTTPMethods methodType, String requestData, boolean processError, String token)
            throws ServiceExecutionException {

        String URL = path;
        if (!path.startsWith("/" + apiGatewayAppName + "/")) {
            URL = "/" + apiGatewayAppName + path;
        }

        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put(API_GATEWAY_REQUEST_HEADER, token);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "API Gateway request URL : [" + URL + "], Token: [" + token + "], requestData : " + requestData);
        }

        String response = null;
        try {
            switch (methodType) {
            case POST:
                response = CommonUtils.executePost(URL, requestData, requestHeaders, null);
                break;
            case GET:
                response = CommonUtils.executeGet(URL, requestHeaders);
                break;
            case PUT:
                response = CommonUtils.executePut(URL, requestData, requestHeaders, null);
                break;
            case DELETE:
                response = CommonUtils.executeDelete(URL, requestData, requestHeaders, null);
                break;
            default:
                break;
            }
            if (logger.isDebugEnabled()) {
                logger.debug(" API Gateway Response : " + response);
            }
        } catch (CustomHttpClientException | IOException e) {
            logger.error(e.getMessage(), e);
            if (processError)
                throw new ServiceExecutionException(ResponseUtils.processErrorMessage(e.getMessage()));
            else
                throw new ServiceExecutionException(e.getMessage());
        }
        return response;
    }

    public String remoateGatewayLogin(JsonObject repoDetails) throws ServiceExecutionException {

        String host = JSONUtils.getAsString(repoDetails.get("host"));
        String port = JSONUtils.getAsString(repoDetails.get("port"));
        try {
            CustomHttpClient httpClient = CustomHttpClient.prepareCustomHttpClient(host, port, "http", null);
            httpClient.buildHttpClient();

            JsonObject loginDetails = new JsonObject();
            loginDetails.add("user", repoDetails.get("user"));
            loginDetails.add("password", repoDetails.get("password"));

            String URL = "/" + apiGatewayAppName + "/login";

            final List<Header> headers = new ArrayList<>();
            headers.add(new BasicHeader("Content-Type", "application/json"));
            return httpClient.executePost(URL, headers, null, loginDetails.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ServiceExecutionException("Remote authentication failed for host : " + host);
        }
    }

    public String executeOnRemoteAPIGateway(JsonObject repoDetails, String path, HTTPMethods methodType, String data)
            throws ServiceExecutionException {
        String host = JSONUtils.getAsString(repoDetails.get("host"));
        String port = JSONUtils.getAsString(repoDetails.get("port"));
        try {
            CustomHttpClient httpClient = CustomHttpClient.prepareCustomHttpClient(host, port, "http", null);
            httpClient.buildHttpClient();

            JsonElement jsEl = repoDetails.get("userInfo");
            if (jsEl != null && jsEl.isJsonObject()) {
                JsonObject userDetails = jsEl.getAsJsonObject();
                if (userDetails.get(API_GATEWAY_SESSION_ID) != null) {
                    String apiGatewaySessionId = userDetails.get(API_GATEWAY_SESSION_ID).getAsString();

                    final List<Header> headers = new ArrayList<>();
                    headers.add(new BasicHeader("Content-Type", "application/json"));
                    headers.add(new BasicHeader(API_GATEWAY_REQUEST_HEADER, apiGatewaySessionId));

                    String URL = "/api_gateway" + path;
                    switch (methodType) {
                    case POST:
                        return httpClient.executePost(URL, headers, null, data);
                    case GET:
                        return httpClient.executeGet(URL, headers);
                    }
                }
            }
        } catch (CustomHttpClientException e) {
            logger.error(e.getMessage(), e);
            throw new ServiceExecutionException(ResponseUtils.processErrorMessage(e.getMessage()));
        }
        throw new ServiceExecutionException("Remote authentication failed for host : " + host);
    }
}
