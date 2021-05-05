package uk.ac.man.cs.eventlite.controllers;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {
	@Autowired
	private VenueService venueService;

	@Autowired
	private EventService eventService;

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
		if (venueService.findById(venue.getId()) == null) {
			redirectAttrs.addFlashAttribute("error_message", "Venue does not exist.");
			return "redirect:/venues";
		}
		if(!venue.setCoords()) {
			redirectAttrs.addFlashAttribute("error_message",
					"Failed to add venue. Can not find the address");
			return "redirect:/venues/updateVenue/"+venue.getId();
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
	@PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, value="venueSubmit")
	public String venueSubmit(@RequestBody @Valid @ModelAttribute Venue venue, BindingResult errors,
			Model model, RedirectAttributes redirectAttrs) {
		
		if (errors.hasErrors()) {
			model.addAttribute("venue", venue);
			return "venues/addVenue";
		}
		if(!venue.setCoords()) {
			redirectAttrs.addFlashAttribute("error_message",
					"Failed to add venue. Can not find the address");
			return "redirect:/venues/addVenue/";
		}
		venueService.save(venue);
		return "redirect:/events";
	}

	@DeleteMapping("/{id}")
	public String deleteEvent(@PathVariable("id") long id, RedirectAttributes redirectAttrs) {
		Optional<Venue> venue = venueService.findVenueById(id);

		if (venue.isEmpty()) {
			redirectAttrs.addFlashAttribute("error_message",
					"Failed to delete venue. No venue by that id.");
			return "redirect:/venues";
		}

		Iterable<Event> events = eventService.findAllByVenue(venue.get());
		if (events.iterator().hasNext()) {
			redirectAttrs.addFlashAttribute("error_message",
					"Failed to delete venue. Can not delete a venue with events.");
			return "redirect:/venue/" + id;
		}

		venueService.deleteById(venue.get().getId());

		redirectAttrs.addFlashAttribute("ok_message",
				"Successfully deleted venue.");
		return "redirect:/venues";
	}
}
