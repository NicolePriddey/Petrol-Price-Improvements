package worthIt;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static java.time.temporal.ChronoUnit.SECONDS;


// Worth It will show the user the closest station to them and the station with the cheapest total cost
// The total cost consists of the the cost of fuel over the distance to travel to and from the station, and the cost to fill a tank
// of a certain number of liters 
public class WorthIt {
	
	private static String googleApiKey = "AIzaSyDDnwKyNq--sYKl3ZuO5jRlBUbZLsJzmaE";	
	private static String endpoint = "petrol-price-comparison-db.cue3oifjeliu.ap-southeast-2.rds.amazonaws.com";
	private static String port = "3306";
	private static String databaseName  = "PPC";
	private static String usr = "admin";
	private static String pwd = "AdminPassword";
	private static String url = "jdbc:mysql://" + endpoint + ":" + port + "/" + databaseName + "?autoReconnect=true&useSSL=false";
	private static Connection connect = null;
	private static Statement stmt = null; 
	
	
	private static double lat = 0;
	private static double lng = 0;
	private static int fuelTypeId = 0;  
	
	public String stationName;
	public double cost;
	public int timeS;
	
	public static LocalTime start;
	
		
	public WorthIt(String stationName, double cost, int timeS) {
		super();
		this.stationName = stationName;
		this.cost = cost;
		this.timeS = timeS;
	}
	
	/* Get Worth It information from the database
	 * Input: double lat - latitude coordinate of the user
	 * 			double lng - longitude coordinate of the user
	 *  		String type - the type of fuel the user wants to see Worth It for [91, 95, 98, Diesel]
	 * 			int fuelEco - the id based off their vehicle class and type to get fuel economy value [1-9]
	 * 			int litres - the number of liters the user wants the fill amount to be.
	 * return: json array with two objects [ stationName, cost, timeS ] the first is the cheapest station and second is the closest
	 */
	public static String getWorthIt(double uLat, double uLng, String ufuelType, int fuelEconomy, int uLiters) throws SQLException {
		//See how long it takes to run 
		start = java.time.LocalTime.now();
		
		double literPerKM;
		String cheapestStationId;
		String closestStationId;
		ArrayList<WorthIt> worthItStations = new ArrayList<WorthIt>();
		Gson gsonBuilder = new GsonBuilder().create();
		
		databaseConnect();
		
		lat = uLat;
		lng = uLng;
		getFuelTypeId(ufuelType);  
		literPerKM = getFuelEconomyValueInLPerKM(fuelEconomy);

		cheapestStationId = getlowestTotalCost(getStationList(20), literPerKM, uLiters);
		closestStationId = getStationClosestToUser();
		
		WorthIt obj1 = new WorthIt(getStationName(cheapestStationId), (Math.round(getTotalCost(cheapestStationId, literPerKM, uLiters ) * 100.0) / 100.0), getTimeTripToStation(cheapestStationId));
		worthItStations.add(obj1);
		
		WorthIt obj2 = new WorthIt(getStationName(closestStationId), (Math.round(getTotalCost(closestStationId, literPerKM, uLiters) * 100.0) / 100.0), getTimeTripToStation(closestStationId));
		worthItStations.add(obj2);
		
		connect.close();

		System.out.println(SECONDS.between(start, java.time.LocalTime.now()) + " END " + java.time.LocalTime.now());  
		return gsonBuilder.toJson(worthItStations);
	}
	
	
	/* get the travel cost + fill cost for a station
	 * Input: String station - the id of the station to get the total cost for 
	 * 			double literPerKM - the fuel economy value in L/km
	 * 			int liters - the number of liters to base the fill cost calculation on
	 * return: the travel cost + fill cost for the station
	 */
	public static double getTotalCost(String station, double literPerKM, int Litres ) throws SQLException {
		double litersToStation;
		double price;
		double costTripToStation;
		double costForL;
		
		litersToStation = literPerKM * (getDistanceTripToStation(station) / 1000);
		price = getPriceFromDB(station);
		costTripToStation = price * litersToStation;
		costForL = price * Litres;
		
		return costTripToStation + costForL;
	}
	
