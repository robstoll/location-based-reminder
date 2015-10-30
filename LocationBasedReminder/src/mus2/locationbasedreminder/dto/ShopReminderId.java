package mus2.locationbasedreminder.dto;

public class ShopReminderId {
	private String shopName;
	private int shopId;
	private int reminderId;

	public ShopReminderId(String shopName, int shopId, int reminderId) {
		super();
		this.shopName = shopName;
		this.shopId = shopId;
		this.reminderId = reminderId;
	}
	
	public static ShopReminderId splitGeofenceId(String requestId){
		String[] split = requestId.split("_");
		return new ShopReminderId(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]));
	}

	public static String getGeofenceId(ReminderItem item, ShopItem shop) {
		return shop.getName() + "_" + shop.getId() + "_" + item.getId();
	}

	public String getShopName() {
		return shopName;
	}

	public void setShopName(String shopName) {
		this.shopName = shopName;
	}

	public int getShopId() {
		return shopId;
	}

	public void setShopId(int shopId) {
		this.shopId = shopId;
	}

	public int getReminderId() {
		return reminderId;
	}

	public void setReminderId(int reminderId) {
		this.reminderId = reminderId;
	}
	
	
}
