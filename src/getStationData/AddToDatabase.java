package getStationData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


/* Add the station object to the database 
 * TODO: Add logic for if there are two entries for the same station
 */
public class AddToDatabase {
	private static String hostName = "petrol-price-comparison-db.cue3oifjeliu.ap-southeast-2.rds.amazonaws.com";
	private static String port = "3306";
	private static String databaseName  = "PPC";
	private static String usr = "admin";
	private static String pwd = "AdminPassword";
	private static String url = "jdbc:mysql://" + hostName + ":" + port + "/" + databaseName;
	private static Connection connect = null;
	public static Statement stmt = null; 
	//Station with quotes in the name
	public static Integer pakNSave = 7;
	static Map<Integer, String> brands = new HashMap<>();
	
	//Creates a connection to the database
	public static void databaseConnect() {
		System.out.println("Connecting...");
		try {
			connect = DriverManager.getConnection(url, usr, pwd);
		} catch (SQLException e) {
			System.out.println("Error connecting to database: " + e.toString());
			e.printStackTrace();
		}
	}
	
	/*Add the station objects to the database
	 * Input: ArrayList<Station> stations - an array list of station object taken from Google Places API 
	 */
	public static void addStation(ArrayList<Station> stations) {
		databaseConnect();
		getBrands();
		
		
		//insert each station object into database
		for ( Station station : stations ) {
			Integer brandId = getBrandId(station.name);
			
			if(brandId != null){
				//if the brand is pak'nSave escape the single quote in the name so the sql insert will work 
				if(brandId.equals(pakNSave)) station.name = station.name.replace("'", "''");
				
				try {
					stmt = connect.createStatement();
					stmt.executeUpdate("INSERT INTO PPC.Station VALUES ('" + station.placeId + "', '" + station.name + "', '" + brandId + "', '" + station.address + "', '" + station.lat + "', '" + station.lng + "') " 
					+ "ON DUPLICATE KEY UPDATE name='" + station.name + "', brandId='" + brandId + "', address='" + station.address + "', lat='" + station.lat + "', lng='" + station.lng + "';");
				} catch (SQLException e) {
					System.out.println("Error inserting into database: " + e.toString());
				}
			}
			else System.out.println("Brand is not in database for: " + station.name + " address: " + station.name);
		}
		
		
		try {
			connect.close();
			System.out.println("Connection closed");
		} catch (SQLException e) {
			System.out.println("Error closing connection: " + e.toString());
		}	
	}	
	
	/*finds the brands in the database in the station name 
	 * Input: the station name
	 */
	public static Integer getBrandId(String s ){
		//match the station name with the brand id from the database
		Integer brandId = null;
		for ( Entry<Integer, String> brand : brands.entrySet()) {
			if ( s.toLowerCase().contains(brand.getValue()) ) {
				brandId = brand.getKey();
				//exit loop once brand is found
				break;
			};
		}
		//check for pak n save with spaces
		if (brandId == null)
			if ( s.toLowerCase().contains("pak 'n save")) brandId = 7;
		
		return brandId;
	}
	
	// get the brands from the database 
	public static void getBrands(){		
		//populate map with the brands from the database
		try {
			stmt = connect.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM Brand");
			while (rs.next()) {	
				brands.put(rs.getInt("brandID"), rs.getString("name").toLowerCase());		
			} 
		} catch (SQLException e) {
			System.out.println("Error: " + e.toString());
		}
	}
	
}
