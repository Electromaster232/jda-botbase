package me.djelectro.djbot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The purpose of this class is to parse out the configuration.
 * By instantiating multiple instances of the class, you can parse multiple configs
 * (Not really sure why you would want to do that, but you can!)
 *
 * The intention is to eventually allow config value setting, both temporary and permanent
 */
public class Config {

  // Static instance of config, public to be assigned by anyone if needed for some reason.
  // Most values will have been read by the time this could be changed
  public static Config instance;
  private final String configFileName;
  private final HashMap<String, String> configMap;

  public Config(String fileName) throws IOException {
    this.configFileName = fileName;

    // Parse out config
    String fileContents = readFile(configFileName, Charset.defaultCharset());
    this.configMap = jsonToMap(fileContents);


  }

  public int getConfigSize(){
    return configMap.size();
  }

  public String getConfigFileName(){
    return configFileName;
  }

  /**
   * Get all keys
   * @return String array of all keys
   */
  public String[] getAllKeys(){
    String[] res = new String[getConfigSize()];
    final int[] i = {0};
    configMap.keySet().forEach((e) -> {res[i[0]] = e; i[0]++;});

    return res;
  }

  /**
   * Update the config with a new value
   * @param key The key to update. Will be created if it does not exist
   * @param value The value to set.
   */
  public void updateValue(String key, String value){
    configMap.remove(key);
    configMap.put(key, value);
  }

  /**
   * Gets a value from the configuration store
   * @param valueName The name of the value to retrieve
   * @return The string contents of the value
   */
  public String getConfigValue(String valueName){
    return configMap.get(valueName);
  }


  private static HashMap<String, String> jsonToMap(String t) throws JSONException {

    HashMap<String, String> map = new HashMap<>();
    JSONObject jObject = new JSONObject(t);
    Iterator<?> keys = jObject.keys();

    while( keys.hasNext() ){
      String key = (String)keys.next();
      String value = jObject.getString(key);
      map.put(key, value);
    }

    return map;
  }

  public String returnJsonObj(){
    ObjectMapper o = new ObjectMapper();
      try {
          return o.writeValueAsString(configMap);
      } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
      }
  }

  private static String readFile(String path, Charset encoding)
    throws IOException
  {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }
}
