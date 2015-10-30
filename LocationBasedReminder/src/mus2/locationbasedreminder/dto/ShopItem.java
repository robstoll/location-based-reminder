package mus2.locationbasedreminder.dto;

public class ShopItem {
	private int id;
	private String name;
	private String address;
	private String zip;
	private String city;
	private double latitude;
	private double longitude;
	
	public ShopItem(){}
	
	public ShopItem(String name, String address, String zip, String city, double latitude, double longitude) {
		super();
		this.name = name;
		this.address = address;
		this.zip = zip;
		this.city = city;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

}
