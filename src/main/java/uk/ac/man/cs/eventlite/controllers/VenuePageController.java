package uk.ac.man.cs.eventlite.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value = "venue/{venueId}", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuePageController {
	
	@Autowired
	private VenueService venueService;
	
	@Autowired
	private EventService eventService;
	
	@GetMapping
	public String getVenueInfo(Model model, @PathVariable Long venueId) {
		Optional<Venue> venueContainer = venueService.findVenueById(venueId);
		if (!venueContainer.isPresent()) {
			return "redirect:/venues";
		}
		Venue venue = venueContainer.get();
		
		model.addAttribute("venue", venue);
		model.addAttribute("events", eventService.findEventsAtVenue(venue));
		return "venues/info-page";
	}
}