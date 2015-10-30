package mus2.locationbasedreminder.dto;

import com.google.android.gms.location.Geofence;

public class ReminderItem{
	public static int INVALID_ID = -1;

	private int id;
	private String shopName;
	private String title;
	private String description;
	private Double latitude;
	private Double longitude;
	private int radius;

	public ReminderItem(String title,String description, String shopName, int radius){
		super();
		this.id = INVALID_ID;
		this.title = title;
		this.description = description;
		this.shopName = shopName;
		this.radius = radius;
	}
	
	public ReminderItem(String title, String description, double latitude, double longitude, int radius) {
		super();
		this.id = INVALID_ID;
		this.title = title;
		this.description = description;
		this.latitude = latitude;
		this.longitude = longitude;
		this.radius = radius;
	}

	public ReminderItem() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getShopName() {
		return shopName;
	}

	public void setShopName(String shopName) {
		this.shopName = shopName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}
	
	/**
     * Creates a Location Services Geofence object from a
     * SimpleGeofence.
     *
     * @return A Geofence object
     */
    public Geofence toGeofence() {
        // Build a new Geofence object
        return new Geofence.Builder()
                .setRequestId(getId()+"")
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setCircularRegion(getLatitude(), getLongitude(), getRadius())
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }
}
