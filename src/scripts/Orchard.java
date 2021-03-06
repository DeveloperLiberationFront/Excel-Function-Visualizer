package scripts;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import core.Example;
import core.Token;
import core.FunctionNode;

public class Orchard {
  private final Map<String, FunctionNode> trees = new HashMap<String, FunctionNode>();
  private final Map<String, Map<Integer, Example>> examples 
      = new HashMap<String, Map<Integer, Example>>();
  private int totalFormulae = 0;
  
  public void add(Token formula, Example example) {
    String toplevel = formula.toSimpleString();
    if (!trees.containsKey(toplevel)) {
      trees.put(toplevel, new FunctionNode(toplevel));
      examples.put(toplevel, new HashMap<Integer, Example>());
    }
    
    trees.get(toplevel).add(formula, example);      
    examples.get(toplevel).put(example.getID(), example);
    
    ++totalFormulae;
  }
  
  public void flush(String outputDirectory) {
    //Lifted from AllTrees.java
    ArrayList<FunctionNode> sort = new ArrayList<FunctionNode>(trees.values());
    Collections.sort(sort);
    
    LinkedHashMap<String, FunctionNode> allFuncs = new LinkedHashMap<String, FunctionNode>();
    for (FunctionNode node : sort) {
      allFuncs.put(node.getFunction(), node);
      node.setChildren();
    }
    
    GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting().serializeNulls().excludeFieldsWithoutExposeAnnotation();
    Gson gson = builder.create();
    
    ArrayList<String> list = new ArrayList<String>();
    for (String func : allFuncs.keySet()) {
      String fileFunc;
      if (func.equals("/")) {
        fileFunc = "DIV";
      } else if (func.equals("*")) {
        fileFunc = "MUL";
      } else if (func.equals(" ")) {
        fileFunc = "SPACE";
      } else if (func.equals("()")) {
        fileFunc = "PAREN";
      } else if (func.equals("=")) {
        fileFunc = "EQUALS";
      } else if (func.equals("&")) {
        fileFunc = "AMP";
      } else {
        fileFunc = func;
      }
      
      BufferedWriter write;
      try {
        write = new BufferedWriter(new FileWriter(outputDirectory + "j" + fileFunc + ".json"));
        write.write(gson.toJson(allFuncs.get(func)));
        write.flush();
        write.close();
      } catch (IOException ex) {
        System.err.println("Unable to print JSON for " + func);
      }
      
      try {
        write = new BufferedWriter(new FileWriter(outputDirectory + "ex" + fileFunc + ".json"));
        write.write(gson.toJson(examples.get(func)));
        write.flush();
        write.close();
      } catch (IOException ex) {
        System.err.println("Unable to print example JSON for " + func);
      }
      
      list.add(fileFunc);
    }
    
    Collections.sort(list);
    
    try {
      BufferedWriter write = new BufferedWriter(new FileWriter(outputDirectory + "index.json"));
      write.write(gson.toJson(list));
      write.flush();
      write.close();
    } catch (IOException ex) {
      System.err.println("Unable to print index file.");
    }
    
    System.out.println("Formulae added: " + totalFormulae);
  }
}