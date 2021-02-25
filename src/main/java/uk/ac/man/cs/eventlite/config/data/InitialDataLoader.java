package uk.ac.man.cs.eventlite.config.data;

import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
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

		if (eventService.count() > 0) {
			log.info("Database already populated. Skipping data initialization.");
			return;
		}

		// Build and save initial models here.
		Event event1 = new Event();
		event1.setName("COMP23412 Showcase, group G");
		event1.setTime(LocalTime.of(16, 0));
		event1.setDate(LocalDate.of(2021, 5, 13));
		event1.setVenue(2);
		
		Event event2 = new Event();
		event2.setName("COMP23412 Showcase, group H");
		event2.setTime(LocalTime.of(11, 0));
		event2.setDate(LocalDate.of(2021, 5, 11));
		event2.setVenue(2);
		
		Event event3 = new Event();
		event3.setName("COMP23412 Showcase, group F");
		event3.setTime(LocalTime.of(16, 00));
		event3.setDate(LocalDate.of(2021, 5, 10));
		event3.setVenue(2);
		
		eventService.save(event1);
		eventService.save(event2);
		eventService.save(event3);

	}
}
