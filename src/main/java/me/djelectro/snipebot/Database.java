package me.djelectro.snipebot;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import kotlin.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("SqlSourceToSinkFlow")
/*
Yoinked from Raisable
*/
public class Database {
  private final DataSource cpds;

  private static final Logger logger = LoggerFactory.getLogger(Database.class);

  private static Database instance;

  public static void initialize(Database db){
      if(instance != null) {
          logger.error("Tried to initialize a new database when one is already set!");
          return;
      }
      instance = db;

  }

  public static Database getInstance(){return instance;}

  public Database(String host, String port, String db, String user, String pass, boolean debugMode){

    ComboPooledDataSource cpds = new ComboPooledDataSource();
    String dbURL = String.format("jdbc:mysql://%s:%s/%s", host, port, db);

    cpds.setInitialPoolSize(10);
    cpds.setIdleConnectionTestPeriod(200);
    cpds.setMaxPoolSize(30);
    if(debugMode){
      logger.info("Database debug mode enabled. Unreturned connections will time out after 300 seconds and generate a stack trace.");
      cpds.setUnreturnedConnectionTimeout(300);
      cpds.setDebugUnreturnedConnectionStackTraces(true);
      cpds.setTestConnectionOnCheckout(true);
    }
    else{
      cpds.setTestConnectionOnCheckout(false);
    }
    cpds.setTestConnectionOnCheckin(true);

    cpds.setJdbcUrl( dbURL );
    cpds.setUser(user);
    cpds.setPassword(pass);
    this.cpds = cpds;
    logger.info("Database successfully connected.");
  }

  public Database(DataSource dataSource){
    cpds = dataSource;
  }


  public Pair<Object, Connection> executeStatement(String stmt, Object... args) {
        try {
            PreparedStatement pstmt;
            Connection dbConn = cpds.getConnection();
            pstmt = dbConn.prepareStatement(stmt);

            if(args != null){
                int argCounter = 1;
                for(Object arg : args){
                    if(arg instanceof String)
                        pstmt.setString(argCounter, (String) arg);
                    else if(arg instanceof Integer)
                        pstmt.setInt(argCounter, (Integer) arg);
                    argCounter++;
                }
            }

            Object r;
            if(stmt.startsWith("SELECT"))
                r = pstmt.executeQuery();
            else if(stmt.startsWith("UPDATE") || stmt.startsWith("INSERT"))
                r = pstmt.executeUpdate();
            else
                r = pstmt.execute();

            return new Pair<>(r, dbConn);
        } catch (SQLException throwables) {
            logger.error(throwables.getMessage());
            return null;
        }

    }

  public boolean executeUpdate(String stmt) {
      return executeUpdate(stmt, (Object) null);
    }

  public boolean executeUpdate(String stmt, Object... args) {
    Pair<Object, Connection> a = executeStatement(stmt, args);
    try(Connection ignored = a.component2()){
        return a.component1() != null;
    } catch (SQLException e) {
        logger.error(e.getMessage());
        return false;
    }
  }

  public Map<Integer, String[]> executeAndReturnData(String stmt, Object... args){
        Pair<Object, Connection> conPair = executeStatement(stmt, args);
        try (Connection ignored = conPair.component2()){
          //logger.info("Number of busy connections: " + getNumBusyConnections());
          ResultSet res = (ResultSet) conPair.component1();
          return getData(res, conPair.component2());
        }catch(Exception e){
          logger.error(e.getMessage());
          return null;
        }
    }

  public Map<Integer, String[]> executeAndReturnData(String stmt){
      return executeAndReturnData(stmt, (Object) null);
    }

  public static String getData(String column, int loc, ResultSet data, Connection a){
        try (a) {
            data.absolute(loc);
            String res = data.getString(column);
            a.close();
            return res;
        } catch (SQLException throwables) {
            logger.error(throwables.getMessage());

        }
        return null;
    }

  public static Map<Integer, String[]> getData(ResultSet data, Connection a){
        Map<Integer, String[]> result = new HashMap<>();
        try (a) {
            int columnCount = data.getMetaData().getColumnCount();
            int rowCounter = 1;
            while (data.next()) {
                ArrayList<String> thisResult = new ArrayList<>();
                for (int i = 1; i < columnCount + 1; i++) {
                    thisResult.add(data.getString(i));
                }
                result.put(rowCounter, thisResult.toArray(new String[]{}));
                rowCounter++;
            }
        } catch (SQLException throwables) {
            logger.error(throwables.getMessage());
        }


        return result;

    }

    //public int getNumBusyConnections() throws SQLException {return ((ComboPooledDataSource) cpds).getNumBusyConnectionsAllUsers();}
}
