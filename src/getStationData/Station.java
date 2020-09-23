package getStationData;

/* Station object 
 * String placeId - the id user to identify the station from Google API
 * String name - name of the station from Google API
 * String address - address of the station from Google API
 * double lat - latitude coordinate of the station from Google API
 * double lng - longatitude coordinate of the station from Google API
 * int brandID - the id of the brand as corresponds to the database
 */
public class Station {
	public String placeId;
	public String name;
	public String address;
	public double lat;
	public double lng;
	public int brandID;
	
	
	public Station() { }

	public Station(String placeId, String name, String address) {
		super();
		this.placeId = placeId;
		this.name = name;
		this.address = address;
	}
}
