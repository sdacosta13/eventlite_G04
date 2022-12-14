package uk.ac.man.cs.eventlite.config.data;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.mapbox.api.geocoding.v5.MapboxGeocoding;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.entities.Event;

@Component
@Profile({ "default", "test" })
public class InitialDataLoader implements ApplicationListener<ContextRefreshedEvent> {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {

		if (eventService.count() > 0 && venueService.count() > 0) {
			log.info("Database already populated. Skipping data initialization.");
			return;
		}
		
		// Build and save initial models here.
		
		Venue venue1 = new Venue();
		venue1.setName("Kilburn, G23");
		venue1.setAddress("The University of Manchester\nOxford Rd\nManchester");
		venue1.setPostcode("M13 9PL");
		venue1.setCapacity(100);
		venue1.setCoords();
		venueService.save(venue1);
		
		Venue venue2 = new Venue();
		venue2.setName("Online");
		venue2.setAddress("zoom.us");
		venue2.setPostcode("AAA 777");
		venue2.setCapacity(100000);
		venue2.setCoords();
		venueService.save(venue2);
		
		Venue venue3 = new Venue();
		venue3.setName("NASA");
		venue3.setAddress("300 Hidden Figures Way SW Washington D.C.");
		venue3.setPostcode("20546-0001");
		venue3.setCapacity(500);
		venue3.setCoords();
		venueService.save(venue3);
		
		Event event1 = new Event();
		event1.setName("COMP23412 Showcase, group G");
		event1.setTime(LocalTime.of(16, 0));
		event1.setDate(LocalDate.of(2022, 5, 13));
		event1.setDescription("An event for showcasing your product made during COMP23412. Specifically for Lab Group G");
		event1.setVenue(venue1);
		
		Event event2 = new Event();
		event2.setId(5);	// Hard code this one to use in the integration tests
		event2.setName("COMP23412 Showcase, group H");
		event2.setTime(LocalTime.of(11, 0));
		event2.setDate(LocalDate.of(2022, 5, 11));
		event2.setVenue(venue2);
		event2.setDescription("An event for showcasing your product made during COMP23412. Specifically for Lab Group H");
		
		Event event3 = new Event();
		event3.setName("COMP23412 Showcase, group F");
		event3.setTime(LocalTime.of(16, 00));
		event3.setDate(LocalDate.of(2022, 5, 10));
		event3.setVenue(venue3);
		event3.setDescription("An event for showcasing your product made during COMP23412. Specifically for Lab Group F");
		
		eventService.save(event1);
		eventService.save(event2);
		eventService.save(event3);
		
		Venue venue4 = new Venue();
		venue4.setName("Disney");
		venue4.setAddress("Defense Pentagon Washington, DC");
		venue4.setPostcode("20301-1100");
		venue4.setCapacity(10000);
		venue4.setCoords();
		venueService.save(venue4);

	}
}
