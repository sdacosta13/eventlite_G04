package uk.ac.man.cs.eventlite.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import retrofit2.Response;
import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@ActiveProfiles("test")
public class VenueTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Test
	public void testMapBoxGeocodingCall() throws Exception {
		Venue myVenue = new Venue();
		myVenue.setName("New venue");
		myVenue.setAddress("Oxford Rd, Manchester");
		myVenue.setPostcode("M13 9PL");
		myVenue.setCapacity(100);
		
		// Must create object and not a mock because
		// we are testing setCoords method.
		
		assertTrue(myVenue.setCoords());
		
		
		// Check that the coordinates were set correctly
		MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
				.accessToken("pk.eyJ1Ijoic2RhY29zdGExMyIsImEiOiJja21wMm45bzIyYWhkMnBwZnQ1Yzg0Zm8xIn0.mbzmSCCHSGvuxW3_DCxJYg")
				.query(myVenue.getAddress() + " " + myVenue.getPostcode()).build();
		assertNotNull(mapboxGeocoding);
		
		Response<GeocodingResponse> response = mapboxGeocoding.executeCall();
		assertEquals(200, response.code());
		
		Point resultPoint = response.body().features().get(0).center();
		assertTrue(resultPoint.latitude() == myVenue.getLatitude());
		assertTrue(resultPoint.longitude() == myVenue.getLongitude());
	}

}
