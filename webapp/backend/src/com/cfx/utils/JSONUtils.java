package com.cfx.utils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.json.XML;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

public class JSONUtils {

    private static final Gson GSON = new GsonBuilder().create();

    /*
     * Convert any given object to JSON
     */
    public static String getValueAsStr(JsonObject jsObj, String key) {
        return getAsString(jsObj.get(key));
    }

    public static String getAsString(JsonElement el, String defaultValue) {
        if (el == null || el.isJsonNull())
            return defaultValue;

        if (el.isJsonObject() || el.isJsonArray()) {
            return el.toString();
        } else {
            String value = el.getAsString();

            if (StringUtils.isEmpty(value)) {
                return defaultValue;
            }
            return value;
        }
    }

    public static String getAsString(JsonElement el) {
        return getAsString(el, null);
    }

    public static String jsonize(Object o) {
        return GSON.toJson(o);
    }

    public static String objectifyMap(Map<String, Object> map) {
        return GSON.toJson(map);
    }

    public static JsonObject getJsonObjectByString(String jsonStr) {
        return (JsonObject) getJsonElementByString(jsonStr);
    }

    public static JsonArray getJsonArrayByString(String jsonStr) {
        return (JsonArray) getJsonElementByString(jsonStr);
    }

    public static JsonElement getJsonElementByString(String jsonStr) {

        if (StringUtils.isEmpty(jsonStr) || "null".equals(jsonStr)) {
            return new JsonObject();
        }
        JsonElement jsonElement = null;
        try {
            jsonElement = new JsonParser().parse(jsonStr);
        } catch (JsonSyntaxException e) {
            jsonElement = new JsonPrimitive(jsonStr);
        }
        return jsonElement;
    }

    // *************** Client to Server JSON Parser Start *********************
    public static JsonObject buildServerJsonObj(String jsonUIStr) {
        JsonElement jsonEl = getJsonElementByString(jsonUIStr);

        if (jsonEl.isJsonObject()) {
            return buildServerJsonObj(jsonEl.getAsJsonObject());
        } else {
            throw new JsonParseException("Not a valid json Object.");
        }
    }

    public static JsonObject buildServerJsonObj(JsonObject uiJsonObj) {
        JsonObject serverJsonObj = new JsonObject();
        for (Entry<String, JsonElement> entrySet : uiJsonObj.entrySet()) {
            String key = entrySet.getKey();

            JsonElement value = entrySet.getValue();

            if (key.contains(".")) {
                getValue(key, value, serverJsonObj);

            } else {
                serverJsonObj.add(key, uiJsonObj.get(key));
            }
        }
        return serverJsonObj;
    }

    public static JsonObject buildServerJsonObj(JsonObject uiJsonObj, JsonObject serverJsonObj) {
        for (Entry<String, JsonElement> entrySet : uiJsonObj.entrySet()) {
            String key = entrySet.getKey();

            JsonElement value = entrySet.getValue();

            if (key.contains(".")) {
                getValue(key, value, serverJsonObj);

            } else {
                serverJsonObj.add(key, uiJsonObj.get(key));
            }
        }
        return serverJsonObj;
    }

    private static JsonElement getValue(String identifier, JsonElement source, JsonObject target) {
        String[] ids = identifier.split("\\.");

        JsonObject obj;
        if (ids.length > 1) {
            String id = ids[0];
            String updatedId = identifier.replaceFirst(id + ".", "");

            JsonElement jsonEl = target.get(id);
            if (jsonEl != null && jsonEl.isJsonObject()) {
                obj = jsonEl.getAsJsonObject();
            } else {
                obj = new JsonObject();
                target.add(id, obj);
            }
            getValue(updatedId, source, obj);
        } else {
            if (source.isJsonArray()) {
                target.add(identifier, buildServerJsonArray(source.getAsJsonArray()));
            } else if (source.isJsonObject()) {
                target.add(identifier, buildServerJsonObj(source.getAsJsonObject()));
            } else {
                target.add(identifier, source);
            }
        }

        return target;
    }

    public static JsonArray buildServerJsonArray(JsonArray sourceArray) {

        JsonArray targetArray = new JsonArray();
        sourceArray.forEach(new Consumer<JsonElement>() {
            @Override
            public void accept(JsonElement t) {
                if (t.isJsonObject()) {
                    targetArray.add(buildServerJsonObj(t.getAsJsonObject()));
                } else if (t.isJsonArray()) {
                    targetArray.add(buildServerJsonArray(t.getAsJsonArray()));
                } else {
                    targetArray.add(t);
                }
            }
        });

        return targetArray;
    }

