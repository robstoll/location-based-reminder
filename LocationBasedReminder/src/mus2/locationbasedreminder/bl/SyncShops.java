package mus2.locationbasedreminder.bl;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import mus2.locationbasedreminder.dal.IShopPersistence;
import mus2.locationbasedreminder.dal.JsonFileShopPersistence;
import mus2.locationbasedreminder.dal.JsonHelper;
import mus2.locationbasedreminder.dto.ShopItem;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

public class SyncShops extends AsyncTask<URI, Void, Void> {

	private static String latitude = "latitude";
	private static String longitude = "longitude";
	private static String address = "address";
	private static String zip = "zip";
	private static String city = "city";

	private Context context;
	
	public SyncShops(Context context){
		this.context = context;
	}
	
	protected Void doInBackground(URI... uris) {
		URI uri = uris[0];
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(uri);
			request.addHeader("Accept", "json/application");
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				JSONObject jObject = JsonHelper.InputStreamtoJson(response.getEntity().getContent());
				if(jObject != null){
					JSONArray jArray = jObject.getJSONArray("billa");
					List<ShopItem> shopItems = new ArrayList<ShopItem>();
					for (int i = 0; i < jArray.length(); ++i) {
						JSONObject jsonItem = jArray.getJSONObject(i);
						shopItems.add(new ShopItem(
								"billa",
								jsonItem.getString(address),
								jsonItem.getString(zip),
								jsonItem.getString(city),
								jsonItem.getDouble(latitude),
								jsonItem.getDouble(longitude)
								));
					}
					IShopPersistence shopPersistence = new JsonFileShopPersistence(context);
					shopPersistence.ReplaceAll(shopItems);
				}
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
