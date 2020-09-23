package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//This class will update the price in the database for a specific station and fuel type
public class UpdatePrice {
	private static String hostName = "petrol-price-comparison-db.cue3oifjeliu.ap-southeast-2.rds.amazonaws.com";
	private static String port = "3306";
	private static String databaseName  = "PPC";
	private static String usr = "admin";
	private static String pwd = "AdminPassword";
	private static String url = "jdbc:mysql://" + hostName + ":" + port + "/" + databaseName;
	private static Connection connect = null;
	public static Statement stmt = null; 
	
	
	//create the connection to the database 
	public static void databaseConnect() {
		System.out.println("Connecting...");
		try {
			connect = DriverManager.getConnection(url, usr, pwd);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/* Insert price into database if there is already one there for that fuel type and station then update the price
	 * Input: String fuelType - the type of fuel the user is updating an entry for [91, 95, 98, Diesel]
	 * 			String stationId - the id for the station the user wants to update
	 * 			double price - the new price
	 * return: boolean - true for successful update, false for unsuccessful. 
	 */
	public static boolean updatePrice(String fuelType, String stationID, double price){
		int fuelID = 0;
		boolean success = false;
		databaseConnect();
		
	
		try {
			stmt = connect.createStatement();
			ResultSet rs =  stmt.executeQuery("SELECT typeID FROM FuelType WHERE name='" + fuelType + "';");
			
			while (rs.next()) {
				fuelID = rs.getInt("typeID");
			}
			
			stmt.executeUpdate("INSERT INTO FuelEntry VALUE (" + fuelID + ", '" + stationID + "', " + price + ", CURRENT_TIMESTAMP ) ON DUPLICATE KEY UPDATE price='" + price + "', lastUpdated=CURRENT_TIMESTAMP;");
			
			success = true;
			
		} catch (SQLException e) {
			if (e.toString().contains("a foreign key constraint fails")) System.out.println("Error: Station id or fuel type incorrect");
			else System.out.println("Error executing query: " + e.toString());
		} 
		
		try {
			connect.close();
		} catch (SQLException e) {
			System.out.println("Error closing connection: " + e.toString());
		}
		
		return success;
	}
}