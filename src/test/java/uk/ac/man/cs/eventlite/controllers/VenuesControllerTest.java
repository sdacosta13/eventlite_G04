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
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesController.class)
public class VenuesControllerTest {

	@Autowired
	private MockMvc mvc;

	@Mock
	private Venue venue;

	@MockBean
	private VenueService venueService;

	@MockBean
	private EventService eventService;

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
	public void getVenueForUpdateTest() throws Exception {
		when(venueService.findVenueById(8888)).thenReturn(Optional.of(venue));
		
		mvc.perform(get("/venues/updateVenue/8888").contentType(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/updateVenue"))
				.andExpect(handler().methodName("getVenueById"));
		
		verify(venueService).findVenueById(8888);
	}
	
	@Test
	public void getNonExistentVenueForUpdateTest() throws Exception {
		mvc.perform(get("/venues/updateVenue/8888").contentType(MediaType.TEXT_HTML))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/venues"))
				.andExpect(handler().methodName("getVenueById"));
		
		verify(venueService).findVenueById(8888);
	}
	
	@Test
	public void updateVenueWithoutAuthentication() throws Exception {
		mvc.perform(post("/venues/updateVenue").contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", endsWith("/sign-in")));
		
		verify(venueService, never()).save(any());
	}
	
	@Test
	public void addVenueWithoutAuthentication() throws Exception {
		mvc.perform(post("/venues/venueSubmit").contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", endsWith("/sign-in")));
		
		verify(venueService, never()).save(any());
	}
	
	@Test
	public void updateVenueWithAuthentication() throws Exception {
		when(venueService.findById(1111)).thenReturn(venue);
		
		// All parameters satisfy the constraints
		mvc.perform(post("/venues/updateVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("id", "1111")
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
		
		verify(venueService).save(any());
	}
	
	@Test
	public void addVenueWithAuthentication() throws Exception {
		// All parameters satisfy the constraints
		mvc.perform(post("/venues/venueSubmit").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "NASA")
				.param("address", "NASA address")
				.param("postcode", "123456")
				.param("capacity", "10000")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/events"))
				.andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("venueSubmit"))
				.andExpect(flash().attributeExists("ok_message"));
		
		verify(venueService).save(any());
	}
	
	@Test
	public void updateNonExistentVenueWithAuthentication() throws Exception {
		when(venueService.findById(1111)).thenReturn(venue);
		
		// All parameters satisfy the constraints
		mvc.perform(post("/venues/updateVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("id", "2222")
				.param("name", "NASA")
				.param("address", "NASA address")
				.param("postcode", "123456")
				.param("capacity", "10000")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/venues"))
				.andExpect(flash().attributeExists("error_message"));
		
		verify(venueService, never()).save(any());
	}
	
	@Test
	public void updateWithAuthenticationBadCapacity() throws Exception {
		when(venueService.findById(1111)).thenReturn(venue);
		
		// 0 as capacity
		mvc.perform(post("/venues/updateVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("id", "1111")
				.param("name", "NASA")
				.param("address", "NASA address")
				.param("postcode", "123456")
				.param("capacity", "00")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/updateVenue"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("updateVenue"));
		
		verify(venueService, never()).save(any());
		
		// Negative number
		mvc.perform(post("/venues/updateVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("id", "1111")
				.param("name", "NASA")
				.param("address", "NASA address")
				.param("postcode", "123456")
				.param("capacity", "-105")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/updateVenue"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("updateVenue"));
		
		verify(venueService, never()).save(any());
		
		// Floating-point number
		mvc.perform(post("/venues/updateVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("id", "1111")
				.param("name", "NASA")
				.param("address", "NASA address")
				.param("postcode", "123456")
				.param("capacity", "12.24")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/updateVenue"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("updateVenue"));
		
		verify(venueService, never()).save(any());
		
		// No capacity
		mvc.perform(post("/venues/updateVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("id", "1111")
				.param("name", "NASA")
				.param("address", "NASA address")
				.param("postcode", "123456")
				.param("capacity", "")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/updateVenue"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("updateVenue"));
		
		verify(venueService, never()).save(any());
	}
	
	@Test
	public void addVenueWithAuthenticationBadCapacity() throws Exception {
		// 0 as capacity
		mvc.perform(post("/venues/venueSubmit").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "NASA")
				.param("address", "NASA address")
				.param("postcode", "123456")
				.param("capacity", "00")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/addVenue"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("venueSubmit"));
		
		verify(venueService, never()).save(any());
		
		// Negative number
		mvc.perform(post("/venues/venueSubmit").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "NASA")
				.param("address", "NASA address")
				.param("postcode", "123456")
				.param("capacity", "-105")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/addVenue"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("venueSubmit"));
		
		verify(venueService, never()).save(any());
		
		// Floating-point number
		mvc.perform(post("/venues/venueSubmit").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "NASA")
				.param("address", "NASA address")
				.param("postcode", "123456")
				.param("capacity", "12.24")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/addVenue"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("venueSubmit"));
		
		verify(venueService, never()).save(any());
		
		// No capacity
		mvc.perform(post("/venues/venueSubmit").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "NASA")
				.param("address", "NASA address")
				.param("postcode", "123456")
				.param("capacity", "")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/addVenue"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("venueSubmit"));
		
		verify(venueService, never()).save(any());
	}
	
	@Test
	public void updateWithAuthenticationNoName() throws Exception {
		when(venueService.findById(1111)).thenReturn(venue);
		
		mvc.perform(post("/venues/updateVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("id", "1111")
				.param("name", "")
				.param("address", "address")
				.param("postcode", "123456")
				.param("capacity", "12")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/updateVenue"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("updateVenue"));
		
		verify(venueService, never()).save(any());
	}
	
	@Test
	public void addVenueWithAuthenticationNoName() throws Exception {
		mvc.perform(post("/venues/venueSubmit").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "")
				.param("address", "address")
				.param("postcode", "123456")
				.param("capacity", "12")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/addVenue"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("venueSubmit"));
		
		verify(venueService, never()).save(any());
	}
	
	@Test
	public void updateWithAuthenticationNoAddress() throws Exception {
		when(venueService.findById(1111)).thenReturn(venue);
		
		mvc.perform(post("/venues/updateVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("id", "1111")
				.param("name", "NASA")
				.param("address", "")
				.param("postcode", "123456")
				.param("capacity", "10")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/updateVenue"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("updateVenue"));
		
		verify(venueService, never()).save(any());
	}
	
	@Test
	public void addVenueWithAuthenticationNoAddress() throws Exception {
		mvc.perform(post("/venues/venueSubmit").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "NASA")
				.param("address", "")
				.param("postcode", "123456")
				.param("capacity", "10")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/addVenue"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("venueSubmit"));
		
		verify(venueService, never()).save(any());
	}
	
	@Test
	public void updateWithAuthenticationNoPostCode() throws Exception {
		when(venueService.findById(1111)).thenReturn(venue);
		
		mvc.perform(post("/venues/updateVenue").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("id", "1111")
				.param("name", "NASA")
				.param("address", "NASA address")
				.param("postcode", "")
				.param("capacity", "11")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/updateVenue"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("updateVenue"));
		
		verify(venueService, never()).save(any());
	}
	
	@Test
	public void addVenueWithAuthenticationNoPostCode() throws Exception {
		mvc.perform(post("/venues/venueSubmit").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.with(csrf())
				.param("name", "NASA")
				.param("address", "NASA address")
				.param("postcode", "")
				.param("capacity", "11")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/addVenue"))
				.andExpect(model().hasErrors())
				.andExpect(handler().methodName("venueSubmit"));
		
		verify(venueService, never()).save(any());
	}

	@Test
	public void deleteVenueNoAuth() throws Exception {
		long id = 666;

		mvc.perform(delete("/events/666")
				.accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", endsWith("/sign-in")));

		verify(venueService, never()).findVenueById(id);
		verify(venueService, never()).deleteById(666);
	}

	@Test
	public void deleteVenueNotFound() throws Exception {
		long id = 666;

		when(venueService.findVenueById(id)).thenReturn(Optional.empty());

		mvc.perform(delete("/venues/" + id)
				.accept(MediaType.TEXT_HTML)
				.with(user("Rob").roles(Security.ADMIN_ROLE))
				.with(csrf()))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/venues"))
				.andExpect(flash().attribute("error_message", "Failed to delete venue. No venue by that id."));

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

		mvc.perform(delete("/venues/" + id)
				.accept(MediaType.TEXT_HTML)
				.with(user("Rob").roles(Security.ADMIN_ROLE))
				.with(csrf()))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/venue/" + id))
				.andExpect(flash().attribute("error_message", "Failed to delete venue. Can not delete a venue with events."));

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

		mvc.perform(delete("/venues/" + id)
				.accept(MediaType.TEXT_HTML)
				.with(user("Rob").roles(Security.ADMIN_ROLE))
				.with(csrf()))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/venues"))
				.andExpect(flash().attribute("ok_message", "Successfully deleted venue."));

		verify(venueService).findVenueById(id);
		verify(eventService).findAllByVenue(v);
		verify(venueService).deleteById(id);
	}
}
