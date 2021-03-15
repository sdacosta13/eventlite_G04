package uk.ac.man.cs.eventlite.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "venues")
public class Venue {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;

	private String name;

	private int capacity;
	
	private String address;
	
	private String postcode;
	public Venue() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	public String getAddress() {
		return this.address;
	}
	public void setAddressLine1(String addr) {
		this.address = addr;
	}
	public String getPostcode() {
		return this.postcode;
	}
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
}
