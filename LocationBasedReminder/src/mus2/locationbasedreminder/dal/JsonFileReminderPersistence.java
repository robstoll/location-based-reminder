package mus2.locationbasedreminder.dal;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import mus2.locationbasedreminder.dto.ReminderItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class JsonFileReminderPersistence implements IReminderPersistence {

	private String filename = "locationbasedreminder_reminder";

	private static String reminderItems = "reminderItems";
	private static String itemId = "id";
	private static String shopName = "shopName";
	private static String title = "title";
	private static String description = "description";
	private static String latitude = "latitude";
	private static String longitude = "longitude";
	private static String radius = "radius";
	
	private Context context;
	
	public JsonFileReminderPersistence(Context context) {
		this.context = context;
	}

	private static Map<Integer, ReminderItem> cache = null;

	private void fillCache() {
		cache = new HashMap<Integer, ReminderItem>();
		
		ReminderItem item = new ReminderItem("test", "lüfteneggerstrasse", 48.308966, 14.294656, 500);
		item.setId(1);
		cache.put(1, item);
	
		FileInputStream inputStream = null;
		try {
			inputStream = context.openFileInput(filename);
			JSONObject jObject = JsonHelper.InputStreamtoJson(inputStream);
			if(jObject!=null){
	
				JSONArray jArray = jObject.getJSONArray(reminderItems);
				for (int i = 0; i < jArray.length(); i++) {
					ReminderItem reminderItem = new ReminderItem();
					JSONObject jsonItem = jArray.getJSONObject(i);
					int id = jsonItem.getInt(itemId);
					reminderItem.setId(id);
					reminderItem.setShopName(jsonItem.getString(shopName));
					reminderItem.setTitle(jsonItem.getString(title));
					reminderItem.setDescription(jsonItem.getString(description));
					reminderItem.setLatitude(jsonItem.getInt(latitude));
					reminderItem.setLongitude(jsonItem.getInt(longitude));
					reminderItem.setRadius(jsonItem.getInt(radius));
	
					cache.put(id, reminderItem);
				}
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

			for (ReminderItem item : cache.values()) {
				JSONObject jsonItem = new JSONObject();
				jsonItem.put(itemId, item.getId());
				jsonItem.put(shopName, item.getShopName());
				jsonItem.put(title, item.getTitle());
				jsonItem.put(description, item.getDescription());
				jsonItem.put(latitude, item.getLatitude());
				jsonItem.put(longitude, item.getLongitude());
				jsonItem.put(radius, item.getRadius());
				jArray.put(jsonItem);
			}

			jObject.put(reminderItems, jArray);

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

	private int getNextId() {
		if(cache.size() != 0){
			return Collections.max(cache.keySet()) + 1;
		}else{
			return 1;
		}
	}

	@Override
	public int save(ReminderItem item) {
		int id = ReminderItem.INVALID_ID;

		if (cache == null) {
			fillCache();
		}

		if (item.getId() != ReminderItem.INVALID_ID) {
			id = item.getId();
			cache.put(id, item);
		} else {
			id = getNextId();
			item.setId(id);
			cache.put(id, item);
		}

		writeBackCache();

		return id;
	}

	@Override
	public ReminderItem load(int id) {

		ReminderItem reminderItem = null;

		if (cache == null) {
			fillCache();
		}

		if (cache.containsKey(id)) {
			reminderItem = cache.get(id);
		}

		return reminderItem;
	}

	@Override
	public Collection<ReminderItem> getAll() {
		if (cache == null) {
			fillCache();
		}

		return cache.values();
	}

	@Override
	public void delete(int reminderId) {
		cache.remove(reminderId);
		writeBackCache();
	}

}
