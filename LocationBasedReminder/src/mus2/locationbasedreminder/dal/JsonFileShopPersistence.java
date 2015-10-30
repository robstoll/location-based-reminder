package mus2.locationbasedreminder.dal;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mus2.locationbasedreminder.dto.ShopItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class JsonFileShopPersistence implements IShopPersistence{
	
	private String filename = "locationbasedreminder_shops";
	
	private static String shopId = "id";
	private static String shopItems = "shopItems";
	private static String shopName = "name";
	private static String address = "address";
	private static String zip = "zip";
	private static String latitude = "latitude";
	private static String longitude = "longitude";
	private static String city = "city";
	private Context context;
	
	public JsonFileShopPersistence(Context context){
		this.context = context;
	}
	
	private static Map<String, List<ShopItem>> cache = null;
	
	private void fillChache() {
		cache = new HashMap<String, List<ShopItem>>();
		FileInputStream inputStream = null;
		try {
			inputStream = context.openFileInput(filename);
			JSONObject jObject = JsonHelper.InputStreamtoJson(inputStream);
			JSONArray jArray = jObject.getJSONArray(shopItems);
			for (int i = 0; i < jArray.length(); i++) {
				ShopItem shopItem = new ShopItem();
				JSONObject jsonItem = jArray.getJSONObject(i);
				shopItem.setAddress(jsonItem.getString(address));
				shopItem.setLatitude(jsonItem.getDouble(latitude));
				shopItem.setLongitude(jsonItem.getDouble(longitude));
				String name = jsonItem.getString(shopName);
				shopItem.setName(name);
				shopItem.setCity(jsonItem.getString(city));
				shopItem.setId(jsonItem.getInt(shopId));
				shopItem.setZip(jsonItem.getString(zip));
				if (!cache.containsKey(name))
				{
					cache.put(name, new ArrayList<ShopItem>());
				}
				cache.get(name).add(shopItem);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void writeBackCache() {
		FileOutputStream outputStream = null;
		try {
			outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

			JSONObject jObject = new JSONObject();
			JSONArray jArray = new JSONArray();

			Iterator<Entry<String, List<ShopItem>>> it = cache.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, List<ShopItem>> pairs = (Map.Entry<String, List<ShopItem>>)it.next();
				List<ShopItem> value = pairs.getValue();
				for (ShopItem item : value) {
					JSONObject jsonItem = new JSONObject();
					jsonItem.put(address, item.getAddress());
					jsonItem.put(latitude, item.getLatitude());
					jsonItem.put(longitude, item.getLongitude());
					jsonItem.put(shopName, item.getName());
					jsonItem.put(shopId, item.getId());
					jsonItem.put(city, item.getCity());
					jsonItem.put(zip, item.getZip());
					jArray.put(jsonItem);
				}
			}

			jObject.put(shopItems, jArray);

			writer.write(jObject.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public ShopItem load(String shopName, int id) {
		for (ShopItem item : load(shopName)) {
			if (item.getId() == id) {
				return item;
			}
		}
		return null;
	}
	
	@Override
	public List<ShopItem> load(String shopName) {
		if (cache == null) {
			fillChache();
		}
		
		return cache.get(shopName);
	}

	@Override
	public void ReplaceAll(List<ShopItem> shopItems) {
		if(cache == null){
			cache = new HashMap<String, List<ShopItem>>();
		}
		cache.clear();
		int i = 0;
		for (ShopItem item : shopItems) {
			if (!cache.containsKey(item.getName())) {
				cache.put(item.getName(), new ArrayList<ShopItem>());
			}
			item.setId(i);
			cache.get(item.getName()).add(item);
			i++;
		}
		writeBackCache();
	}

	

}
