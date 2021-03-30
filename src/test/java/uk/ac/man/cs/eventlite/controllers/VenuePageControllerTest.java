package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuePageController.class)
@Import(Security.class)
public class VenuePageControllerTest {

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

	@Test
	public void getPageWhenVenueExists() throws Exception {
		Optional<Venue> venueContainer = Optional.of(venue);
		when(venueService.findVenueById(0)).thenReturn(venueContainer);
		when(eventService.findEventsAtVenue(venue)).thenReturn(Collections.emptyList());

		mvc.perform(get("/venue/0").accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
		.andExpect(view().name("venues/info-page")).andExpect(handler().methodName("getVenueInfo"));

		verify(venueService).findVenueById(0);
		verify(eventService).findEventsAtVenue(venue);
	}
}