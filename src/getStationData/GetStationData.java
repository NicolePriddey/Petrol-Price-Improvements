package getStationData;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* Query the Google places API and add the stations to the database 
 * Change the latLong variable to the latitude and longitude coordinates you want to get 
 * the stations nearby using the next page token to get up to 60 results
 * TODO: needs lat and long for each different region - can only do 60 results total
 */
public class GetStationData { 
	private static String placesApiBase = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
	private static String latLong = "-41.223040, 174.804898";
	private static String apiKey = "AIzaSyDDnwKyNq--sYKl3ZuO5jRlBUbZLsJzmaE";
	private static String apiString = placesApiBase + "location=" + latLong + "&radius=10000&type=gas_station&key=" + apiKey;

	
	static HttpURLConnection conn;
	static ArrayList<Station> resultList = new ArrayList<Station>();;
	static String nextPageToken = null;
	
	public static void main(String[] args) {
		System.out.println("Running...");
		do {
		     queryApi();
		     try {
		    	//need to sleep because the next page token from google does not work immediately. 
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				System.out.println("Error with the sleep: " + e.toString());
			}
		} while (nextPageToken != null);

		AddToDatabase.addStation(resultList);
	}
	
	// query the Google places API and add the stations to the database  
	@SuppressWarnings("static-access")
	public static void queryApi() {
		StringBuilder jsonResults = new StringBuilder();
		String urlString = apiString;
		
		if (nextPageToken != null) {
			String nextPageApiString = placesApiBase + "pagetoken=" + nextPageToken + "&key=" + apiKey; 
			urlString = nextPageApiString;
		}

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
			System.out.println("Error with the API URL: " + e);
		} catch (IOException e) {
			System.out.println("Error connection to the API: " + e);
		} finally {
			if (conn != null ) conn.disconnect();
		}
		 
		try {
			// Create a JSON object hierarchy from the results
			JSONObject jsonObj = new JSONObject(jsonResults.toString());
			JSONArray jsonArray = jsonObj.getJSONArray("results");
			if (jsonObj.has("next_page_token"))	nextPageToken = jsonObj.getString("next_page_token");
			else nextPageToken = null;
			
			// Extract the Place descriptions from the results
			for (int i = 0; i < jsonArray.length(); i++) {
				Station station = new Station();
				station.placeId = jsonArray.getJSONObject(i).getString("place_id");
				station.name = jsonArray.getJSONObject(i).getString("name");
				station.address = jsonArray.getJSONObject(i).getString("vicinity");
				station.lat = (double) jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").get("lat");
				station.lng = (double) jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").get("lng");
				resultList.add(station);
			}			
		} catch (JSONException e) {
			System.out.println("Error with JSON: " + e);
			e.printStackTrace();
		}
	}
}