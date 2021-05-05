package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import retrofit2.Response;
import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
//@DirtiesContext
@ActiveProfiles("test")
//@Disabled
public class VenueServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@InjectMocks
	private VenueServiceImpl venueService;
	
	@Mock
	private VenueRepository venueRepository;
	
	@Mock 
	private Venue v1 = mock(Venue.class, Mockito.RETURNS_DEEP_STUBS);
	@Mock 
	private Venue v2 = mock(Venue.class, Mockito.RETURNS_DEEP_STUBS);
	@Mock 
	private Venue v3 = mock(Venue.class, Mockito.RETURNS_DEEP_STUBS);
	@Mock 
	private Venue v4 = mock(Venue.class, Mockito.RETURNS_DEEP_STUBS);
	@Mock 
	private Venue v5 = mock(Venue.class, Mockito.RETURNS_DEEP_STUBS);
	@Mock 
	private Venue v6 = mock(Venue.class, Mockito.RETURNS_DEEP_STUBS);
	@Mock 
	private Venue v7 = mock(Venue.class, Mockito.RETURNS_DEEP_STUBS);
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(v1.getName()).thenReturn("Venue a b");
		when(v2.getName()).thenReturn("Venue b");
		when(v3.getName()).thenReturn("Venue c a");
		when(v4.getName()).thenReturn("Venue d a");
		when(v5.getName()).thenReturn("Venue e");
		when(v6.getName()).thenReturn("Venue f d a");
		when(v7.getName()).thenReturn("Venue g");
		
		when(venueRepository.findAllContainingAlternativeIgnoreCaseOrderByNameAsc(anyString(),anyString(),anyString(),anyString())).thenReturn(setupVenuesArray());
		when(venueRepository.findAllContainingAlternativeIgnoreCaseOrderByNameAsc("a %", "% a", "% a %", "a")).thenReturn(setupVenuesArrayA());
		when(venueRepository.findAllContainingAlternativeIgnoreCaseOrderByNameAsc("d %", "% d", "% d %", "d")).thenReturn(setupVenuesArrayD());
	}
	
	private Iterable<Venue> setupVenuesArrayA() {
		ArrayList<Venue> veAr = new ArrayList<Venue>();
		veAr.add(v1);
		veAr.add(v3);
		veAr.add(v4);
		veAr.add(v6);
		
		return (Iterable<Venue>) veAr;
	}
	
	private Iterable<Venue> setupVenuesArrayD() {
		ArrayList<Venue> veAr = new ArrayList<Venue>();
		veAr.add(v4);
		veAr.add(v6);
		
		return (Iterable<Venue>) veAr;
	}
	
	private Iterable<Venue> setupVenuesArray(){			
		ArrayList<Venue> veAr = new ArrayList<Venue>();
		veAr.add(v1);
		veAr.add(v2);
		veAr.add(v3);
		veAr.add(v4);
		veAr.add(v5);
		veAr.add(v6);
		veAr.add(v7);
		
		return (Iterable<Venue>) veAr;
	}
	
	@Test
	public void testSearchByMultipleKeywordsUsingRegex1() {
		String testName = "Venue a";
		Iterable<Venue> results = venueService.findAllMultipleKeywordsUsingRegex(testName);
		verify(venueRepository, times(1)).findAllContainingAlternativeIgnoreCaseOrderByNameAsc(anyString(), anyString(), anyString(), anyString());
		
		Iterator<Venue> iterator = results.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(v1, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(v3, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(v4, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(v6, iterator.next());
		assertFalse(iterator.hasNext());
		
	}
	
	@Test
	public void testSearchByMultipleKeywordsUsingRegex2() {
		String testName = "Event d";
		Iterable<Venue> results = venueService.findAllMultipleKeywordsUsingRegex(testName);
		verify(venueRepository, times(1)).findAllContainingAlternativeIgnoreCaseOrderByNameAsc(anyString(), anyString(), anyString(), anyString());
		
		Iterator<Venue> iterator = results.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(v4, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(v6, iterator.next());
		assertFalse(iterator.hasNext());
	}
	
	@Test
	public void testSearchByMultipleKeywords1() {
		String testName = "Event a";
		Iterable<Venue> results = venueService.findAllMultipleKeywords(testName);
		verify(venueRepository, times(2)).findAllContainingAlternativeIgnoreCaseOrderByNameAsc(anyString(), anyString(), anyString(), anyString());
		
		Iterator<Venue> iterator = results.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(v1, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(v3, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(v4, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(v6, iterator.next());
		assertFalse(iterator.hasNext());
		
	}
	
	@Test
	public void testSearchByMultipleKeywords2() {
		String testName = "Event a d";
		Iterable<Venue> results = venueService.findAllMultipleKeywords(testName);
		verify(venueRepository, times(3)).findAllContainingAlternativeIgnoreCaseOrderByNameAsc(anyString(), anyString(), anyString(), anyString());
		
		Iterator<Venue> iterator = results.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(v4, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(v6, iterator.next());
		assertFalse(iterator.hasNext());
	}
	
	@Test
	public void testMapBoxGeocodingCall() throws Exception {
		Venue myVenue = new Venue();
		myVenue.setName("New venue");
		myVenue.setAddress("Oxford Rd, Manchester");
		myVenue.setPostcode("M13 9PL");
		myVenue.setCapacity(100);
		
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

	// This class is here as a starter for testing any custom methods within the
	// VenueService. Note: It is currently @Disabled!
}