	/* get the name of the station for the station id
	 * Input: String station - the station id to get the name for 
	 * return: the station name 
	 */
	public static String getStationName(String station) throws SQLException {
		String stationName = null;			
		
		ResultSet rs = stmt.executeQuery("SELECT name FROM Station WHERE stationID='" + station + "';");
		while (rs.next()){
		stationName = rs.getString("name");
		}

		return stationName;
	}
	
	/* get the id of the station with the lowest travel + fill cost
	 * Input:  ArrayList<String>  stationList - the list of station id's in a 20km radius
	 * 			double literPerKM - the fuel economy value in L/km
	 * 			int liters - the number of liters to base the fill cost calculation on
	 * return: the id of the station with the lowest travel + fill cost 
	 */
	public static String getlowestTotalCost(ArrayList<String>  stationList, double literPerKM, int liters) throws SQLException {
		double lowestCost = 100;
		String lowStationID = null;
		
		for(String s : stationList) {
			double p = getTotalCost(s, literPerKM, liters);
			if ( p !=0 & p < lowestCost) {
				lowestCost = p;
				lowStationID = s;
			}
		}
		return lowStationID;		
	}	
	
	/* get the id of the station closest to the user
	 * return: the id of the station closest to the user
	 */
	public static String getStationClosestToUser() throws SQLException {
		//use an 8km radius to cut down on the number of stations to check
		ArrayList<String> stationList = getStationList(8);
		double smallestDistance = 8000;
		String smallStationID = null;
		
		for (String s : stationList) {
			int distance = getDistanceTripToStation(s);
			if ( distance < smallestDistance ) {
				smallestDistance = distance;
				smallStationID = s;
			}
		}
		return smallStationID;
	}
	
	/* use the google maps directions api to get the return trip distance for a station
	 * Input: String placeId - the id of the station to get the distance to
	 * return: distance in km for the return trip according to google at that point in time
	 */
	public static int getDistanceTripToStation(String placeId){
		String placesApiBase = "https://maps.googleapis.com/maps/api/directions/json?";
		String apiString = placesApiBase + "origin=" + lat + "," + lng + "&destination=place_id:" + placeId + "&key=" + googleApiKey;
		HttpURLConnection conn = null;
		String urlString = apiString;
		StringBuilder jsonResults = new StringBuilder();
		int distanceInM = 0;
	
		try {
			URL url = new URL(urlString);
			conn = (HttpURLConnection) url.openConnection();
			InputStreamReader in = new InputStreamReader(conn.getInputStream());
			int read;
			char[] buff = new char[1024];
			while((read = in.read(buff)) != -1) {
				jsonResults.append(buff, 0, read);
			}			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			JSONObject jsonObj = new JSONObject(jsonResults.toString());
			JSONObject jsonArray = jsonObj.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0);
			distanceInM = (int) jsonArray.getJSONObject("distance").get("value");
						
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return distanceInM * 2;
	}
	
	/* use the google maps directions api to get the return trip travel time for a station
	 * Input: String placeId - the id of the station to get the time to
	 * return: time in seconds for the return trip according to google at that point in time
	 */
	public static int getTimeTripToStation(String placeId){
		String placesApiBase = "https://maps.googleapis.com/maps/api/directions/json?";
		String apiString = placesApiBase + "origin=" + lat + "," + lng + "&destination=place_id:" + placeId + "&key=" + googleApiKey;
		HttpURLConnection conn = null;
		String urlString = apiString;
		StringBuilder jsonResults = new StringBuilder();
		int timeInSec = 0;
	
		try {
			URL url = new URL(urlString);
			conn = (HttpURLConnection) url.openConnection();
			InputStreamReader in = new InputStreamReader(conn.getInputStream());
			int read;
			char[] buff = new char[1024];
			while((read = in.read(buff)) != -1) {
				jsonResults.append(buff, 0, read);
			}			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (conn != null ) conn.disconnect();
		}
		
		try {
			JSONObject jsonObj = new JSONObject(jsonResults.toString());
			JSONObject jsonArray = jsonObj.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0);
			timeInSec = (int) jsonArray.getJSONObject("duration").get("value");
						
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return timeInSec * 2;
	}
		
