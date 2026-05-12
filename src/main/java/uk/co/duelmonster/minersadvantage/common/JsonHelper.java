package uk.co.duelmonster.minersadvantage.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class JsonHelper {
    private static final Gson gson = new Gson();

    public static JsonObject ParseObject(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    public static JsonObject ParseObject(String[] stringList) {
        JsonObject json = new JsonObject();
        if (stringList == null) {
            return json;
        }

        for (String value : stringList) {
            if (value != null && !value.isBlank()) {
                json.addProperty(value, "");
            }
        }
        return json;
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public static String[] toStringList(JsonObject json) {
        if (json == null || json.size() == 0) {
            return new String[0];
        }

        List<String> values = new ArrayList<>();
        for (Entry<String, JsonElement> entry : json.entrySet()) {
            if (!values.contains(entry.getKey())) {
                values.add(entry.getKey());
            }
        }
        return values.toArray(new String[0]);
    }

    public static boolean contains(JsonObject json, String key) {
        return json != null && key != null && json.has(key);
    }

    public static int size(JsonObject json) {
        return json == null ? 0 : json.entrySet().size();
    }

    public static boolean isEmpty(JsonObject json) {
        return size(json) == 0;
    }

    public static JsonArray GetArray(JsonObject json, String key) {
        if (json != null && json.has(key) && json.get(key).isJsonArray()) {
            return json.get(key).getAsJsonArray();
        }
        return new JsonArray();
    }

    public static JsonObject GetObject(JsonObject json, String key) {
        if (json != null && json.has(key) && json.get(key).isJsonObject()) {
            return json.get(key).getAsJsonObject();
        }
        return new JsonObject();
    }

    public static String GetString(JsonObject json, String key, String defaultValue) {
        if (json != null && json.has(key) && json.get(key).isJsonPrimitive() && json.get(key).getAsJsonPrimitive().isString()) {
            return json.get(key).getAsString();
        }
        return defaultValue;
    }

    public static Number GetNumber(JsonObject json, String key, Number defaultValue) {
        if (json != null && json.has(key) && json.get(key).isJsonPrimitive()) {
            try {
                return json.get(key).getAsNumber();
            } catch (Exception ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static boolean GetBoolean(JsonObject json, String key, boolean defaultValue) {
        if (json != null && json.has(key) && json.get(key).isJsonPrimitive()) {
            try {
                return json.get(key).getAsBoolean();
            } catch (Exception ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static JsonObject ReadFromFile(File sourceFile) {
        if (sourceFile == null || !sourceFile.exists()) {
            return new JsonObject();
        }

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(sourceFile), StandardCharsets.UTF_8)) {
            JsonObject json = new Gson().fromJson(reader, JsonObject.class);
            return json == null ? new JsonObject() : json;
        } catch (Exception ex) {
            Constants.LOGGER.error("An error occurred while loading JSON from file", ex);
            return new JsonObject();
        }
    }

    public static void WriteToFile(File outputFile, JsonObject json) {
        if (outputFile == null) {
            return;
        }

        try {
            if (!outputFile.exists()) {
                File parent = outputFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                outputFile.createNewFile();
            }

            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(json == null ? new JsonObject() : json, writer);
            }
        } catch (Exception ex) {
            Constants.LOGGER.error("An error occurred while saving JSON to file", ex);
        }
    }

    public static void CopyPaste(File sourceFile, File outputFile) {
        if (sourceFile == null || outputFile == null) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            char[] buffer = new char[256];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, read);
            }
        } catch (Exception ex) {
            Constants.LOGGER.error("Failed copy-paste operation", ex);
        }
    }
}
