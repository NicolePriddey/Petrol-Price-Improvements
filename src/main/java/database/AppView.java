package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

//Get the station info and price from the database where the stations are in specific range from the user. 
public class AppView {
	//Database variables 
	private static String hostName = "petrol-price-comparison-db.cue3oifjeliu.ap-southeast-2.rds.amazonaws.com";
	private static String port = "3306";
	private static String databaseName  = "PPC";
	private static String usr = "admin";
	private static String pwd = "AdminPassword";
	private static String url = "jdbc:mysql://" + hostName + ":" + port + "/" + databaseName;
	private static Connection connect = null;
	public static Statement stmt = null; 
	
	//AppView Object variables
	String stationID;
	String brandName;
	double lat;
	double lng;
	Timestamp lastUpdate;
	String stationName;
	String address;
	String fuelType;
	double price;
	
	static ArrayList<AppView> stationArray;
	
	//Used to build JSON from an arrayList
	static Gson gsonBuilder = new GsonBuilder().create();
	
	public AppView(String stationID, String brandName, double lat, double lng, Timestamp lastUpdate, String stationName,
			String address, String fuelType, double price) {
		super();
		this.stationID = stationID;
		this.brandName = brandName;
		this.lat = lat;
		this.lng = lng;
		this.lastUpdate = lastUpdate;
		this.stationName = stationName;
		this.address = address;
		this.fuelType = fuelType;
		this.price = price;
	}

	//create the connection to the database 
	public static void databaseConnect() {
		System.out.println("Connecting...");
		try {
			connect = DriverManager.getConnection(url, usr, pwd);
		} catch (SQLException e) {
			System.out.println("Error connecting to database: " + e.toString());
			e.printStackTrace();
		}
	}
	
	/* Get entries for a specific fuel type within a bounding box based on the distance from the user
	 * Input: String fuelType - the type of fuel the user wants to see entries for [91, 95, 98, Diesel]
	 * 			int radius - value in km to dictate how far from the user the stations are 
	 * 			double lat - latitude coordinate for the user
	 * 			double lng - longitude coordinate for the user
	 * return: json array of station objects that contain [stationID, Brand, lat, lng , lastUpdated, Station, address, fuel, price] for each 
	 */
	public static String viewDatabase( String fuelType, int radius, double uLat, double uLng) {
		stationArray = new ArrayList<>();
		ArrayList<Double> latLngRange = new ArrayList<Double>(); 
		double minLat;
		double maxLat; 
		double minLng;
		double maxLng;
		
		latLngRange = getBoundingBox(radius, uLat,uLng);
		
		minLat = latLngRange.get(0);
		maxLat = latLngRange.get(1);
		minLng = latLngRange.get(2);
		maxLng = latLngRange.get(3);
		databaseConnect();
		
		try {
			stmt = connect.createStatement();
			ResultSet rs =  stmt.executeQuery("SELECT * FROM AppView WHERE Fuel = '" + fuelType + "' AND stationID IN(SELECT stationID FROM Station WHERE lat BETWEEN " + minLat + " AND " + maxLat + " AND lng BETWEEN " + minLng + " AND " + maxLng + ")  ORDER BY price;");
			
			while (rs.next()) {
				AppView obj = new AppView(rs.getString("stationID"), rs.getString("Brand"), rs.getDouble("lat"), rs.getDouble("lng"), rs.getTimestamp("lastUpdated") , rs.getString("Station"), rs.getString("address"), rs.getString("fuel"), rs.getDouble("price"));
				stationArray.add(obj);
				
			}
		} catch (SQLException e) {
			System.out.println("Error executing the query: " + e.toString());
		} 
		
		
		try {
			connect.close();
		} catch (SQLException e) {
			System.out.println("Error closing the connection: " + e.toString());
		}
		
		return gsonBuilder.toJson(stationArray);
	}

	/* get the max and min lat and long coordinates for the bounding box of a given radius
	 * Input: int uRadius - the distance from the user in km for the size of the bounding box
	 * 			double lat - latitude coordinate of the user
	 * 			double lng - longitude coordinate of the user
	 * return: an array with 4 elements that are the min and max lat and long of the bounding box
	 */
	private static ArrayList<Double> getBoundingBox(int uRadius, double lat, double lng) {
		ArrayList<Double> latLngRange = new ArrayList<Double>();  
		double earthRadius = 6371;
		
		double latDiff = Math.toDegrees(uRadius/earthRadius);
		double lngDiff = Math.toDegrees(Math.asin(uRadius/earthRadius) / Math.cos(Math.toDegrees(lat)));
		
		double minLat = lat - latDiff;
		double maxLat = lat + latDiff;
		double minLng = lng - lngDiff;
		double maxLng = lng + lngDiff;
		
		latLngRange.add(minLat);
		latLngRange.add(maxLat);
		latLngRange.add(minLng);
		latLngRange.add(maxLng);
		
		return latLngRange;
	}
}
