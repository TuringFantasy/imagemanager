package com.cfx.restapi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

import com.cfx.utils.CustomHttpClientException;
import com.cfx.utils.JSONUtils;
import com.cfx.utils.ServerConnector;
import com.cfx.utils.ServiceConfig;
import com.cfx.utils.ServiceExecutionException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.research.ws.wadl.HTTPMethods;

import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.NoResponseException;

@Path("/services")
public class ServicesAPI {
	
	private final static Logger logger = LoggerFactory.getLogger(ServicesAPI.class);
	
	private String token = null;
	@POST
    @Path("/api")
    public Response browseApi(String serviceRequestDescriptor) throws ServiceExecutionException {
		JsonObject serviceRequestDescriptorJSON = (JsonObject) JSONUtils.getJsonElementByString(serviceRequestDescriptor);
        if(serviceRequestDescriptorJSON.get("serviceRequestDescriptor") != null) {
            JsonObject serviceRequestJson = (JsonObject)serviceRequestDescriptorJSON.get("serviceRequestDescriptor");

            String serviceName = ServiceConfig.getInstance().getServiceName();
            String serviceNamespace = ServiceConfig.getInstance().getServiceNamespace();
            String serviceVersion = ServiceConfig.getInstance().getServiceVersion();

            String methodName = serviceRequestJson.get("methodName").getAsString();
            String paramsStr = serviceRequestJson.get("params").toString();
            String url = "/service/" + serviceNamespace + "/" + serviceName + "/" + serviceVersion + "/" + methodName;
            //String token = serviceRequestJson.get("token").getAsString();
            
            logger.info(paramsStr);
            
            try {
                String response = ServerConnector.getInstance().execute(url, HTTPMethods.POST, paramsStr, false, getToken());
                JsonObject responseObj = new JsonObject();
                JsonElement o = JSONUtils.getJsonElementByString(String.valueOf(response));
                responseObj.add("serviceResult", o);
                responseObj.addProperty("serviceError", "none");
                return Response.ok(JSONUtils.jsonize(responseObj)).build();
            } catch (ServiceExecutionException e) {
                JsonObject responseObj = new JsonObject();
                JsonElement o = JSONUtils.getJsonElementByString(String.valueOf(e.getMessage()));
                responseObj.add("serviceError", o);
                return Response.ok(JSONUtils.jsonize(responseObj)).build();
            } catch(Exception e){
                String errorMessage = "serviceError : Missing method invocation parameters in service invocation request";
                JsonObject responseObj = new JsonObject();
                JsonElement o = JSONUtils.getJsonElementByString(String.valueOf(errorMessage));
                responseObj.add("serviceError", o);
                return Response.ok(JSONUtils.jsonize(responseObj)).build();
            }
        }
        return Response.ok("Invalid Request").build();
    }
	