	/* get the max and min lat and long coordinates for the bounding box of a given radius
	 * Input: double uRadius - the distance from the user in km for the size of the bounding box
	 * return: an array with 4 elements that are the min and max lat and long of the bounding box
	 */
	public static ArrayList<Double> getBoundingBox(double uRadius) {
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
	/* get the list of stations within a specified distance from the user
	 * Input: double radius - radius of the range the stations are in
	 * return: an array containing all the ID's of stations in that radius
	 */
	public static ArrayList<String> getStationList(double radius) throws SQLException {
		ArrayList<String> stationsIdArray = new ArrayList<String>();
		ArrayList<Double> latLngRange = new ArrayList<Double>(); 
		double minLat;
		double maxLat; 
		double minLng;
		double maxLng;
		
		latLngRange = getBoundingBox(radius);
		//Prevent the min and max getting mixed up
		if (Double.compare(latLngRange.get(0), latLngRange.get(1)) < 0) {
			minLat = latLngRange.get(0);
			maxLat = latLngRange.get(1);
		} else {
			minLat = latLngRange.get(1);
			maxLat = latLngRange.get(0);
		}
		
		if (Double.compare(latLngRange.get(2), latLngRange.get(3)) < 0) {
			minLng = latLngRange.get(2);
			maxLng = latLngRange.get(3);
		} else {
			minLng = latLngRange.get(3);
			maxLng = latLngRange.get(2);
		}

		
		ResultSet rs = stmt.executeQuery("SELECT stationID FROM Station WHERE lat BETWEEN " + minLat + " AND " + maxLat + " AND lng BETWEEN " + minLng + " AND " + maxLng + " AND stationID IN ( SELECT stationID FROM FuelEntry WHERE fuelTypeID=" + fuelTypeId + " );");
		while(rs.next()) {
			stationsIdArray.add(rs.getString("stationID"));
		}
		
		return stationsIdArray;
	}
	
	/* Get the price from the database 
	 * Input: String station - the id of the station to get the price for
	 * return: the price from the database
	 */
	public static double getPriceFromDB(String station) throws SQLException {
		double price = 0;
		
		ResultSet rs =  stmt.executeQuery("SELECT price FROM FuelEntry WHERE stationID='" + station + "' AND fuelTypeID=" + fuelTypeId + ";");
		while (rs.next()) {
			price = rs.getDouble("price");	
		}
		
		return price;		
	}
	
	/* Get the type id based on the fuel type name
	 * Input: String fuelType - the type of fuel [91, 95, 98, Diesel]
	 * return: the id corresponding to the fuel type [1 - 4 ]
	 */
	public static void getFuelTypeId(String fuelType) throws SQLException {
		ResultSet rs =  stmt.executeQuery("SELECT typeID FROM FuelType WHERE name='" + fuelType + "';");
		while (rs.next()) {
			fuelTypeId = rs.getInt("typeID");
		}
	}
	
	/*get the fuel economy value in L/km based on the fuel economy id 
	 * Input: int ufuelEcoId - the id based on the vehicle type and class [1-9] 
	 * return: double value that is the L/km for the fuel economy of that class type.
	 */
	public static double getFuelEconomyValueInLPerKM( int ufuelEcoId ) throws SQLException {
		double value= 0;
		
		ResultSet rs =  stmt.executeQuery("SELECT value FROM FuelEconomy WHERE fuelEconomyID = '" + ufuelEcoId + "';");
		while (rs.next()) {
			value = rs.getDouble("value");	
		}
		
		return value/100;		
	}

	//create the connection to the database 
	public static void databaseConnect() throws SQLException {
		System.out.println("Connecting...");
		connect = DriverManager.getConnection(url, usr, pwd);
		stmt = connect.createStatement();

	}
}
