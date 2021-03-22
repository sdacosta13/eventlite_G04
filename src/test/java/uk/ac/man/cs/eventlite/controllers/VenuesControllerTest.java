package uk.ac.man.cs.eventlite.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

import java.util.Collections;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesController.class)
public class VenuesControllerTest {

	@Autowired
	private MockMvc mvc;

	@Mock
	private Venue venue;

	@MockBean
	private VenueService venueService;

	@Test
	public void getIndexWhenNoVenues() throws Exception {
		when(venueService.findAll()).thenReturn(Collections.emptyList());

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

		verify(venueService).findAll();
		verifyNoInteractions(venue);
	}

	@Test
	public void getIndexWithVenues() throws Exception {
		when(venueService.findAll()).thenReturn(Collections.singletonList(venue));

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

		verify(venueService).findAll();
		verify(venue).getName();
	}
	
	@Test
	public void updateVenueWithoutAuthentication() throws Exception {
		mvc.perform(post("/venues/updateVenue").contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", endsWith("/sign-in")));
		
		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void updateVenueWithAuthentication() throws Exception {
		ArgumentCaptor<Venue> arg = ArgumentCaptor.forClass(Venue.class);
		
		// All parameters satisfy the constraints
		mvc.perform(post("/venues/updateVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "NASA")
				.param("address", "NASA address")
				.param("postcode", "123456")
				.param("capacity", "10000")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/venues"))
				.andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("updateVenue"))
				.andExpect(flash().attributeExists("ok_message"));
		
		verify(venueService).save(arg.capture());
	}
	
	@Test
	public void updateWithAuthenticationBadCapacity() throws Exception {
		
		// 0 as capacity
		mvc.perform(post("/venues/updateVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "NASA")
				.param("address", "NASA address")
				.param("postcode", "123456")
				.param("capacity", "00")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/updateVenue"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("updateVenue"));
		
		// Negative number
		mvc.perform(post("/venues/updateVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "NASA")
				.param("address", "NASA address")
				.param("postcode", "123456")
				.param("capacity", "-105")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/updateVenue"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("updateVenue"));
		
		// Floating-point number
		mvc.perform(post("/venues/updateVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "NASA")
				.param("address", "NASA address")
				.param("postcode", "123456")
				.param("capacity", "12.24")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/updateVenue"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("updateVenue"));
		
		// No capacity
		mvc.perform(post("/venues/updateVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "NASA")
				.param("address", "NASA address")
				.param("postcode", "123456")
				.param("capacity", "")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/updateVenue"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("updateVenue"));
	}
	
	@Test
	public void updateWithAuthenticationNoName() throws Exception {
		mvc.perform(post("/venues/updateVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "")
				.param("address", "address")
				.param("postcode", "123456")
				.param("capacity", "12")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/updateVenue"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("updateVenue"));
	}
	
	@Test
	public void updateWithAuthenticationNoAddress() throws Exception {
		mvc.perform(post("/venues/updateVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "NASA")
				.param("address", "")
				.param("postcode", "123456")
				.param("capacity", "10")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/updateVenue"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("updateVenue"));
	}
	
	@Test
	public void updateWithAuthenticationNoPostCode() throws Exception {
		mvc.perform(post("/venues/updateVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "NASA")
				.param("address", "NASA address")
				.param("postcode", "")
				.param("capacity", "11")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/updateVenue"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("updateVenue"));
	}
}
