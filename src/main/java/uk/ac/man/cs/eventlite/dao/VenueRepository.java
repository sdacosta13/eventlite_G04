package uk.ac.man.cs.eventlite.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;


public interface VenueRepository extends CrudRepository<Venue, Long>{
	@Query("SELECT v FROM Venue v where UPPER(v.name) like UPPER(?1) or UPPER(v.name) like UPPER(?2) or UPPER(v.name) like UPPER(?3) or UPPER(v.name) like UPPER(?4) ORDER BY v.name ASC")
	public Iterable<Venue> findAllContainingAlternativeIgnoreCaseOrderByNameAsc(String prefix, String suffix, String infix, String full);
	
}
