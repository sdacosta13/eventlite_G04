package uk.ac.man.cs.eventlite.entities;

import java.io.IOException;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import retrofit2.Response;


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
	
	private double latitude;
	
	private double longitude;
	
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
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return this.latitude;
	}
	public double getLongitude() {
		return this.longitude;
	}
	public boolean setCoords(){
		MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
				.accessToken("pk.eyJ1Ijoic2RhY29zdGExMyIsImEiOiJja21wMm45bzIyYWhkMnBwZnQ1Yzg0Zm8xIn0.mbzmSCCHSGvuxW3_DCxJYg")
				.query(this.getAddress() + " " + this.getPostcode()).build();
		try {
			Response<GeocodingResponse> response = mapboxGeocoding.executeCall();
			List<CarmenFeature> results = response.body().features();
			if (results.size() > 0) {
				// Log the first results Point.
				Point firstResultPoint = results.get(0).center();
				this.setLongitude(firstResultPoint.longitude());
				this.setLatitude(firstResultPoint.latitude());
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			System.out.println("IO error occured");
			return false;
		}
	}
}
