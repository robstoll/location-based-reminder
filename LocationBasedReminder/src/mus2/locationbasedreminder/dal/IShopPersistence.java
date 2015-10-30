package mus2.locationbasedreminder.dal;

import java.util.List;

import mus2.locationbasedreminder.dto.ShopItem;

public interface IShopPersistence {
	
	ShopItem load(String shopName, int id);
	
	List<ShopItem> load(String shopName);
	/**
	 * All existing entries shall be replaced by the given ones.
	 * 
	 * This method can be used to synchronise the shops with an external source
	 * @param shopItems The new shop items
	 */
	void ReplaceAll(List<ShopItem> shopItems);
}
