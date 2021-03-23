package uk.ac.man.cs.eventlite.controllers;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {
	@Autowired
	private VenueService venueService;

	@GetMapping
	public String getAllVenues(@RequestParam(value = "name", required = false) String name, Model model) {
		if (name == null) {
			model.addAttribute("venues", venueService.findAll());
		} else {
			model.addAttribute("venues", venueService.findAllFromSearch(name));
		}
		return "venues/index";
	}
	
	@GetMapping("updateVenue/{id}")
	public String getVenueById(@PathVariable("id") long id, Model model) {
		Optional<Venue> venue = venueService.findVenueById(id);
		
		if (venue.isEmpty()) {
			return "redirect:/venues";
		}
		
		model.addAttribute("venue", venue.get());
		return "venues/updateVenue";
	}
	
	@PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, value="updateVenue")
	public String updateVenue(@RequestBody @Valid @ModelAttribute Venue venue, BindingResult errors,
			Model model, RedirectAttributes redirectAttrs) {
		
		if (errors.hasErrors()) {
			model.addAttribute("venue", venue);
			return "venues/updateVenue";
		}
		
		venueService.save(venue);
		redirectAttrs.addFlashAttribute("ok_message", "Venue updated successfuly.");
		
		return "redirect:/venues";
	}
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
