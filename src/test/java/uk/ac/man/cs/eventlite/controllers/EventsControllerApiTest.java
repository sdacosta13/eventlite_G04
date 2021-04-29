package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsControllerApi.class)
@Import(Security.class)
public class EventsControllerApiTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private EventService eventService;

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());

		mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(1)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/events")));

		verify(eventService).findAll();
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		Event e = new Event();
		e.setId(0);
		e.setName("Event");
		e.setDate(LocalDate.now());
		e.setTime(LocalTime.now());
		
		Venue venue = new Venue();
		venue.setCapacity(100);
		venue.setName("Name 1");
		
		e.setVenue(venue);
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(e));

		mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(2)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/events")))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(1)));

		verify(eventService).findAll();
	}
	@Test
	public void getEventListTest() throws Exception{
		Venue venue1 = new Venue();
		venue1.setName("Kilburn, G23");
		venue1.setAddress("The University of Manchester\nOxford Rd\nManchester");
		venue1.setPostcode("M13 9PL");
		venue1.setCapacity(100);
		venue1.setCoords();
		
		Event event1 = new Event();
		event1.setName("COMP23412 Showcase, group G");
		event1.setTime(LocalTime.of(16, 0));
		event1.setDate(LocalDate.of(2021, 5, 13));
		event1.setDescription("An event for showcasing your product made during COMP23412. Specifically for Lab Group G");
		event1.setVenue(venue1);
		
		Event event2 = new Event();
		event2.setName("COMP23412 Showcase, group H");
		event2.setTime(LocalTime.of(11, 0));
		event2.setDate(LocalDate.of(2021, 5, 11));
		event2.setVenue(venue1);
		event2.setDescription("An event for showcasing your product made during COMP23412. Specifically for Lab Group H");
		ArrayList<Event> el = new ArrayList<Event>();
		el.add(event1);
		el.add(event2);
		when(eventService.findAll()).thenReturn(el);
		mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
		.andExpect(jsonPath("$._links.self.href", endsWith("/api/events")))
		.andExpect(jsonPath("$._embedded.events.length()", equalTo(2)))
		.andExpect(jsonPath("$._embedded.events[0].name", equalTo(String.valueOf(event1.getName()))))
		.andExpect(jsonPath("$._embedded.events[1].name", equalTo(String.valueOf(event2.getName()))))
		.andExpect(jsonPath("$._embedded.events[0].venue.name", equalTo(String.valueOf(event1.getVenue().getName()))))
		.andExpect(jsonPath("$._embedded.events[1].venue.name", equalTo(String.valueOf(event2.getVenue().getName()))));
		verify(eventService).findAll();
	}
	
	@Test
	public void getEventJsonTest() throws Exception {
		Event event = new Event();
		event.setDate(LocalDate.now());
		event.setName("Hello");
		event.setTime(LocalTime.now());
		when(eventService.findById(0)).thenReturn(event);
		
		mvc.perform(get("/api/events/0").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getEventJson"))
				.andExpect(jsonPath("$.date", equalTo(String.valueOf(event.getDate()))))
				.andExpect(jsonPath("$.time", startsWith(String.valueOf(event.getTime().format(DateTimeFormatter.ofPattern("HH:mm"))))))
				.andExpect(jsonPath("$.name", equalTo(String.valueOf(event.getName()))))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/events/0")))
				.andExpect(jsonPath("$._links.event.href", endsWith("/api/events/0")))
				.andExpect(jsonPath("$._links.venue.href", endsWith("/api/events/0/venue")));
	}

	@Test
	public void deleteEventNoAuth() throws Exception {
		mvc.perform(delete("/api/events/666")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());

		verify(eventService, never()).deleteById(666);
	}

	@Test
	public void deleteEvent() throws Exception {
		mvc.perform(delete("/api/events/666")
				.accept(MediaType.APPLICATION_JSON)
				.with(user("Rob").roles(Security.ADMIN_ROLE)))
				.andExpect(status().isNoContent());

		verify(eventService).deleteById(666);
	}
}
