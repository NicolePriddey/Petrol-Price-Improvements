package PPC.Backend;

import java.sql.SQLException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import database.AppView;
import database.UpdatePrice;
import worthIt.WorthIt;

//Controller for the api calls
@RestController
public class Controller {
	
	//Hello world test
	@GetMapping(path = "/")
	public String helloWorld() {
		return "Hello World";
	}
	
	/* Get entries for a specific fuel type within a given radius around a given location
	 * Input: String fuelType - the type of fuel the user wants to see entries for [91, 95, 98, Diesel]
	 * 			int radius - value in km to dictate how far from the user the stations are 
	 * 			double lat - latitude coordinate of the user
	 * 			double lng - longitude coordinate of the user
	 * return: json array of station objects that contain [stationID, Brand, lat, lng , lastUpdated, Station, address, fuel, price] for each 
	 */
	@GetMapping(path = "/view/{fuelType}/{radius}/{lat}/{lng}")
	public String DatabaseViewFuelType(@PathVariable String fuelType, @PathVariable int radius, @PathVariable double lat, @PathVariable double lng ) {
		return AppView.viewDatabase(fuelType,radius,lat,lng);
	}
	
	/* Add price to database
	 * Input: String fuelType - the type of fuel the user is updating an entry for [91, 95, 98, Diesel]
	 * 			String stationId - the id for the station the user wants to update
	 * 			double price - the new price
	 * return: boolean - true for successful update, false for unsuccessful. 
	 */
	@GetMapping(path = "/update/{fuelType}/{stationID}/{price}")
	public boolean DatabaseUpdate(@PathVariable String fuelType, @PathVariable String stationID, @PathVariable double price) {
		return UpdatePrice.updatePrice(fuelType, stationID, price);
	}
	
	/* Get Worth It information from the database
	 * Input: double lat - latitude coordinate of the user
	 * 			double lng - longitude coordinate of the user
	 *  		String type - the type of fuel the user wants to see Worth It for [91, 95, 98, Diesel]
	 * 			int fuelEco - the id based off their vehicle class and type to get fuel economy value [1-9]
	 * 			int litres - the number of liters the user wants the fill amount to be.
	 * return: json array with two objects [ stationName, cost, timeS ] the first is the cheapest station and second is the closest
	 */
	@GetMapping(path = "/worthit/{lat}/{lng}/{type}/{fuelEco}/{litres}")
	public String ViewWorthIt(@PathVariable double lat, @PathVariable double lng , @PathVariable String type, @PathVariable int fuelEco, @PathVariable int litres) throws SQLException {
		return WorthIt.getWorthIt(lat, lng, type, fuelEco, litres);
	}
}