    public static JsonArray buildServerJsonArray(String arrayAsStr) {

        JsonElement jsonEl = getJsonElementByString(arrayAsStr);

        if (jsonEl.isJsonArray()) {
            return buildServerJsonArray(jsonEl.getAsJsonArray());
        } else {
            throw new JsonParseException("Not a valid json array.");
        }
    }

    public static JsonArray buildJsonArray(String arrayAsStr) {

        JsonElement jsonEl = getJsonElementByString(arrayAsStr);

        if (jsonEl.isJsonObject()) {
            JsonArray tmpArray = new JsonArray();
            tmpArray.add(jsonEl);
            jsonEl = tmpArray;
        }

        if (jsonEl.isJsonArray()) {
            return jsonEl.getAsJsonArray();
        } else {
            throw new JsonParseException("Not a valid json array.");
        }
    }

    // *************** Server to Client JSON Parser End *********************

    public static JsonObject mergeJsonObjects(List<JsonObject> jsonObjects) {
        JsonObject data = new JsonObject();

        if (jsonObjects != null && !jsonObjects.isEmpty()) {
            for (JsonObject obj : jsonObjects) {
                for (Entry<String, JsonElement> entrySet : obj.entrySet()) {
                    data.add(entrySet.getKey(), entrySet.getValue());
                }
            }
        }
        return data;
    }

    public static void mergeJsonObjects(JsonObject parentObj, JsonObject... children) {

        if (children != null && children.length > 0) {
            for (JsonObject obj : children) {
                for (Entry<String, JsonElement> entrySet : obj.entrySet()) {
                    parentObj.add(entrySet.getKey(), entrySet.getValue());
                }
            }
        }
    }

    public static JsonObject mergeCriteriaObjects(JsonObject defaultCriteria, JsonObject filterCriteria) {
        JsonObject searchCriteria = new JsonObject();
        JsonArray conditions = new JsonArray();
        JsonElement orderBy = null;
        JsonArray links = new JsonArray();

        if (defaultCriteria != null) {
            JsonElement cc = defaultCriteria.get("conditions");
            if (cc != null) {
                conditions.addAll(cc.getAsJsonArray());
            }
            JsonElement cl = defaultCriteria.get("condition-links");
            if (cl != null) {
                links.addAll(cl.getAsJsonArray());
            }
            JsonElement co = defaultCriteria.get("order-by");
            if (co != null && co.getAsJsonArray().size() > 0) {
                orderBy = co.getAsJsonArray().get(0);
            }
            JsonElement maxResults = defaultCriteria.get("max-results");
            if (maxResults != null) {
                searchCriteria.add("max-results", maxResults);
            }
            JsonElement offset = defaultCriteria.get("results-offset");
            if (offset != null) {
                searchCriteria.add("results-offset", offset);
            }
        }

        if (filterCriteria != null) {
            JsonElement cc = filterCriteria.get("conditions");
            if (cc != null) {
                conditions.addAll(cc.getAsJsonArray());
            }
            JsonElement cl = filterCriteria.get("condition-links");
            if (cl != null) {
                links.addAll(cl.getAsJsonArray());
            }
            JsonElement co = filterCriteria.get("order-by");
            if (co != null && co.getAsJsonArray().size() > 0) {
                orderBy = co.getAsJsonArray().get(0);
            }
            JsonElement maxResults = filterCriteria.get("max-results");
            if (maxResults != null) {
                searchCriteria.add("max-results", maxResults);
            }
            JsonElement offset = filterCriteria.get("results-offset");
            if (offset != null) {
                searchCriteria.add("results-offset", offset);
            }
        }

        if (conditions.size() > 0) {
            searchCriteria.add("conditions", conditions);
        }
        if (orderBy != null) {
            JsonArray orderByArray = new JsonArray();
            orderByArray.add(orderBy);
            searchCriteria.add("order-by", orderByArray);
        }
        if (links.size() > 0) {
            searchCriteria.add("condition-links", mergeConditionLinks(links));
        }
        return searchCriteria;
    }

    private static JsonArray mergeConditionLinks(JsonArray conditionLinks) {
        JsonArray links = new JsonArray();

        String linksAsStr = null;
        for (JsonElement jE : conditionLinks) {
            JsonObject obj = jE.getAsJsonObject();
            String cl = getAsString(obj.get("link"));
            if (cl != null) {
                if (linksAsStr == null) {
                    linksAsStr = cl;
                } else {
                    linksAsStr += " and " + cl;
                }
            }
        }
        if (linksAsStr != null) {
            JsonObject link = new JsonObject();
            link.addProperty("link", linksAsStr);
            links.add(link);
        }
        return links;
    }

    public static String XMLToJSONStr(String xmlAsStr) {

        JSONObject object = XML.toJSONObject(xmlAsStr);
        return object.toString();
    }
}
