package us.oder.restfetcher.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public class JsonScrubber {
    private final String[] fieldsToScrub;

    public JsonScrubber(String[] fieldsToScrub) {
        this.fieldsToScrub = fieldsToScrub;
    }

    public String scrub(String json) {
        try {
            Gson gson = new Gson();
            Type stringStringMap = new TypeToken<Map<String, Object>>() {
            }.getType();
            Map<String, Object> map = gson.fromJson(json, stringStringMap);
            map = scrubMap(map);
            return gson.toJson(map);
        }catch (JsonSyntaxException e) {
            return json;
        } catch (NullPointerException e) {
            return json;
        }
    }

    private Map<String, Object> scrubMap(Map<String, Object> map) {
        for (String key : map.keySet()) {
            if (isFiltered(key)) {
                map.put(key, "************");
            } else if (map.get(key) instanceof Map ) {
                try {
                    Map<String, Object> subMap = (Map<String, Object>) map.get(key);
                    map.put(key, scrubMap(subMap));
                } catch (ClassCastException e) {
                    return map;
                }
            }
        }
        return map;
    }

    private boolean isFiltered(String key) {
        for (String filter : fieldsToScrub) {
            if (filter.equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }
}
