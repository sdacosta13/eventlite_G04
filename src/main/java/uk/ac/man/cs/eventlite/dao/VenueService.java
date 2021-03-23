package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Venue;


public interface VenueService {
	public Iterable<Venue> findAllContainingAlternativeIgnoreCaseOrderByNameAsc(String prefix, String suffix, String infix, String full);
	public Iterable<Venue> findAllFromSearch(String name);
	public Iterable<Venue> findAllSingleKeyword(String name);
	public Iterable<Venue> findAllMultipleKeywords(String name);
	public Iterable<Venue> findAllMultipleKeywordsUsingRegex(String name);

	public long count();

	public Iterable<Venue> findAll();
	
	public Optional<Venue> findVenueById(long id);
	
	public void deleteById(long id);

	public Venue save(Venue entity);
}
