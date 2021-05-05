package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.services.TwitterService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsController.class)
@Import(Security.class)
public class EventsControllerTest {

	@Autowired
	private MockMvc mvc;

	@Mock
	private Event event;

	@Mock
	private Venue venue;

	@MockBean
	private EventService eventService;

	@MockBean
	private VenueService venueService;
	
	@MockBean
	private TwitterService twitterService;

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAll();
		verifyNoInteractions(event);
		verifyNoInteractions(venue);
		verify(twitterService).getTimeLine(5);
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		Venue venue = new Venue();
		venue.setName("Kilburn Building");

		Event event = new Event();
		event.setVenue(venue);
		event.setDate(LocalDate.now());

		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(event));

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAll();
		verify(twitterService).getTimeLine(5);
	}

	@Test
	public void deleteEventNoAuth() throws Exception {
		mvc.perform(delete("/events/666")
				.accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", endsWith("/sign-in")));

		verify(eventService, never()).deleteById(666);
	}

	@Test
	public void deleteEvent() throws Exception {
		mvc.perform(delete("/events/666")
				.accept(MediaType.TEXT_HTML)
				.with(user("Rob").roles(Security.ADMIN_ROLE))
				.with(csrf()))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/events"));

		verify(eventService).deleteById(666);
	}
	
	@Test
	public void getEventForUpdateTest() throws Exception {
		when(eventService.findEventById(9999)).thenReturn(Optional.of(event));
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));
		when(event.getVenue()).thenReturn(venue);
		
		mvc.perform(get("/events/updateEvent/9999").contentType(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/updateEvent"))
				.andExpect(handler().methodName("getEventById"));
		
		verify(eventService).findEventById(9999);
	}
	
	@Test
	public void getNonExistentEventForUpdateTest() throws Exception {		
		mvc.perform(get("/events/updateEvent/9999").contentType(MediaType.TEXT_HTML))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/events"))
				.andExpect(handler().methodName("getEventById"));
		
		verify(eventService).findEventById(9999);
	}
	
	@Test
	public void eventAddDropdownTest() throws Exception{
		mvc.perform(get("/events/addEvent").accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/addEvent"))
				.andExpect(handler().methodName("getEventAdder"));
		
		verifyNoInteractions(eventService);
		
	}
	
	@Test
	public void updateEventWithoutAuthenticationTest() throws Exception {
		mvc.perform(post("/events").contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", endsWith("/sign-in")));
		
		verifyNoInteractions(eventService);
	}
	
	@Test
	public void testAddEventFunctionalityWithoutSecurity() throws Exception{
		mvc.perform(post("/events/eventSubmit").contentType(MediaType.APPLICATION_FORM_URLENCODED).with(csrf()))
				.andExpect(header().string("Location", endsWith("/sign-in")));
		
		verifyNoInteractions(eventService);
	}
	
	@Test
	public void updateEventWithAuthenticationTest() throws Exception {
		when(eventService.findById(9999)).thenReturn(event);
		when(venueService.findById(8888)).thenReturn(venue);
		
		mvc.perform(post("/events/updateEvent").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("id", "9999")
				.param("name", "New name")
				.param("date", "2022-03-01")
				.param("time", "")
				.param("venue.id", "8888")
				.param("description", "")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/events"))
				.andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("updateEvent"))
				.andExpect(flash().attributeExists("ok_message"));

		verify(eventService).save(any());
	}
	
	@Test
	public void testAddEventFunctionalityWithSecurity() throws Exception{
		when(venueService.findById(8888)).thenReturn(venue);
		
		mvc.perform(post("/events/eventSubmit").with(user("Rob").roles(Security.ADMIN_ROLE))
				.with(csrf())
				.param("name", "test")
				.param("time", "16:00")
				.param("date", "2022-03-01")
				.param("venue.id", "8888")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isFound())
				.andExpect(model().hasNoErrors())
				.andExpect(redirectedUrl("/events"))
				.andExpect(view().name("redirect:/events"))
				.andExpect(flash().attributeExists("ok_message"));
				
		verify(eventService).save(any());
	}
	
	@Test
	public void updateNonExistingEventTest() throws Exception {
		// Event not mocked here
		
		mvc.perform(post("/events/updateEvent").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("id", "9999")
				.param("name", "New name")
				.param("date", "2022-03-01")
				.param("time", "")
				.param("venue.id", String.valueOf(venue.getId()))
				.param("description", "")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/events"))
				.andExpect(flash().attributeExists("error_message"));

		verify(eventService, never()).save(any());
	}
	
	@Test
	public void updateEventNoNameTest() throws Exception {
		when(eventService.findById(9999)).thenReturn(event);
		when(venueService.findById(8888)).thenReturn(venue);
		
		mvc.perform(post("/events/updateEvent").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("id", "9999")
				.param("name", "")
				.param("date", "2022-03-01")
				.param("time", "")
				.param("venue.id", "8888")
				.param("description", "")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/updateEvent"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("updateEvent"));
		
		verify(eventService, never()).save(any());
	}
	
	@Test
	public void addEventNoNameTest() throws Exception {
		when(venueService.findById(8888)).thenReturn(venue);
		
		mvc.perform(post("/events/eventSubmit").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "")
				.param("date", "2022-03-01")
				.param("time", "")
				.param("venue.id", "8888")
				.param("description", "")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/addEvent"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("addEvent"));
		
		verify(eventService, never()).save(any());
	}
	
	@Test
	public void updateEventBadNameTest() throws Exception {
		when(eventService.findById(9999)).thenReturn(event);
		when(venueService.findById(8888)).thenReturn(venue);
		
		mvc.perform(post("/events/updateEvent").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("id", "9999")
				.param("name", "A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... \"\n"
						+ "				+ \"A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... ")
				.param("date", "2022-03-01")
				.param("time", "")
				.param("venue.id", "8888")
				.param("description", "")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/updateEvent"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("updateEvent"));
		
		verify(eventService, never()).save(any());
	}
	
	@Test
	public void addEventBadNameTest() throws Exception {
		when(venueService.findById(8888)).thenReturn(venue);
		
		mvc.perform(post("/events/eventSubmit").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... \"\n"
						+ "				+ \"A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... ")
				.param("date", "2022-03-01")
				.param("time", "")
				.param("venue.id", "8888")
				.param("description", "")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/addEvent"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("addEvent"));
		
		verify(eventService, never()).save(any());
	}
	
	@Test
	public void updateEventNoDateTest() throws Exception {
		when(eventService.findById(9999)).thenReturn(event);
		when(venueService.findById(8888)).thenReturn(venue);
		
		mvc.perform(post("/events/updateEvent").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("id", "9999")
				.param("name", "New name")
				.param("date", "")
				.param("time", "")
				.param("venue.id", "8888")
				.param("description", "")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/updateEvent"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("updateEvent"));
		
		verify(eventService, never()).save(any());
	}
	
	@Test
	public void addEventNoDateTest() throws Exception {
		when(venueService.findById(8888)).thenReturn(venue);
		
		mvc.perform(post("/events/eventSubmit").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "New name")
				.param("date", "")
				.param("time", "")
				.param("venue.id", "8888")
				.param("description", "")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/addEvent"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("addEvent"));
		
		verify(eventService, never()).save(any());
	}
	
	@Test
	public void updateEventBadDateTest() throws Exception {
		when(eventService.findById(9999)).thenReturn(event);
		when(venueService.findById(8888)).thenReturn(venue);
		
		// Try date in the past
		mvc.perform(post("/events/updateEvent").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("id", "9999")
				.param("name", "New name")
				.param("date", "2020-08-01")
				.param("time", "")
				.param("venue.id", "8888")
				.param("description", "")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/updateEvent"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("updateEvent"));
		
		verify(eventService, never()).save(any());
	}
	
	@Test
	public void addEventBadDateTest() throws Exception {
		when(venueService.findById(8888)).thenReturn(venue);
		
		mvc.perform(post("/events/eventSubmit").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "New name")
				.param("date", "2020-08-01")
				.param("time", "")
				.param("venue.id", "8888")
				.param("description", "")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/addEvent"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("addEvent"));
		
		verify(eventService, never()).save(any());
	}
	
	@Test
	public void updateEventNoVenueTest() throws Exception {
		when(eventService.findById(9999)).thenReturn(event);
		when(venueService.findById(8888)).thenReturn(venue);
		
		mvc.perform(post("/events/updateEvent").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("id", "9999")
				.param("name", "New name")
				.param("date", "2022-08-01")
				.param("time", "")
				.param("venue.id", "")
				.param("description", "")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/updateEvent"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("updateEvent"));
		
		verify(eventService, never()).save(any());
	}
	
	@Test
	public void addEventNoVenueTest() throws Exception {
		when(venueService.findById(8888)).thenReturn(venue);
		
		mvc.perform(post("/events/eventSubmit").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "New name")
				.param("date", "2022-08-01")
				.param("time", "")
				.param("venue.id", "")
				.param("description", "")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/addEvent"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("addEvent"));
		
		verify(eventService, never()).save(any());
	}
	
	@Test
	public void updateEventNonExistentVenueTest() throws Exception {
		when(eventService.findById(9999)).thenReturn(event);
		
		mvc.perform(post("/events/updateEvent").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("id", "9999")
				.param("name", "New name")
				.param("date", "2022-03-01")
				.param("time", "")
				.param("venue.id", "8888")
				.param("description", "")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/events/updateEvent"))
				.andExpect(flash().attributeExists("error_message"));

		verify(eventService, never()).save(any());
	}
	
	@Test
	public void addEventNonExistentVenueTest() throws Exception {
		mvc.perform(post("/events/eventSubmit").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "New name")
				.param("date", "2022-08-01")
				.param("time", "")
				.param("venue.id", "8888")
				.param("description", "")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/events"))
				.andExpect(flash().attributeExists("error_message"));
		
		verify(eventService, never()).save(any());
	}
	
	@Test
	public void updateEventBadDescription() throws Exception {
		when(eventService.findById(9999)).thenReturn(event);
		when(venueService.findById(8888)).thenReturn(venue);
		
		mvc.perform(post("/events/updateEvent").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("id", "9999")
				.param("name", "New name")
				.param("date", "2022-08-01")
				.param("time", "")
				.param("venue.id", "8888")
				.param("description", "A very long string meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... "
						+ "A very long string meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... "
						+ "A very long string meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... ")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/updateEvent"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("updateEvent"));
		
		verify(eventService, never()).save(any());
	}
	
	@Test
	public void addEventBadDescription() throws Exception {
		when(venueService.findById(8888)).thenReturn(venue);
		
		mvc.perform(post("/events/eventSubmit").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "New name")
				.param("date", "2022-08-01")
				.param("time", "")
				.param("venue.id", "8888")
				.param("description", "A very long string meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... "
						+ "A very long string meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... "
						+ "A very long string meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... ")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/addEvent"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("addEvent"));
		
		verify(eventService, never()).save(any());
	}
}
