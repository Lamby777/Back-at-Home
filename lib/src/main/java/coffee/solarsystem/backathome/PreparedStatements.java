package coffee.solarsystem.backathome;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PreparedStatements {
  private PreparedStatement _homesWithName;
  private PreparedStatement _homesSegment;
  private PreparedStatement _deleteHome;
  private PreparedStatement _setHome;
  private Logger logger;

  public PreparedStatements(Connection conn, Logger logger) {
    this.logger = logger;

    try {
      _homesWithName = conn.prepareStatement(
          "SELECT * FROM homes WHERE UUID = ? AND NAME = ?");

      _homesSegment = conn.prepareStatement(
          "SELECT * FROM homes WHERE UUID = ? ORDER BY id DESC LIMIT ?,?");

      _deleteHome = conn.prepareStatement(
          "DELETE FROM homes WHERE UUID = ? AND NAME = ?");

      _setHome = conn.prepareStatement(
          "INSERT INTO homes (UUID,Name,world,x,y,z,yaw,pitch,server) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Failed to init prepared", e);
    }
  }

  void setHome(String uuid, String home, HomeLocation hloc) {
    deleteHome(uuid, home);

    logger.info("Inserting user home " + uuid + " with Name:" + home);

    try {
      _setHome.setString(1, uuid);
      _setHome.setString(2, home);
      _setHome.setString(3, hloc.worldname);
      _setHome.setDouble(4, hloc.loc.getX());
      _setHome.setDouble(5, hloc.loc.getY());
      _setHome.setDouble(6, hloc.loc.getZ());
      _setHome.setFloat(7, hloc.loc.getYaw());
      _setHome.setFloat(8, hloc.loc.getPitch());
      _setHome.setString(9, hloc.servername);

      // phew, it's over
      _setHome.execute();
    } catch (SQLException ex) {
      Logger.getLogger(zPHomes.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  ResultSet homesWithName(String uuid, String home) throws SQLException {
    _homesWithName.setString(1, uuid);
    _homesWithName.setString(2, home);
    return _homesWithName.executeQuery();
  }

  ResultSet homesSegment(String uuid, int segment) throws SQLException {
    _homesSegment.setString(1, uuid);

    int start = segment * zPHomes.PAGE_LENGTH;
    _homesSegment.setInt(2, start);
    _homesSegment.setInt(3, start + zPHomes.PAGE_LENGTH);

    return _homesSegment.executeQuery();
  }

  boolean homeExists(String uuid, String home) throws SQLException {
    return homesWithName(uuid, home).next();
  }

  void deleteHome(String uuid, String home) {
    try {
      _deleteHome.setString(1, uuid);
      _deleteHome.setString(2, home);
      _deleteHome.execute();
    } catch (SQLException e) {
      Logger.getLogger(zPHomes.class.getName()).log(Level.SEVERE, null, e);
    }
  }
}
