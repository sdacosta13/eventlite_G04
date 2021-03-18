package uk.ac.man.cs.eventlite.dao;

import java.util.ArrayList;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Service
public class VenueServiceImpl implements VenueService {
	@Autowired
	private VenueRepository venueRepository;

	@Override
	public Venue save(Venue entity) {
		return venueRepository.save(entity);
	}
	
	@Override
	public long count() {
		return venueRepository.count();
	}

	@Override
	public Iterable<Venue> findAll() {
		return venueRepository.findAll();
	}
	@Override
	public Iterable<Venue> findAllContainingAlternativeIgnoreCaseOrderByNameAsc(String prefix, String suffix, String infix, String full){
		return venueRepository.findAllContainingAlternativeIgnoreCaseOrderByNameAsc(prefix, suffix, infix, full);
	}
	
	@Override
	public Iterable<Venue> findAllSingleKeyword(String name){
		return findAllContainingAlternativeIgnoreCaseOrderByNameAsc(name + " %", "% " + name, "% " + name + " %", name);
	}
	
	@Override
	public Iterable<Venue> findAllFromSearch(String name){
		return findAllMultipleKeywordsUsingRegex(name);
	}
	
	@Override
	public Iterable<Venue> findAllMultipleKeywords(String name){
		String[] keywords = name.split("\\s+");
		if (keywords.length == 0) return new ArrayList<Venue>();
		
		Object[] results = new Object[keywords.length];
		for (int i = 0; i < keywords.length; i++) {
			results[i] = findAllSingleKeyword(keywords[i]);
		}
		ArrayList<Venue> returnList = new ArrayList<Venue>();
		Iterator<Venue> iterator =((Iterable<Venue>) results[0]).iterator();
		while(iterator.hasNext()) {
			boolean found = true;
			Venue v = iterator.next();
			for (int i = 1; i < keywords.length; i++) {
				if (!search(v, ((Iterable<Venue>) results[i]))) {
					found = false;
					break;
				}
			}
			if (found) {
				returnList.add(v);
			}
		}
		return returnList;
	}
	
	private boolean search(Venue v, Iterable<Venue> list) {
		Iterator<Venue> iterator = list.iterator();
		while(iterator.hasNext()) {
			if (iterator.next() == v) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Iterable<Venue> findAllMultipleKeywordsUsingRegex(String name){
		String[] keywords = name.split("\\s+");
		if (keywords.length == 0) return new ArrayList<Venue>();
		
		Iterable<Venue> returnList = findAllSingleKeyword(keywords[0]);
		for (int i = 1; i < keywords.length; i++) {
			Iterator<Venue> iterator = returnList.iterator();
			while(iterator.hasNext()) {
				Venue v = iterator.next();
				if (!findKeyword(v.getName(), keywords[i])) {
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

}
