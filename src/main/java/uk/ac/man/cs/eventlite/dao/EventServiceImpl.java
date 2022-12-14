package uk.ac.man.cs.eventlite.dao;


import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Service
public class EventServiceImpl implements EventService {

	private final static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

	private final static String DATA = "data/events.json";

	@Autowired
	private EventRepository eventRepository;

	@Override
	public long count() {
		return eventRepository.count();
	}

	@Override
	public Iterable<Event> findAll() {
		return eventRepository.findAllByOrderByDateAsc();
	}
	
	@Override
	public Iterable<Event> findAllByNameContaining(String infix) {
		return eventRepository.findAllByNameContaining(infix);
	}

	@Override
	public Iterable<Event> findAllByVenue(Venue venue) {
		return eventRepository.findAllByVenue(venue);
	}
	
	@Override
	public Iterable<Event> nextEvents(Optional<Venue> venue, long amount) {
		ArrayList<Event> returnList = new ArrayList<Event>();
		if (venue.isEmpty()) { return returnList; }
		Iterable<Event> upcomingEvents = eventRepository.findEventsAtVenue(venue.get(), LocalDate.now());
		Iterator<Event> iterator = ((Iterable<Event>) upcomingEvents).iterator();
		long i = 0;
		while ((iterator.hasNext()) && (i < amount)) {
			returnList.add(iterator.next());
			i++;
		}
		return returnList;
	}
	
	@Override
	public Iterable<Event> findAllContainingAlternativeIgnoreCaseOrderByNameAscDateAsc(String prefix, String suffix, String infix, String full) {
		return eventRepository.findAllContainingAlternativeIgnoreCaseOrderByNameAscDateAsc(prefix, suffix, infix, full);
	}
	
	@Override	
	public Iterable<Event> findAllSingleKeyword(String name) {
		return findAllContainingAlternativeIgnoreCaseOrderByNameAscDateAsc(name + " %", "% " + name, "% " + name + " %", name);
	}
	
	@Override	
	public Iterable<Event> findAllFromSearch(String name) {
		return findAllMultipleKeywordsUsingRegex(name);
	}
	
	@Override
	public Iterable<Event> findAllMultipleKeywords(String name){
		String[] keywords = name.split("\\s+");
		if (keywords.length == 0) return new ArrayList<Event>();
		
		Object[] results = new Object[keywords.length];
		for (int i = 0; i < keywords.length; i++) {
			results[i] = findAllSingleKeyword(keywords[i]);
		}
		ArrayList<Event> returnList = new ArrayList<Event>();
		Iterator<Event> iterator =((Iterable<Event>) results[0]).iterator();
		while(iterator.hasNext()) {
			boolean found = true;
			Event e = iterator.next();
			for (int i = 1; i < keywords.length; i++) {
				if (!search(e, ((Iterable<Event>) results[i]))) {
					found = false;
					break;
				}
			}
			if (found) {
				returnList.add(e);
			}
		}
		
		return returnList;
	}
	
	private boolean search(Event e, Iterable<Event> list) {
		Iterator<Event> iterator = list.iterator();
		while(iterator.hasNext()) {
			if (iterator.next() == e) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Iterable<Event> findAllMultipleKeywordsUsingRegex(String name){
		String[] keywords = name.split("\\s+");
		if (keywords.length == 0) return new ArrayList<Event>();
		
		Iterable<Event> returnList = findAllSingleKeyword(keywords[0]);
		for (int i = 1; i < keywords.length; i++) {
			Iterator<Event> iterator = returnList.iterator();
			while(iterator.hasNext()) {
				Event e = iterator.next();
				if (!findKeyword(e.getName(), keywords[i])) {
					iterator.remove();
				}
			}
		}
		
		return returnList;
	}
	
	private boolean findKeyword(String str, String keyword) {
		str = str.toUpperCase();
		keyword = keyword.toUpperCase();
		return str.matches("(.*) " + keyword + " (.*)") || 
				str.matches("^" + keyword + " (.*)") || 
				str.matches("(.*) " + keyword + "$") || 
				str.matches("^" + keyword + "$");
	}
	
	public Event findById(long id) {
		return eventRepository.findById(id);
	}

	@Override
	public void save(Event entity) {
		eventRepository.save(entity);
	}

	@Override
	public void deleteById(long id) {
		eventRepository.deleteById(id);
	}
	
	@Override
	public Optional<Event> findEventById(long id) {
		return eventRepository.findEventById(id);
	}

	@Override
	public Iterable<Event> findEventsAtVenue(Venue venue) {
		return eventRepository.findEventsAtVenue(venue, LocalDate.now());
	}
}
