package edu.duke.ece651.classbuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ClassExtractor {
  private HashSet<String> primitives = new HashSet<>(
      Arrays.asList("boolean", "byte", "char", "short", "int", "long", "float", "double"));

  private String extractArray(JSONObject fieldArray) throws JSONException { // not in the form of e:e:...?
    String arrayType = "";
    int dimension = 0;
    Object trygetString;
    try {
      while (true) {
        dimension++;
        trygetString = fieldArray.get("e");
        if (trygetString instanceof String) {
          break;
        } else {
          fieldArray = fieldArray.getJSONObject("e");
        }
      }
    } catch (JSONException jex) {
      System.out.println("invalid format of array");
      throw jex;
    }
    arrayType = "#" + Integer.toString(dimension) + "#" + trygetString;
    return arrayType;
  }

  public HashMap<String, ArrayList<String>> extractFields(JSONObject jobj) {
    HashMap<String, ArrayList<String>> classfields = new HashMap<String, ArrayList<String>>();
    JSONArray jarr = jobj.getJSONArray("classes");
    for (int i = 0; i < jarr.length(); i++) {
      JSONObject jsonObj = jarr.getJSONObject(i);
      ArrayList<String> fields = new ArrayList<String>();
      if (!jsonObj.isNull("fields")) {
        JSONArray jsonFields = jsonObj.getJSONArray("fields");
        for (int j = 0; j < jsonFields.length(); j++) {
          try {
            String fieldName = jsonFields.getJSONObject(j).get("name").toString();
            Object testType = jsonFields.getJSONObject(j).get("type");
            if (testType instanceof String) {
              String fieldType = jsonFields.getJSONObject(j).getString("type");
              if (primitives.contains(fieldType) || fieldType.equals("String")) {
                fields.add(fieldName + "=" + fieldType);
              } else {
                fields.add(fieldName + "=" + fieldType);
              }
            } else {
              JSONObject fieldArray = jsonFields.getJSONObject(j).getJSONObject("type");
              String arrayType = extractArray(fieldArray);
              fields.add(fieldName + "=" + arrayType);
            }
          } catch (JSONException jex) {
            System.out.println("invalid JSON format");
          }
        }
      }
      classfields.put(jsonObj.getString("name"), fields);
    }
    return classfields;
  }
}
