package edu.wpi.teamb.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class LocationName {

  public static final String tableName = "locationname";
  private String longName;
  private String shortName;
  private String locationType;

  public LocationName(String longName, String shortName, String locationType) {
    this.longName = longName;
    this.shortName = shortName;
    this.locationType = locationType;
  }

  public static void initTable() throws SQLException {
    String sql =
        String.join(
            ", ",
            "CREATE TABLE locationname",
            "(longName VARCHAR(70),",
            "shortName VARCHAR(40),",
            "locationType CHAR(4),",
            "PRIMARY KEY (longName));");
    Bdb.processUpdate(sql);
  }

  public static String getTableName() {
    return tableName.toLowerCase();
  }

  public String getLongName() {
    return longName;
  }

  public String getShortName() {
    return shortName;
  }

  public String getLocationType() {
    return locationType;
  }

  public void setLongName(String longName) {
    this.longName = longName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public void setLocationType(String locationType) {
    this.locationType = locationType;
  }

  public void insert() throws SQLException {
    String sql = "INSERT INTO locationname (longname, shortname, locationtype)" + "VALUES (?,?,?);";
    PreparedStatement ps = Bdb.prepareKeyGeneratingStatement(sql);
    ps.setString(1, longName);
    ps.setString(2, shortName);
    ps.setString(3, locationType);

    ps.executeUpdate();
  }

  public static Map<String, LocationName> getAll() throws SQLException {
    HashMap<String, LocationName> locations = new HashMap<String, LocationName>();
    String sql = "SELECT * FROM locationName;";
    ResultSet rs = Bdb.processQuery(sql);
    while (rs.next()) {
      locations.put(
          rs.getString("longname"),
          new LocationName(
              rs.getString("longname"), rs.getString("shortname"), rs.getString("locationtype")));
    }
    return locations;
  }

  public void updateLN(String newName) throws SQLException {
    String old = longName;
    longName = newName;
    String sql =
        "UPDATE LocationName SET longName = '" + longName + "' WHERE longName = '" + old + "';";
    Bdb.processUpdate(sql);
  }

  public void update() throws SQLException {
    String sql =
        "UPDATE locationname "
            + "SET longName = ?, shortName = ?, locationType = ? "
            + "WHERE longName = ?;";

    PreparedStatement ps = Bdb.prepareStatement(sql);
    ps.setString(1, longName);
    ps.setString(2, shortName);
    ps.setString(3, locationType);
    ps.setString(4, longName);
    ps.executeUpdate();
  }

  public void delete() throws SQLException {
    String sql = "DELETE FROM locationname WHERE longName = ?;";
    PreparedStatement ps = Bdb.prepareStatement(sql);
    ps.setString(1, longName);
    ps.executeUpdate();
  }
}
