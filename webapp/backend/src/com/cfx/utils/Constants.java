package com.cfx.utils;

public interface Constants {
    public static final String PROPERTYNAME_OBJECTSTORE_PROVIDER = "io.macaw.objectstorage.provider";
    public static final String PROPERTYNAME_OBJECTSTORE_HOST = "io.macaw.objectstorage.host";
    public static final String PROPERTYNAME_OBJECTSTORE_PORT = "io.macaw.objectstorage.port";
    public static final String PROPERTYNAME_OBJECTSTORE_KEY_ACCESS = "io.macaw.objectstorage.key.access";
    public static final String PROPERTYNAME_OBJECTSTORE_KEY_SECRET = "io.macaw.objectstorage.key.secret";    

    String SERVICE_ERROR = "serviceError";
    String SERVICE_RESULT = "serviceResult";
    String FORM_DEF = "formDef";
    String FORM_DATA = "formData";
    String NONE = "none";
    String PARAMETERS = "parameters";
    String PARAMS = "params";
    String FORM_ID = "formId";
    String CONTEXT = "context";
    String SUBMIT_TYPE = "submitType";
    String VALIDATING_FIELD_ID = "validatingFieldId";
    String SUBMIT_RESULT = "submitResult";
    String STATUS = "status";
    String STATUS_MESSAGE = "statusMessage";
    String NEXT_ACTION = "nextAction";
    String OTHER_ACTION = "otherAction";
    String OTHER_ACTION_PARAMS = "otherActionParam";
    String FIELD_ERRORS = "fieldErrors";

    String REPORTS = "Reports";
    String FORMS = "Forms";
    String WIZARDS = "Wizards";
    String CHARTS = "Charts";
    String ACTIONS = "Actions";

    String TENANT_ID = "tenantid";

    String DATA = "data";

    String API_GATEWAY_HOST = "api.gateway.host";
    String API_GATEWAY_PORT = "api.gateway.port";
    public static String API_GATEWAY_PROTOCOL = "api.gateway.protocol";
    public static String API_GATEWAY_APPNAME = "api.gateway.appname";
    public static String API_GATEWAY_TOKEN_RENEWAL_INTERVAL = "api.gateway.token.renewal.interval";

    String API_GATEWAY_SESSION_ID = "apiGatewaySessionId";
    String USER = "user";
    String CREDENTIAL = "credential";

    String PROJECT_ACCESS_ROLES = "project-access-roles";

    String FORM_SUBMIT_ERROR_MSG = "Error occured on form submit, please contact administrator";
    String TECHNICAL_DIFFICULTIES = "We are currently experiencing technical difficulties. Please try after sometime";

    String[] ENV_PROPERTY_KEYS = new String[] { "MACAW_PLATFORM_VERSION", "MACAW_PLATFORM_RELEASE" };

    String HTTP = "http";
    String HTTPS = "https";
    // the timeout in milliseconds until a connection is established
    String CONNECTION_TIMEOUT = "httpclient.conn.timeout.ms";
    int DEFAULT_CONNECTION_TIMEOUT = 30 * 1000;

    // the timeout in milliseconds used when requesting a connection from the connection manager.
    String CONNECTION_REQUEST_TIMEOUT = "httpclient.conn.request.timeout.ms";
    int DEFAULT_CONNECTION_REQUEST_TIMEOUT = 30 * 1000;

    // the socket timeout (SO_TIMEOUT) in milliseconds, which is the timeout for waiting for data or,
    // put differently, this is the maximum period of inactivity between two consecutive data packets.
    String SOCKET_TIMEOUT = "httpclient.socket.timeout.ms";
    int DEFAULT_SOCKET_TIMEOUT = 5 * 60 * 1000;

    // the maximum number of total open connections.
    int DEFAULT_MAX_CONNECTIONS = 10;

    // the maximum number of concurrent connections per route.
    int DEFAULT_MAX_CONCURRENT_CONNECTIONS_PER_ROUTE = 5;

    // default connection keep alive in ms
    int DEFAULT_CONNECTION_KEEP_ALIVE = 60 * 1000;

    // period of inactivity in milliseconds after which persistent connections must
    // be re-validated prior to being leased} to the consumer. This check helps detect connections
    // that have become stale (half-closed) while kept inactive in the pool.
    int DEFAULT_MAX_VALIDATE_AFTER_INACTIVITY_INTERVAL = 30 * 1000;

    String HTTP_PROXY_HOST = "http.proxyHost";
    String HTTP_PROXY_PORT = "http.proxyPort";
    String HTTP_PROXY_PASSWORD = "http.proxyPassword";
    String HTTP_PROXY_USER = "http.proxyUser";

    String HTTPS_PROXY_HOST = "https.proxyHost";
    String HTTPS_PROXY_PORT = "https.proxyPort";
    String HTTPS_PROXY_PASSWORD = "https.proxyPassword";
    String HTTPS_PROXY_USER = "https.proxyUser";
    int HTTP_PROXY_DEFAULT_PORT = 80;
    int HTTPS_PROXY_DEFAULT_PORT = 443;
    
    public static boolean isSupported(String protocol) {
        if (!protocol.equalsIgnoreCase(HTTP) && !protocol.equalsIgnoreCase(HTTPS)) {
            return false;
        }
        return true;
    }
}