package scripts.filesAndDB;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class plusReplace {
  private static final Matcher NULL = Pattern.compile("null").matcher("");

  public static void main(String[] args) {
    String test = "OFFSET(Delpoint,3,null)",
           replace = "B3+2";
    
    NULL.reset(test);
    System.out.println(NULL.replaceFirst(replace));
  }
}