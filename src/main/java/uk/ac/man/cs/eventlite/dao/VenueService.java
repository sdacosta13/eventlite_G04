package uk.ac.man.cs.eventlite.dao;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;


public interface VenueService {
	public Iterable<Venue> findAllContainingAlternativeIgnoreCaseOrderByNameAsc(String prefix, String suffix, String infix, String full);
	public Iterable<Venue> findAllFromSearch(String name);
	public Iterable<Venue> findAllSingleKeyword(String name);
	public Iterable<Venue> findAllMultipleKeywords(String name);
	public Iterable<Venue> findAllMultipleKeywordsUsingRegex(String name);

	public long count();

	public Iterable<Venue> findAll();
	
	public Venue save(Venue entity);
}
