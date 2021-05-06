package uk.ac.man.cs.eventlite.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesControllerApi.class)
public class VenuesControllerApiTest {
	@Autowired
	private MockMvc mvc;

	@MockBean
	private VenueService venueService;

	@MockBean
	private EventService eventService;

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(venueService.findAll()).thenReturn(Collections.emptyList());

		mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllVenues")).andExpect(jsonPath("$.length()", equalTo(1)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")));

		verify(venueService).findAll();
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		Venue v = new Venue();
		v.setId(0);
		v.setName("Venue");
		v.setAddress("localhost");
		v.setPostcode("1337");
		v.setCapacity(100);

		when(venueService.findAll()).thenReturn(Collections.singletonList(v));

		mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllVenues")).andExpect(jsonPath("$.length()", equalTo(2)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")))
				.andExpect(jsonPath("$._embedded.venues.length()", equalTo(1)));

		verify(venueService).findAll();
	}

	@Test
	public void getVenueNotFound() throws Exception {
		long id = 666;

		when(venueService.findVenueById(id)).thenReturn(Optional.empty());

		mvc.perform(get("/api/venues/" + id)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());

		verify(venueService).findVenueById(id);
	}

	@Test
	public void getVenue() throws Exception {
		long id = 666;

		Venue v = new Venue();
		v.setId(id);
		v.setName("Venue");
		v.setAddress("localhost");
		v.setPostcode("1337");
		v.setCapacity(100);

		when(venueService.findVenueById(id)).thenReturn(Optional.of(v));

		mvc.perform(get("/api/venues/" + id)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(handler().methodName("getVenue"))
				.andExpect(jsonPath("$.name", equalTo(v.getName())))
				.andExpect(jsonPath("$.capacity", equalTo(v.getCapacity())))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/" + id)))
				.andExpect(jsonPath("$._links.venue.href", endsWith("/api/venues/" + id)))
				.andExpect(jsonPath("$._links.events.href", endsWith("/api/venues/" + id + "/events")))
				.andExpect(jsonPath("$._links.next3events.href", endsWith("/api/venues/" + id + "/next3events")));

		verify(venueService).findVenueById(id);
	}

	@Test
	public void deleteVenueNoAuth() throws Exception {
		long id = 666;

		mvc.perform(delete("/api/venues/" + 666)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());

		verify(venueService, never()).findVenueById(id);
		verify(venueService, never()).deleteById(666);
	}

	@Test
	public void deleteVenueNotFound() throws Exception {
		long id = 666;

		when(venueService.findVenueById(id)).thenReturn(Optional.empty());
		mvc.perform(delete("/api/venues/" + id)
				.accept(MediaType.APPLICATION_JSON)
				.with(user("Rob").roles(Security.ADMIN_ROLE)))
				.andExpect(status().isNotFound());

		verify(venueService).findVenueById(id);
		verify(venueService, never()).deleteById(666);
	}

	@Test
	public void deleteVenueHasEvents() throws Exception {
		long id = 666;

		Venue v = new Venue();
		v.setId(id);
		v.setName("Venue");
		v.setAddress("localhost");
		v.setPostcode("1337");
		v.setCapacity(100);

		Event e = new Event();
		e.setVenue(v);

		when(venueService.findVenueById(id)).thenReturn(Optional.of(v));
		when(eventService.findAllByVenue(v)).thenReturn(Collections.singletonList(e));

		mvc.perform(delete("/api/venues/" + id)
				.accept(MediaType.APPLICATION_JSON)
				.with(user("Rob").roles(Security.ADMIN_ROLE)))
				.andExpect(status().isConflict());

		verify(venueService).findVenueById(id);
		verify(eventService).findAllByVenue(v);
		verify(venueService, never()).deleteById(id);
	}

	@Test
	public void deleteVenue() throws Exception {
		long id = 666;

		Venue v = new Venue();
		v.setId(id);
		v.setName("Venue");
		v.setAddress("localhost");
		v.setPostcode("1337");
		v.setCapacity(100);

		when(venueService.findVenueById(id)).thenReturn(Optional.of(v));
		when(eventService.findAllByVenue(v)).thenReturn(Collections.<Event>emptyList());

		mvc.perform(delete("/api/venues/" + id)
				.accept(MediaType.APPLICATION_JSON)
				.with(user("Rob").roles(Security.ADMIN_ROLE)))
				.andExpect(status().isNoContent());

		verify(venueService).findVenueById(id);
		verify(eventService).findAllByVenue(v);
		verify(venueService).deleteById(id);
	}
	
	@Test
	public void getNextThreeEvents() throws Exception {
		long id = 666;
		
		Venue v = new Venue();
		v.setId(id);
		v.setName("Venue");
		v.setAddress("localhost");
		v.setPostcode("1337");
		v.setCapacity(100);
		
		Event e = new Event();
		e.setId(1);
		e.setVenue(v);
		e.setName("name");
		e.setDate(LocalDate.now());
		e.setTime(LocalTime.now());
		e.setDescription("description");
		
		when(venueService.findVenueById(id)).thenReturn(Optional.of(v));
		when(eventService.nextEvents(Optional.of(v), 3)).thenReturn(Collections.singletonList(e));

		mvc.perform(get("/api/venues/" + id + "/next3events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getNextThreeEvents")).andExpect(jsonPath("$.length()", equalTo(2)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/666/next3events")))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(1)));

		verify(eventService).nextEvents(Optional.of(v), 3);
	}
}