	@GET
	@Path("/token")
	public String getToken() throws CustomHttpClientException, IOException {
		//TODO: as there is no login process right now, we use the default credentials to
		//get access to api gateway
		
		/*
		if (token != null) {
			return token;
		}
		*/
		String user = ServiceConfig.getInstance().getUserid();
		String password = ServiceConfig.getInstance().getPassword();
		JsonObject loginDetails = new JsonObject();
		loginDetails.addProperty("user", user);
		loginDetails.addProperty("password", password);
		String response = ServerConnector.getInstance().gatewayLogin(loginDetails);
		JsonObject loginResponse = JSONUtils.getJsonObjectByString(response);
		token = loginResponse.get("apiGatewaySessionId").getAsString();
		return token;
	}
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(@FormDataParam("file") InputStream uploadedInputStream, 
			@FormDataParam("file") FormDataContentDisposition fileDetail,
			@FormDataParam("token") String token,
			@FormDataParam("customer") String customer,
			@FormDataParam("code") String code)  throws Exception{

        String fileName = fileDetail.getFileName();

        File relativeFile = new File(fileDetail.getFileName());
        String relativeFilePath = relativeFile.getAbsolutePath();

        String tomcatPath = relativeFilePath.substring(0, relativeFilePath.lastIndexOf(File.separator));
        String tempFolderPath = tomcatPath + File.separator + "tmp";
        String tempFileLocation = tempFolderPath + File.separator + fileDetail.getFileName() ;

        try {
        	File targetFile = new File(tempFileLocation);

            FileUtils.copyInputStreamToFile(uploadedInputStream, targetFile);
        	//writeToFile(uploadedInputStream, tempFileLocation);

            // logger.info("input stream should be copied into" + targetFile.getAbsolutePath());

            Boolean secure = true ;
            
            String minioServer = ServiceConfig.getInstance().getMinioServer();
            String port = ServiceConfig.getInstance().getMinioPort();
            String accessKey = ServiceConfig.getInstance().getMinioAccessKey();
            String secretKey = ServiceConfig.getInstance().getMinioSecretKey();

            MinioClient minioClient = new MinioClient(minioServer, Integer.parseInt( port ), accessKey, secretKey, secure);
            minioClient.ignoreCertCheck();
            
            ServiceConfig.getInstance().initBucket(minioClient, "imagemanager");

            minioClient.putObject("imagemanager", fileName, targetFile.getAbsolutePath());

            String serviceName = ServiceConfig.getInstance().getServiceName();
            String serviceNamespace = ServiceConfig.getInstance().getServiceNamespace();
            String serviceVersion = ServiceConfig.getInstance().getServiceVersion();

            String methodName = "uploadImage";
            String url = "/service/" + serviceNamespace + "/" + serviceName + "/" + serviceVersion + "/" + methodName;
            
            JsonObject params = new JsonObject();
            JsonArray args = new JsonArray();
            
            args.add(new JsonPrimitive(code));
            args.add(new JsonPrimitive("minio://imagemanager/" + fileName));
            args.add(new JsonPrimitive(""));
                        
            params.add("params", args);
            
            String paramsStr = params.toString();

            String response = ServerConnector.getInstance().execute(url, HTTPMethods.POST, paramsStr, false, getToken());
            //JsonObject responseObj = new JsonObject();
            //JsonElement o = JSONUtils.getJsonElementByString(String.valueOf(response));
            if (response != null) {
            	//uploadImage api call was successfultoken
            }
            
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("bucketName", "imagemanager");
            jsonResponse.addProperty("objectName", fileName );
            jsonResponse.addProperty("displayName", fileDetail.getFileName() );
            return Response.status(200).entity( jsonResponse.toString() ).build();
            
        }
        catch (IOException e) {
         	logger.error("internal configuration error", e);
            return Response.status(500).entity("internal configuration error").build();
        } catch (KeyManagementException e) {
         	logger.error("internal configuration error", e);
            return Response.status(500).entity("internal configuration error").build();
		} catch (InvalidEndpointException e) {
         	logger.error("internal configuration error", e);
            return Response.status(500).entity("internal configuration error").build();
		} catch (InvalidPortException e) {
         	logger.error("internal configuration error", e);
            return Response.status(500).entity("internal configuration error").build();
		} catch (InvalidKeyException e) {
         	logger.error("internal configuration error", e);
            return Response.status(500).entity("internal configuration error").build();
		} catch (InvalidBucketNameException e) {
         	logger.error("internal configuration error", e);
            return Response.status(500).entity("internal configuration error").build();
		} catch (NoSuchAlgorithmException e) {
         	logger.error("internal configuration error", e);
            return Response.status(500).entity("internal configuration error").build();
		} catch (InsufficientDataException e) {
         	logger.error("internal configuration error", e);
            return Response.status(500).entity("internal configuration error").build();
		} catch (NoResponseException e) {
         	logger.error("communication error", e);
            return Response.status(408).entity("communication error").build();
		} catch (ErrorResponseException e) {
         	logger.error("communication error", e);
            return Response.status(408).entity("communication error").build();
		} catch (InternalException e) {
         	logger.error("internal error", e);
            return Response.status(500).entity("internal error").build();
		} catch (XmlPullParserException e) {
         	logger.error("internal configuration error", e);
            return Response.status(500).entity("internal configuration error").build();
		} catch (InvalidArgumentException e) {
         	logger.error("invalid arguments", e);
            return Response.status(400).entity("invalid arguments").build();
		}

	}
	
	@GET
	@Path("/image")
	public Response getObjectFromMinio(@QueryParam("fileName") String fileName) {
		Boolean secure = true ;

		String minioServer = ServiceConfig.getInstance().getMinioServer();
        String port = ServiceConfig.getInstance().getMinioPort();
        String accessKey = ServiceConfig.getInstance().getMinioAccessKey();
        String secretKey = ServiceConfig.getInstance().getMinioSecretKey();
        
        String bucketName = "imagemanager";

		MinioClient minioClient;
		try {
			minioClient = new MinioClient(minioServer, Integer.parseInt( port ), accessKey, secretKey, secure);
	        minioClient.ignoreCertCheck();

			ObjectStat metaData = minioClient.statObject(bucketName, fileName);
			String contentType = metaData.contentType();

			InputStream is = minioClient.getObject(bucketName, fileName);

			StreamingOutput stream = new StreamingOutput() {
				@Override
				public void write(OutputStream output) throws IOException {
					try {
						//write file content to output;
						byte[] buf = new byte[16384];
						int bytesRead;
						while ((bytesRead = is.read(buf, 0, buf.length)) >= 0) {
							output.write(buf, 0, bytesRead);
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						is.close();
						output.flush();
						output.close();
					}
				}
			};

			return Response.ok(stream, contentType) //set content-type of your file
					.build();


		} catch (NumberFormatException | InvalidEndpointException | InvalidPortException |
				KeyManagementException | NoSuchAlgorithmException | InvalidKeyException |
				InvalidBucketNameException | InsufficientDataException | NoResponseException
				| ErrorResponseException | InternalException | IOException | XmlPullParserException
				| InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Response.status(Response.Status.NOT_FOUND).build();
	}
	
	public static void main(String [] argv) {
		
	}

}
