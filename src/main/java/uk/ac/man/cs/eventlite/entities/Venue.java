package uk.ac.man.cs.eventlite.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;


@Entity
@Table(name = "venues")
public class Venue {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;

	@NotEmpty(message = "Venue name is required.")
	@Size(max = 255, message = "Name length must not exceed 255 characters.")
	private String name;

	@Positive(message = "Must be positive integer.")
	private int capacity;

	@NotEmpty(message = "Address is required.")
	@Size(max = 299, message = "Road name must be less than 300 characters long.")
	private String address;

	@NotEmpty(message = "Postcode is required.")
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
	public void setAddress(String addr) {
		this.address = addr;
	}
	public String getPostcode() {
		return this.postcode;
	}
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
}
