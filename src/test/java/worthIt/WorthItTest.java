//package worthIt;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import java.sql.Connection;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.Random;
//
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//
//class WorthItTest {
//
//	//assume that database connection is there and working so no need to test methods solely 
//	
//	@InjectMocks
//	private WorthIt service;
//	
//	@Mock
//	private Connection connect;
//	
//	@Mock
//	private Statement stmt; 
//	
//	@Mock
//	ResultSet rs;
//	
//	
//	@Test
//	void testGetWorthIt() {
//		
//	}
//
//	@Test
//	void testGetTotalCost() {
//		Mockito.when(WorthIt.getDistanceToStation("")).thenReturn(5600);
//		Mockito.when(service.getPriceFromDB("ChIJV6FltMmtOG0RCHzPA7TnzG4")).thenReturn(2.12);
//		assert(service.getTotalCost("ChIJV6FltMmtOG0RCHzPA7TnzG4", 0.0675) == 24.0408);
//	}
//
//
//	@Test
//	void testGetLowestPrice() {
//		Mockito.when(service.getPriceFromDB("ChIJV6FltMmtOG0RCHzPA7TnzG4")).thenReturn(2.12);
//		
//		ArrayList<String> s = new ArrayList<>();
//		s.add("ChIJV6FltMmtOG0RCHzPA7TnzG4");
//		
//		assert(service.getLowestPrice(s).equals("ChIJV6FltMmtOG0RCHzPA7TnzG4"));
//			
//	}
//
//	@Test
//	void testGetStationClosestToUser() {
//		Mockito.when(service.getDistanceToStation("ChIJV6FltMmtOG0RCHzPA7TnzG4")).thenReturn(1600);
//		
//		ArrayList<String> s = new ArrayList<>();
//		s.add("ChIJV6FltMmtOG0RCHzPA7TnzG4");
//		
//		assert(service.getStationClosestToUser(s).equals("ChIJV6FltMmtOG0RCHzPA7TnzG4"));
//	}
//
//	@Test
//	void testGetDistanceToStation() {
//		
//	}
//
//	@Test
//	void testGetTimeToStation() {
//		
//	}
//
//	@Test
//	void testGetStationsInRadiusWithPrices() {
//		
//	}
//
//	@Test
//	void testGetFuelEconomyValueInLPerKM() throws SQLException {
//		Mockito.when(connect.createStatement()).thenReturn(stmt);
//		Mockito.when(stmt.executeQuery(Mockito.anyString())).thenReturn(rs); 
//		assertEquals(service.getFuelEconomyValueInLPerKM(1), 0.0675);
//	}
//
//	@Test
//	void testHaversine() {
//		
//	}
//
//	@Test
//	void testGetStationlocation() {
//		
//	}
//}
