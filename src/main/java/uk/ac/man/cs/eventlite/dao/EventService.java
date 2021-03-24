package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();
	public Iterable<Event> findAllByNameContaining(String infix);
	public Iterable<Event> findAllByVenue(Venue venue);
	public Iterable<Event> findAllContainingAlternativeIgnoreCaseOrderByNameAscDateAsc(String prefix, String suffix, String infix, String full);
	public Iterable<Event> findAllFromSearch(String name);
	public Iterable<Event> findAllSingleKeyword(String name);
	public Iterable<Event> findAllMultipleKeywords(String name);
	public Iterable<Event> findAllMultipleKeywordsUsingRegex(String name);
	
	public Iterable<Event> findEventsAtVenue(Venue venue);
	
	public Event findById(long id);

	public void save(Event entity);

	public void deleteById(long id);
	
	public Optional<Event> findEventById(long id);
}
