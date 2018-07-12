package com.cfx.utils;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomHttpClient.class);
    private String host;
    private int portNumber;
    private String protocol;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;

    private CustomHttpClient(String host, int portNumber, String protocol, RequestConfig requestConfig) {
        this.host = host;
        this.portNumber = portNumber;
        this.protocol = protocol;
        this.requestConfig = requestConfig;
    }

    public static CustomHttpClient prepareCustomHttpClient() throws CustomHttpClientException {
        return prepareCustomHttpClient(null);
    }

    public static CustomHttpClient prepareCustomHttpClient(final Properties configProperties)
            throws CustomHttpClientException {

    	//TODO: cause this is a POC we need to hard code the location of the api gateway
    	//this can be fixed to read from properties file later
    	//HARD CODE
        //String host = "10.95.122.144";
        String host = "ec2-35-168-116-172.compute-1.amazonaws.com";
        String port = "8080";
        String protocol = "http";

        return prepareCustomHttpClient(host, port, protocol, configProperties);
    }

    public static CustomHttpClient prepareCustomHttpClient(final String host, final String port, String protocol,
            final Properties configProperties) throws CustomHttpClientException {

        final int portNumber;
        try {
            portNumber = Integer.parseInt(port.trim());
        } catch (NumberFormatException nfe) {
            throw new IllegalStateException("Service registry port: " + port + " is invalid");
        }
        protocol = protocol.trim().toLowerCase(Locale.ENGLISH);
        if (!Constants.isSupported(protocol)) {
            throw new CustomHttpClientException("protocol " + protocol + " is not supported.");
        }

        // request connection related configurations...
        final RequestConfig requestConfig = makeRequestConfig(configProperties);
        return new CustomHttpClient(host, portNumber, protocol, requestConfig);
    }

    public void buildHttpClient() {
        final HttpClientBuilder builder = HttpClients.custom();
        final HttpClientConnectionManager connectionManager = makeConnectionManager();
        builder.setConnectionManager(connectionManager);
        builder.setDefaultRequestConfig(requestConfig);
        builder.setKeepAliveStrategy(makeKeepAliveStrategy());
        // User proxy settings...
        builder.setDefaultCredentialsProvider(prepareProxyCredentials(new BasicCredentialsProvider()));
        final SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());
        builder.setRoutePlanner(routePlanner);

        this.client = builder.build();
    }

    public CloseableHttpClient getClient() {
        return client;
    }

    /*
     * Use the server’s Keep-Alive policy stated in the header. If that info is not present in the response header,
     * then we keep alive connections for DEFAULT_CONNECTION_KEEP_ALIVE seconds
     */
    private ConnectionKeepAliveStrategy makeKeepAliveStrategy() {
        ConnectionKeepAliveStrategy keepAliveStrategy = new ConnectionKeepAliveStrategy() {

            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                HeaderElementIterator it = new BasicHeaderElementIterator(
                        response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase("timeout")) {
                        return Long.parseLong(value) * 1000;
                    }
                }
                return Constants.DEFAULT_CONNECTION_KEEP_ALIVE;
            }
        };
        return keepAliveStrategy;
    }

    private HttpClientConnectionManager makeConnectionManager() {
        PoolingHttpClientConnectionManager cm;
        cm = new PoolingHttpClientConnectionManager();

        cm.setMaxTotal(Constants.DEFAULT_MAX_CONNECTIONS);
        cm.setDefaultMaxPerRoute(Constants.DEFAULT_MAX_CONCURRENT_CONNECTIONS_PER_ROUTE);
        cm.setValidateAfterInactivity(Constants.DEFAULT_MAX_VALIDATE_AFTER_INACTIVITY_INTERVAL);
        return cm;
    }

    private static RequestConfig makeRequestConfig(final Properties configProperties) {
        final Builder builder = RequestConfig.custom();

        String connectionTimeout = null;
        String connectionRequestTimeout = null;
        String socketTimeout = null;
        if (configProperties != null && !configProperties.isEmpty()) {
            // connection timeout...
            connectionTimeout = configProperties.getProperty(Constants.CONNECTION_TIMEOUT);
            // connection request timeout...
            connectionRequestTimeout = configProperties.getProperty(Constants.CONNECTION_REQUEST_TIMEOUT);
            // connection request timeout...
            socketTimeout = configProperties.getProperty(Constants.SOCKET_TIMEOUT);
        }
        if (connectionTimeout == null) {
            builder.setConnectTimeout(Constants.DEFAULT_CONNECTION_TIMEOUT);
        } else {
            try {
                builder.setConnectTimeout(Integer.parseInt(connectionTimeout.trim()));
            } catch (NumberFormatException nfe) {
                throw new IllegalStateException(
                        "API Gateway connection timeout value: " + connectionTimeout + " is invalid");
            }
        }
        if (connectionRequestTimeout == null) {
            builder.setConnectionRequestTimeout(Constants.DEFAULT_CONNECTION_REQUEST_TIMEOUT);
        } else {
            try {
                builder.setConnectionRequestTimeout(Integer.parseInt(connectionRequestTimeout.trim()));
            } catch (NumberFormatException nfe) {
                throw new IllegalStateException(
                        "API Gateway connection request timeout value: " + connectionRequestTimeout + " is invalid ");
            }
        }

        if (socketTimeout == null) {
            builder.setSocketTimeout(Constants.DEFAULT_SOCKET_TIMEOUT);
        } else {
            try {
                builder.setSocketTimeout(Integer.parseInt(socketTimeout.trim()));
            } catch (NumberFormatException nfe) {
                throw new IllegalStateException("API Gateway socket timeout value: " + socketTimeout + " is invalid ");
            }
        }
        return builder.build();
    }

    /**
     * Executes a HttpGet request.
     * 
     * @param headers
     *            - List of headers in the request .If no headers then pass null or empty list.
     * @param endpoint
     * @return Response as string.
     * @throws CustomHttpClientException
     */
    public String executeGet(final String endpoint, final List<Header> headers) throws CustomHttpClientException {
        CloseableHttpResponse response = null;
        try {
            final HttpGet httpGet = new HttpGet(endpoint);
            processHeaders(headers, httpGet);
            response = this.client.execute(new HttpHost(host, portNumber, protocol), httpGet);
            logResponse(endpoint, response);
            return getResponseAsString(response);
        } catch (final ParseException | IOException e) {
            throw new CustomHttpClientException(
                    "Problem processing  response " + ((response != null && response.getStatusLine() != null)
                            ? response.getStatusLine().getReasonPhrase() : ""),
                    e);
        } finally {
            destroy();
        }
    }

    public String executePost(final String endpoint, final List<Header> headers, final List<NameValuePair> params,
            final String inputJson) throws CustomHttpClientException {
        return execute(endpoint, headers, params, inputJson, "post");
    }

    public String executeDelete(final String endpoint, final List<Header> headers, final List<NameValuePair> params,
            final String inputJson) throws CustomHttpClientException {
        return execute(endpoint, headers, params, inputJson, "delete");
    }

    public String executePut(final String endpoint, final List<Header> headers, final List<NameValuePair> params,
            final String inputJson) throws CustomHttpClientException {
        return execute(endpoint, headers, params, inputJson, "put");
    }

    private String execute(final String endpoint, final List<Header> headers, final List<NameValuePair> params,
            final String inputJson, final String methodType) throws CustomHttpClientException {

        CloseableHttpResponse response = null;
        try {
            final StringEntity inputEntity = new StringEntity(
                    (inputJson != null && !inputJson.trim().isEmpty() ? inputJson : ""), ContentType.APPLICATION_JSON);
            final HttpHost httpHost = new HttpHost(host, portNumber, protocol);

            HttpUriRequest httpRequest = null;
            final URIBuilder uriBuilder = new URIBuilder(endpoint);
            processParams(uriBuilder, params);

            if ("post".equals(methodType)) {
                HttpPost p = new HttpPost(uriBuilder.build());
                p.setEntity(inputEntity);
                httpRequest = p;
            } else if ("put".equals(methodType)) {
                HttpPut p = new HttpPut(uriBuilder.build());
                p.setEntity(inputEntity);
                httpRequest = p;
            } else if ("delete".equals(methodType)) {
                HttpDelete p = new HttpDelete(uriBuilder.build());
                p.setEntity(inputEntity);
                httpRequest = p;
            }
            processHeaders(headers, httpRequest);

            response = this.client.execute(httpHost, httpRequest);

            logResponse(httpRequest.getURI().getPath(), response);
            return getResponseAsString(response);
        } catch (IOException | URISyntaxException e) {
            throw new CustomHttpClientException("Unable to read content", e);
        } finally {
            destroy();
        }
    }

    public void destroy() {
        try {
            if (this.client != null) {
                this.client.close();
            }
        } catch (IOException e) {
            // do nothing...
        }
    }

    public String getResponseAsString(final CloseableHttpResponse response)
            throws IOException, CustomHttpClientException {
        String responseStr = null;
        try {
            if (response != null) {
                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    responseStr = EntityUtils.toString(entity, "UTF-8");
                    EntityUtils.consume(entity);
                }
                final StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() >= 300) {
                    throw new CustomHttpClientException(responseStr);
                }
            }
        } finally {
            closeResponse(response);
        }
        return responseStr;
    }

    public void closeResponse(final CloseableHttpResponse response) {
        try {
            if (response != null) {
                response.close();
            }
        } catch (IOException e) {
            LOGGER.error("Problem closing response ", e);
        }
    }

    private void processHeaders(final List<Header> headers, final HttpUriRequest httpUriRequest) {
        if (headers != null && !headers.isEmpty()) {
            for (Header header : headers) {
                httpUriRequest.addHeader(header);
            }
        }
    }

    private void processParams(final URIBuilder uriBuilder, final List<NameValuePair> params) {
        if (params != null && !params.isEmpty()) {
            uriBuilder.addParameters(params);
        }
    }

    protected CredentialsProvider prepareProxyCredentials(CredentialsProvider credsProvider) {
        if (credsProvider == null) {
            credsProvider = new BasicCredentialsProvider();
        }
        prepareHttpProxyCredentials(credsProvider);
        return credsProvider;
    }

    protected CredentialsProvider prepareHttpProxyCredentials(final CredentialsProvider credsProvider) {
        int port = Constants.HTTP_PROXY_DEFAULT_PORT;
        final String proxyUser = System.getProperty(Constants.HTTP_PROXY_USER);
        final String proxyPassword = System.getProperty(Constants.HTTP_PROXY_PASSWORD);
        if (proxyUser != null && !proxyUser.trim().isEmpty() && proxyPassword != null
                && !proxyPassword.trim().isEmpty()) {
            final String portStr = System.getProperty(Constants.HTTP_PROXY_PORT);
            if (portStr != null && !portStr.trim().isEmpty()) {
                port = Integer.valueOf(portStr);
            }
            credsProvider.setCredentials(new AuthScope(System.getProperty(Constants.HTTP_PROXY_HOST), port),
                    new UsernamePasswordCredentials(proxyUser, proxyPassword));
        }
        return credsProvider;
    }

    private void logResponse(final String endpoint, final HttpResponse response) {
        if (response != null && response.getStatusLine() != null) {
            LOGGER.trace("endpoint " + endpoint);
            LOGGER.trace("StatusCode " + response.getStatusLine().getStatusCode());
            LOGGER.trace("ReasonPhrase " + response.getStatusLine().getReasonPhrase());
        }
    }

    /**
     * This class is created since HttpDelete class of HttpClient does not allow to set entity as a part of HttpDelete
     * request.
     * 
     * @author harsh
     *
     */
    private static class HttpDelete extends HttpEntityEnclosingRequestBase {

        public HttpDelete(final URI uri) {
            super();
            setURI(uri);
        }

        @Override
        public String getMethod() {
            return "DELETE";
        }
    }

}
