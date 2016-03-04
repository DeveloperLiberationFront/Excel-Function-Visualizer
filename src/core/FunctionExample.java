package core;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FunctionExample {
  private String function = "",
                 file     = "";
  private int    sheet    = 0,
                 row      = 0,
                 col      = 0;
  
  /**
   * Expects a row from the database itself. Will not iterate.
   * @param row   ID -> Function -> SRC -> File -> Sheet -> Row -> Col
   */
  public FunctionExample(ResultSet row) {
    try {
      this.function = row.getString(2);
      this.file = row.getString(4);
      this.sheet = row.getInt(5);
      this.row = row.getInt(6);
      this.col = row.getInt(7);
    } catch (SQLException e) {
      System.err.println("Error reading example from result set.");
    }
  }
}
