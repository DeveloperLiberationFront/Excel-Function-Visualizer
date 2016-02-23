package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtils {
  //TODO: Return null for one exception but throws the other? Seems inconsistent.
  public static Connection connectToDatabase() throws SQLException {   
    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      System.err.println("connectToDatabase: No database driver found.");
      return null;
    }

    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/spreadsheet_funcs", 
        "root", System.getenv("MYSQL_PASSWORD"));
    return con;
  }
}