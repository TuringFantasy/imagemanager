package com.cfx.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.minio.MinioClient;


public class ServiceConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceConfig.class);
        
    private static final ServiceConfig _instance = new ServiceConfig();
    private Properties macawProperties = null;
    
    static {
        _instance.start();
    }
            
    private ServiceConfig() {
    }
    
    public static ServiceConfig getInstance() {
        return _instance;
    }
    
    public String getUserid() {
        return getProperty("io.macaw.demo.user", "admin@macaw.io");
    }

    public String getPassword() {
    	return getProperty("io.macaw.demo.password", "abcd123$");
    }
    
    public String getMinioServer() {
        return getProperty(Constants.PROPERTYNAME_OBJECTSTORE_HOST, "");
    }
    
    public String getMinioPort() {
        return getProperty(Constants.PROPERTYNAME_OBJECTSTORE_PORT, "");
    }
    
    public String getMinioAccessKey() {
        return getProperty(Constants.PROPERTYNAME_OBJECTSTORE_KEY_ACCESS, "");
    }
    
    public String getMinioSecretKey() {
        return getProperty(Constants.PROPERTYNAME_OBJECTSTORE_KEY_SECRET, "");
    }
    
    public String getApiGatewayHost() {
        return getProperty("api.gateway.host", "");
    }
    
    public String getApiGatewayPort() {
        return getProperty("api.gateway.port", "");
    }
    
    public String getApiGatewayProtocol() {
        return getProperty("api.gateway.protocol", "");
    }
    
    public String getServiceName() {
        return getProperty("image.manager.service.name", "");
    }
    
    public String getServiceNamespace() {
        return getProperty("image.manager.service.name.space", "");
    }
    
    public String getServiceVersion() {
        return getProperty("image.manager.service.version", "");
    }

    public void start()  {
        
    }
    
    public void stop() {
    }
    
    public Properties getMacawProperties(InputStream is) {
        if (macawProperties == null || macawProperties.isEmpty()) {
                try {
                    macawProperties = loadMacawProperties(is);
                } catch (Exception e) {
                        throw new RuntimeException("Failed to load macaw properties", e);
                }
        }
        return macawProperties;
    }
    
    public Properties loadMacawProperties(InputStream is) {
        
        macawProperties = new Properties();
        try {
        	macawProperties.load(is);
		} catch (IOException e) {
			logger.error("problem loading config file", e);
			return null;
		}
       
        return macawProperties;
    }

    public String getRequiredProperty(String propertyName, String errorMessage) {
        String value = macawProperties.getProperty(propertyName);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return value;
    }

    public String getProperty(String name, String defaultValue) {
    
            String value = macawProperties.getProperty(name, defaultValue);
            if (value == null) {
                    throw new IllegalArgumentException("Macaw property " + name + "does not exist");
            }
            return value;
    }
    
    public void initBucket(MinioClient client, String bucketName) {
        boolean isExist = false;
        try {
            isExist = client.bucketExists(bucketName);
            if( ! isExist ) {
                client.makeBucket(bucketName);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }
    
    
}