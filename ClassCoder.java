package edu.duke.ece651.classbuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class ClassCoder {

  private HashMap<String, ArrayList<String>> classfields;
  private HashSet<String> primitives = new HashSet<>(
      Arrays.asList("boolean", "byte", "char", "short", "int", "long", "float", "double"));

  ClassCoder(HashMap<String, ArrayList<String>> rhsfields) {
    classfields = rhsfields;
  }

  private String capitalize(String fieldName) {
    if (fieldName.isEmpty()) {
      return "";
    } else {
      return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }
  }

  private String[] getArrayType(int dimension, String dataType) {
    String[] arrayType = { "", "" };
    String arrayName = "";
    String arrayVal = "";
    // String arrayInstance = "";
    if (dimension == 1) {
      arrayName = "ArrayList<" + dataType + ">";
      // arrayName = "Collection<" + dataType + ">";
      arrayVal = dataType;
    } else {
      for (int i = 0; i < dimension - 1; i++) {
        // arrayVal += "Collection<";
        arrayVal += "ArrayList<";
      }
      arrayVal += dataType;
      // arrayInstance += dataType;
      for (int j = 0; j < dimension - 1; j++) {
        arrayVal += ">";
        // arrayInstance += ">";
      }
      // arrayName = "Collection<" + arrayVal + ">";
      arrayName = "ArrayList<" + arrayVal + ">";
    }
    arrayType[0] = arrayName;
    arrayType[1] = arrayVal;
    return arrayType;
  }

  private String getArraySerializer(int dimension, String dataType, String name) {
    String res = "";
    for (int i = 1; i <= dimension; i++) {
      String[] arrayType = getArrayType(dimension - i + 1, dataType);
      res += "   ";
      res += "private JSONArray " + " parse_" + Integer.toString(i) + "_" + name + "(" + arrayType[0]
          + " toParse, HashMap<Object,Integer> serialMap) throws JSONException{\n";
      res += "   ";
      res += "JSONArray jarr = new JSONArray();\n";
      if (i < dimension) {
        res += "   ";
        res += "for (int i = 0; i < toParse.size(); i++){\n";
        res += "   ";
        res += "jarr.put(" + " parse_" + Integer.toString(i + 1) + "_" + name + "(toParse.get(i), serialMap));\n";
        res += "   ";
        res += "}\n";
        res += "   ";
        res += "return jarr;\n   }\n";
      } else {
        if (primitives.contains(arrayType[1]) || arrayType[1].equals("String")) {
          res += "for (int i = 0; i < toParse.size(); i++){\n";
          res += "   ";
          res += "jarr.put(toParse.get(i));\n";
          res += "   ";
          res += "}\n";
          res += "   ";
          res += "return jarr;\n   }\n";
        } else {
          if (!classfields.containsKey(arrayType[1])) {
            System.out.println("No such class package or type: " + arrayType[1]);
          } else {
            res += "for (int i = 0; i < toParse.size(); i++){\n";
            res += "   ";
            res += "if (serialMap.containsKey(toParse.get(i)) ){\n";
            res += "   ";
            res += "JSONObject fieldObjElement = new JSONObject();\n";
            res += "   ";
            res += "fieldObjElement.put(" + " \"ref\" " + ", " + "serialMap.get(toParse.get(i)));\n";
            res += "jarr.put(fieldObjElement);\n";
            res += "   ";
            res += "}\n";
            res += "   ";
            res += "else{\n";
            res += "   ";
            res += "jarr.put(toParse.get(i).__toJSON__Helper(serialMap)" + ");\n";
            res += "   ";
            res += "}\n";
            res += "}\n";
            res += "   ";
            res += "return jarr;\n   }\n";
          }
        }
      }
    }
    return res;
  }

  private String getArrayCode(String type, String name) {
    String res = "";
    int first = type.indexOf("#");
    int last = type.substring(first + 1).indexOf("#");
    int dimension = Integer.parseInt(type.substring(first + 1, last + 1));
    String dataType = type.substring(last + 2);
    String[] arrayType = getArrayType(dimension, dataType);
    res += "private" + " " + arrayType[0] + " " + name + ";\n";
    res += "   ";
    // numX
    res += "public" + " " + "int " + "num" + capitalize(name) + "(){\n";
    res += "   ";
    res += "return" + " " + "this." + name + ".size()" + ";\n";
    res += "   ";
    res += "}\n";
    // addX
    res += "public" + " " + "void" + " " + "add" + capitalize(name) + "(" + arrayType[1] + " " + "P_" + name + "){\n";
    res += "   ";
    res += "this." + name + ".add(" + "P_" + name + ");\n";
    res += "   ";
    res += "return" + ";\n";
    res += "   ";
    res += "}\n";
    // getX
    res += "public" + " " + arrayType[1] + " " + "get" + capitalize(name) + "(" + "int" + " " + "P_index" + "){\n";
    res += "   ";
    res += "return" + " " + "this." + name + ".get(" + "P_index" + ");\n";
    res += "   ";
    res += "}\n";
    // setX
    res += "public" + " " + "void" + " " + "set" + capitalize(name) + "(" + "int" + " " + "P_index" + ", "
        + arrayType[1] + " " + "P_" + name + "){\n";
    res += "   ";
    res += "this." + name + ".set(" + "P_index" + ", " + "P_" + name + ");\n";
    res += "   ";
    res += "return" + ";\n";
    res += "   ";
    res += "}\n";
    // parseX
    res += getArraySerializer(dimension, dataType, name);
    return res;
  }

  private String getFieldCode(String type, String name) {
    String res = "";
    if (type.charAt(0) == '#') {
      res += getArrayCode(type, name);
    } else {
      res += "private" + " " + type + " " + name + ";\n";
      res += "   ";
      res += "public" + " " + type + " " + "get" + capitalize(name) + "(){\n";
      res += "   ";
      res += "return" + " " + "this." + name + ";\n";
      res += "   ";
      res += "}\n";
      res += "   ";
      res += "public" + " " + "void" + " " + "set" + capitalize(name) + "(" + type + " " + "P_" + name + "){\n";
      res += "   ";
      res += "this." + name + " " + "=" + " " + "P_" + name + ";\n";
      res += "   ";
      res += "return;\n";
      res += "   ";
      res += "}\n";
    }
    return res;
  }

  private String getConstructor(String className, ArrayList<String> classMap) {
    String result = "public " + className + "() " + "{" + "\n";
    for (int i = 0; i < classMap.size(); i++) {
      String name = classMap.get(i).substring(0, classMap.get(i).indexOf("="));
      String type = classMap.get(i).substring(classMap.get(i).indexOf("=") + 1);
      if (type.charAt(0) == '#') {
        int first = type.indexOf("#");
        int last = type.substring(first + 1).indexOf("#");
        int dimension = Integer.parseInt(type.substring(first + 1, last + 1));
        String dataType = type.substring(last + 2);
        String[] arrayType = getArrayType(dimension, dataType);
        result += "   ";
        result += "this. " + name + "=" + "new" + " " + arrayType[0] + "();\n";
      }
    }
    result += "   }\n";
    return result;
  }

  private String getSerializerHelper(String className, ArrayList<String> classMap) {
    String result = "public JSONObject __toJSON__Helper(HashMap<Object,Integer> serialMap) throws JSONException{\n";
    result += "   ";
    result += "serialMap.put(" + "this" + ", " + "serialMap.size()" + ");\n";
    result += "   ";
    result += "JSONObject jobj = new JSONObject();\n";
    result += "   ";
    result += "jobj.put(" + "\"id\"" + ", " + "serialMap.get(this)" + ");\n";
    result += "   ";
    result += "jobj.put(" + "\"type\"" + ", " + "\"" + className + "\"" + ");\n";
    result += "   ";
    result += "JSONArray fieldArr = new JSONArray();\n";
    for (int i = 0; i < classMap.size(); i++) {
      String name = classMap.get(i).substring(0, classMap.get(i).indexOf("="));
      String type = classMap.get(i).substring(classMap.get(i).indexOf("=") + 1);
      if (primitives.contains(type) || type.equals("String")) {
        result += "   ";
        result += "JSONObject fieldObj" + capitalize(name) + " = new JSONObject();\n";
        result += "   ";
        result += "fieldObj" + capitalize(name) + ".put(" + "\"" + name + "\"" + ", " + "this." + name + ");\n";
        result += "   ";
        result += "fieldArr.put(fieldObj" + capitalize(name) + ");\n";
      } else if (type.charAt(0) == '#') {
        result += "JSONArray parsedArr = parse_1_" + name + "( " + name + ", " + "serialMap);\n";
        result += "   ";
        result += "JSONObject fieldObj" + capitalize(name) + " = new JSONObject();\n";
        result += "   ";
        result += "fieldObj" + capitalize(name) + ".put(" + "\"" + name + "\"" + ", " + "parsedArr" + ");\n";
        result += "   ";
        result += "fieldArr.put(fieldObj" + capitalize(name) + ");\n";
      } else {
        if (!classfields.containsKey(type)) {
          System.out.println("No such class package or type: " + type);
        } else {
          result += "   ";
          result += "if (serialMap.containsKey(" + "this." + name + ") ){\n";
          result += "   ";
          result += "JSONObject fieldObj" + capitalize(name) + " = new JSONObject();\n";
          result += "   ";
          result += "fieldObj" + capitalize(name) + ".put(" + " \"ref\" " + ", " + "serialMap.get(" + "this." + name
              + "));\n";
          result += "   ";
          result += "fieldArr.put(fieldObj" + capitalize(name) + ");\n";
          result += "   ";
          result += "}\n";
          result += "   ";
          result += "else{\n";
          result += "   ";
          result += "JSONObject fieldObj" + capitalize(name) + " = new JSONObject();\n";
          result += "   ";
          result += "fieldObj" + capitalize(name) + ".put(" + "\"" + name + "\"" + ", " + "this." + name
              + ".__toJSON__Helper(serialMap)" + ");\n";
          result += "   ";
          result += "fieldArr.put(fieldObj" + capitalize(name) + ");\n";
          result += "   ";
          result += "}\n";
          result += "   ";
        }
      }
    }
    result += "   ";
    result += "jobj.put(" + "\"values\"" + ", " + "fieldArr" + ");\n";
    result += "   ";
    result += "return jobj;\n";
    result += "   }\n";
    return result;

  }

  private String getSerializer() {
    String result = "public" + " " + "JSONObject" + " " + "toJSON()" + " " + "throws JSONException {\n";
    result += "   ";
    result += "HashMap<Object,Integer> serialMap = new HashMap<Object,Integer>();\n";
    result += "   ";
    result += "return this.__toJSON__Helper(serialMap);\n";
    result += "   ";
    result += "}\n";
    return result;
  }

  private String getDeserializer() {
    String res = "import org.json.*;\nimport java.util.*;\n";
    res += "public class " + "Deserializer" + "{" + "\n";
    res += "   ";
    res += "private HashMap<Integer,Object> deserialMap = new HashMap<Integer,Object>();\n";
    for (HashMap.Entry<String, ArrayList<String>> classEntry : classfields.entrySet()) {
      String classname = classEntry.getKey();
      ArrayList<String> fields = classEntry.getValue();
      res += "public " + classname + " read" + capitalize(classname) + "(JSONObject jobj){\n";
      res += "   ";
      res += "int id = jobj.getInt(\"id\");\n";
      res += "   ";
      res += classname + " result = new " + classname + " ();\n";
      res += "   ";
      res += "deserialMap.put(id,result);\n";
      for (int i = 0; i < fields.size(); i++) {
        String name = fields.get(i).substring(0, fields.get(i).indexOf("="));
        String type = fields.get(i).substring(fields.get(i).indexOf("=") + 1);
        if (primitives.contains(type) || type.equals("String")) {
          res += "   ";
          res += "result.set" + capitalize(name) + "(("+ type +")jobj.getJSONArray(\"values\").getJSONObject("+ Integer.toString(i) + ").get(" +"\"" + name + "\"" + "));\n" ;
        } else if (type.charAt(0) == '#') {
          System.out.println("array");
        } else {
          res += "  ";
          res += "if (jobj.getJSONArray(\"values\").getJSONObject(" + Integer.toString(i) + ").keySet().contains(\"ref\")){\n";
          res += "  ";
          res += "result.set" + capitalize(name) + "(deserialMap.get(jobj.getJSONArray(\"values\").getJSONObject("+ Integer.toString(i) + ").getInt(" +"\"" + name + "\"" + ")));\n";
          res += "  ";
          res += "}\n";
          res += "   ";
          res += "else{\n";
          res += "result.set" + capitalize(name) + "(read"+ capitalize(type) +"(jobj.getJSONArray(\"values\").getJSONObject("+ Integer.toString(i) + ").getJSONObject(" +"\"" + name + "\"" + ")));\n";
          res += "  ";
          res += "}\n";
        }
      }
      res += "   ";
      res += "}\n";
    }
    res += "}\n";
    return res;
  }

  public String buildSourceCode(String className) {
    String result = "import org.json.*;\n import java.util.*;\n";
    result += "public class " + className + "{" + "\n";
    if (className.equals("Deserializer")) {
      result += getDeserializer();
    } else {
      ArrayList<String> classMap = classfields.get(className);
      for (int i = 0; i < classMap.size(); i++) {
        String name = classMap.get(i).substring(0, classMap.get(i).indexOf("="));
        String type = classMap.get(i).substring(classMap.get(i).indexOf("=") + 1);
        result += getFieldCode(type, name);
        result += "\n";
      }
      result += getConstructor(className, classMap);
      result += getSerializerHelper(className, classMap);
      result += getSerializer();
      result += "}\n";
    }
    return result;
  }
}
/*
 * result += "   "; result += "JSONArray fieldArr = new JSONArray();\n"; result
 * += "   "; result += "for (int i = 0; i < serialArray.size(); i++){\n"; result
 * += "   "; result += "if (serialArray.get(i) instanceof ArrayList<?>){\n";
 * result += "   "; result +=
 * "fieldArr.put(this.__toJSON__Array(serialArray.get(i), isPrimitive));\n";
 * result += "   "; result += "}\n"; result += "   "; result += "else{\n";
 * result += "   "; result += "if (isPrimitive){\n"; result +=
 * "fieldArr.put(serialArray.get(i));\n"; result += "   "; result += "   }\n";
 * result += "   "; result += "else {\n"; result +=
 * "fieldArr.put(serialArray.get(i).toJSON());\n"; result += "   "; result +=
 * "}\n"; result += "   "; result += "}\n"; result += "   "; result += "}\n";
 */
