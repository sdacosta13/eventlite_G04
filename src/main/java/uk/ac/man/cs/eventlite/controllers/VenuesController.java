package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import uk.ac.man.cs.eventlite.dao.VenueService;

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
			//model.addAttribute("venues", venueService.findAllFromSearch(name));
		}
		return "venues/index";
	}
}
