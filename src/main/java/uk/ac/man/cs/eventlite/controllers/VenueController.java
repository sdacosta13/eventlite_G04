package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenueController {
	
	@Autowired
	private VenueService venueService;
	
	@GetMapping(value="/addVenue")
	public String addVenuePage(Model model) {
		model.addAttribute("venue", new Venue());
		return "venues/addVenue";
	}
	@PostMapping(value="/venueSubmit")
	public String venueSubmit(@ModelAttribute Venue venue) {
		venueService.save(venue);
		return "redirect:/events";
	}
}
