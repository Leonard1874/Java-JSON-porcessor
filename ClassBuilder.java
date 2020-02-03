package edu.duke.ece651.classbuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ClassBuilder {
  private JSONObject jobj;
  private List<String> classnames;
  private HashMap<String, ArrayList<String>> classfields;

  ClassBuilder(String json) throws JSONException {
    classnames = new ArrayList<>();
    classfields = new HashMap<String, ArrayList<String>>();
    try {
      jobj = new JSONObject(json);
    } catch (JSONException jex) {
      System.out.println("Invalid JSON!");
    }
    ClassExtractor extractor = new ClassExtractor();
    classfields = extractor.extractFields(jobj);
  }

  ClassBuilder(InputStream ins) throws JSONException, IOException {
    classnames = new ArrayList<>();
    classfields = new HashMap<String, ArrayList<String>>();
    JSONTokener jtk = new JSONTokener(ins);
    try {
      jobj = new JSONObject(jtk);
    } catch (JSONException jex) {
      System.out.println("Invalid JSON!");
    }
    ClassExtractor extractor = new ClassExtractor();
    classfields = extractor.extractFields(jobj);
  }

  public Collection<String> getClassNames() throws JSONException {
    List<String> res = new ArrayList<>();
    res.add("Deserializer");
    try {
      JSONArray jarr = jobj.getJSONArray("classes");
      for (int i = 0; i < jarr.length(); i++) {
        String class_name = jarr.getJSONObject(i).getString("name");
        res.add(class_name);
      }
    } catch (JSONException jex) {
      System.out.println("Unexpected Json Format: no array");
    }
    classnames = res;
    return res;
  }

  public void createAllClasses(String basePath) throws JSONException, IOException {
    //System.out.println(classfields);
    String base = ".java";
    String packagePath = "";
    String code = "";
    if (!jobj.isNull("package")) {
      packagePath = jobj.getString("package") + "/";
    }
    if (classnames.isEmpty()) {
      getClassNames();
    }
    for (String classname : classnames) {
      System.out.println(classname);
      String classFileName = classname + base;
      String classFilePath = basePath + packagePath;
      try {
        File testFilePath = new File(basePath + packagePath);
        if (!testFilePath.isDirectory()) {
          testFilePath.mkdir();
        }
        PrintWriter writer = new PrintWriter(classFilePath + classFileName);
        code = getSourceCode(classname);
        System.out.println(code);
        writer.write(code);
        writer.close();
      } catch (IOException ioe) {
        System.out.println("IO Error");
      }
    }
  }

  public String getSourceCode(String className) {
    if (!classfields.containsKey(className) && !className.equals("Deserializer")) {
      System.out.println("unknown classname!");
      return "";
    } else {
      String code = "";
      ClassCoder coder = new ClassCoder(classfields);
      code += coder.buildSourceCode(className);
      return code;
    }
  }

  public void test() {
    String json = jobj.toString();
    System.out.println("test: " + json);
    System.out.println(classfields);
  }
}
