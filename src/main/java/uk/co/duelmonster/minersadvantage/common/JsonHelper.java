package uk.co.duelmonster.minersadvantage.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonHelper {

  private static final Gson gson = new Gson();

  public static JsonObject ParseObject(String json) {
    return JsonParser.parseString(json).getAsJsonObject();
  }

  public static JsonObject ParseObject(String[] stringList) {
    String sValues = null;
    for (String sVal : stringList)
      sValues += ", \"" + sVal + "\":\"\"";

    return (sValues == null ? new JsonObject() : JsonHelper.ParseObject("{" + sValues.replaceFirst(", ", "") + "}"));
  }

  public static String toJson(Object obj) {
    return gson.toJson(obj);
  }

  public static <T> T fromJson(String json, Class<T> classOfT) {
    return gson.fromJson(json, classOfT);
  }

  public static String[] toStringList(JsonObject json) {
    String[] sResults = {};
    if (json.size() > 0) {
      String[]                             sValues = new String[json.size()];
      Iterator<Entry<String, JsonElement>> entries = json.entrySet().iterator();

      for (int iIndx = 0; iIndx < json.size(); iIndx++)
        if (entries.hasNext()) {
          String sEntry = entries.next().getKey();
          if (!ArrayUtils.contains(sValues, sEntry))
            sValues[iIndx] = sEntry;
        }

      sResults = sValues;
    }
    return sResults;

  }

  public static boolean contains(JsonObject json, String key) {
    json.has(key);
    return false;
  }

  public static int size(JsonObject json) {
    return json.entrySet().size();
  }

  public static boolean isEmpty(JsonObject json) {
    return json.entrySet().size() <= 0;
  }

  public static JsonArray GetArray(JsonObject json, String key) {
    if (json == null)
      return new JsonArray();

    if (json.has(key) && json.get(key).isJsonArray())
      return json.get(key).getAsJsonArray();
    else
      return new JsonArray();
  }

  public static JsonObject GetObject(JsonObject json, String key) {
    if (json == null)
      return new JsonObject();

    if (json.has(key) && json.get(key).isJsonObject())
      return json.get(key).getAsJsonObject();
    else
      return new JsonObject();
  }

  public static String GetString(JsonObject json, String key, String sDefault) {
    if (json == null)
      return sDefault;

    if (json.has(key) && json.get(key).isJsonPrimitive() && json.get(key).getAsJsonPrimitive().isString())
      return json.get(key).getAsString();
    else
      return sDefault;
  }

  public static Number GetNumber(JsonObject json, String key, Number iDefault) {
    if (json == null)
      return iDefault;

    if (json.has(key) && json.get(key).isJsonPrimitive()) {
      try {
        return json.get(key).getAsNumber();
      } catch (Exception ex) {
        return iDefault;
      }
    } else
      return iDefault;
  }

  public static boolean GetBoolean(JsonObject json, String key, boolean bDefault) {
    if (json == null)
      return bDefault;

    if (json.has(key) && json.get(key).isJsonPrimitive()) {
      try {
        return json.get(key).getAsBoolean();
      } catch (Exception ex) {
        return bDefault;
      }
    } else
      return bDefault;
  }

  public static JsonObject ReadFromFile(File sourceFile) {
    if (sourceFile == null || !sourceFile.exists())
      return new JsonObject();

    try {
      InputStreamReader reader = new InputStreamReader(new FileInputStream(sourceFile), StandardCharsets.UTF_8);
      JsonObject        json   = new Gson().fromJson(reader, JsonObject.class);
      reader.close();
      return json;
    } catch (Exception ex) {
      Constants.LOGGER.log(Level.ERROR, "An error occured while loading JSON from file:", ex);
      return new JsonObject();
    }
  }

  public static void WriteToFile(File outputFile, JsonObject json) {
    try {
      if (!outputFile.exists()) {
        if (outputFile.getParentFile() != null)
          outputFile.getParentFile().mkdirs();

        outputFile.createNewFile();
      }

      OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
      new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
      writer.close();
    } catch (Exception ex) {
      Constants.LOGGER.log(Level.ERROR, "An error occured while saving JSON to file:", ex);
      return;
    }
  }

  public static void CopyPaste(File sourceFile, File outputFile) {
    BufferedReader reader = null;
    BufferedWriter writer = null;

    try {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), StandardCharsets.UTF_8));
      writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8));

      char[] buffer = new char[256];
      int    read;
      while ((read = reader.read(buffer)) != -1)
        writer.write(buffer, 0, read);
    } catch (Exception ex) {
      Constants.LOGGER.log(Level.ERROR, "Failed copy paste", ex);
    } finally {
      try {
        reader.close();
        writer.close();
      } catch (Exception ex) {}
    }
  }
}
