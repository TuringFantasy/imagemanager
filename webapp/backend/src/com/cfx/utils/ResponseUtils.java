package com.cfx.utils;

import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ResponseUtils {

    public static Response getResponseAsJsonObj(Object respObj) {
        return getResponseAsJsonObj(respObj, null);
    }

    public static Response getFormDataAsResponse(Object respObj, String error) {

        JsonObject responseObj = new JsonObject();
        JsonObject formDataObj = new JsonObject();
        JsonElement o = JsonNull.INSTANCE;
        if (respObj != null) {
            if (respObj instanceof JsonElement) {
                o = (JsonElement) respObj;
            } else if (respObj instanceof String) {
                o = JSONUtils.getJsonElementByString(String.valueOf(respObj));
            } else {
                o = new JsonParser().parse(JSONUtils.jsonize(respObj)).getAsJsonObject();
            }
        }

        formDataObj.add(Constants.FORM_DATA, o);
        responseObj.add(Constants.SERVICE_RESULT, formDataObj);
        responseObj.addProperty(Constants.SERVICE_ERROR, (error == null || error.isEmpty() ? Constants.NONE : error));

        return Response.ok(JSONUtils.jsonize(responseObj)).build();
    }

    public static Response createJsonResponseforDef(Object respObj, String error) {

        JsonObject responseObj = new JsonObject();

        JsonElement o = JsonNull.INSTANCE;
        if (respObj != null) {
            if (respObj instanceof JsonElement) {
                o = (JsonElement) respObj;
            } else if (respObj instanceof String) {
                o = JSONUtils.getJsonElementByString(String.valueOf(respObj));
            } else {
                o = new JsonParser().parse(JSONUtils.jsonize(respObj)).getAsJsonObject();
            }
        }

        responseObj.add(Constants.SERVICE_RESULT, o);
        responseObj.addProperty(Constants.SERVICE_ERROR, (error == null || error.isEmpty() ? Constants.NONE : error));

        return Response.ok(JSONUtils.jsonize(responseObj)).build();
    }

    public static Response getResponseAsJsonObj(Object respObj, String error) {

        JsonObject responseObj = new JsonObject();

        JsonElement o = JsonNull.INSTANCE;
        if (respObj != null) {
            if (respObj instanceof JsonElement) {
                o = (JsonElement) respObj;
            } else if (respObj instanceof String) {
                o = JSONUtils.getJsonElementByString(String.valueOf(respObj));
            } else {
                o = new JsonParser().parse(JSONUtils.jsonize(respObj));
            }
        }

        responseObj.add(Constants.SERVICE_RESULT, o);
        responseObj.addProperty(Constants.SERVICE_ERROR, (error == null || error.isEmpty() ? Constants.NONE : error));

        return Response.ok(JSONUtils.jsonize(responseObj)).build();
    }

    public static Response getResponseAsJsonObj(Map<String, Object> map, String error) {

        JsonObject responseObj = new JsonObject();

        if (map != null) {
            JsonElement o = new JsonParser().parse(JSONUtils.objectifyMap(map)).getAsJsonObject();
            JsonObject data = new JsonObject();
            data.add(Constants.DATA, o);
            responseObj.add(Constants.SERVICE_RESULT, data);
        } else {
            responseObj.add(Constants.SERVICE_RESULT, JsonNull.INSTANCE);
        }

        responseObj.addProperty(Constants.SERVICE_ERROR, (error == null || error.isEmpty() ? Constants.NONE : error));

        return Response.ok(JSONUtils.jsonize(responseObj)).build();
    }

    public static Response getErrorResponse(String error) {
        return getErrorResponse(error, true);
    }

    public static Response getErrorResponse(String error, boolean processErrorMessage) {
        if (processErrorMessage)
            return getResponseAsJsonObj(null, processErrorMessage(error));
        else
            return getResponseAsJsonObj(null, error);
    }

    public static String processErrorMessage(String error) {
        JsonElement jsonError = JSONUtils.getJsonElementByString(error);
        String message = error;
        if (jsonError.isJsonObject() && jsonError.getAsJsonObject().get("serviceError") != null) {
            String serviceError = JSONUtils.getAsString(jsonError.getAsJsonObject().get("serviceError"));
            if (serviceError.indexOf(":") != -1) {
                message = StringUtils.substringAfter(serviceError, ":");
            } else {
                message = serviceError;
            }

            if (message.contains("Lookup of service")
                    && message.contains("failed since there are no versions available for this service")) {
                message = jsonError.getAsJsonObject().get("serviceName") + " is not available";
            }
        }
        String[] array = message.split(":");
        StringBuffer messageBuffer = new StringBuffer();
        for (String s : array) {
            if (s.trim().startsWith("com.") || s.trim().startsWith("java.")) {
                continue;
            }
            messageBuffer.append(s).append(" ");
        }

        return messageBuffer.toString();
    }
}
