package mus2.locationbasedreminder.bl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mus2.locationbasedreminder.dal.IReminderPersistence;
import mus2.locationbasedreminder.dal.IShopPersistence;
import mus2.locationbasedreminder.dal.JsonFileReminderPersistence;
import mus2.locationbasedreminder.dal.JsonFileShopPersistence;
import mus2.locationbasedreminder.dto.ReminderItem;
import mus2.locationbasedreminder.dto.ShopItem;
import mus2.locationbasedreminder.dto.ShopReminderId;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;
import com.google.android.gms.location.LocationStatusCodes;

public class GeofenceHelperService extends IntentService implements GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, OnAddGeofencesResultListener,
		OnRemoveGeofencesResultListener {

	public static String REMINDER_ID = "reminderId";
	
	private LocationClient locationClient;
	private List<Geofence> geofencesToAdd;
	private List<String> geofenceIdsToRemove;
	private IntentFilter matcher;

	public enum EAction {
		ADD_ALL, REMOVE_ALL, CREATE, CANCEL, CREATE_SHOP, CANCEL_SHOP
	}

	private EAction currentAction;

	public GeofenceHelperService() {
		super("GeofenceHelperService");
		matcher = new IntentFilter();
		for (EAction action : EAction.values()) {
			matcher.addAction(action.name());
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (matcher.matchAction(intent.getAction())) {
			int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
			if (resp == ConnectionResult.SUCCESS) {

				IReminderPersistence persistence = new JsonFileReminderPersistence(this);
				currentAction = EAction.valueOf(intent.getAction());
				switch (currentAction) {
					case ADD_ALL:
					case REMOVE_ALL:
						addOrRemoveAll(persistence);
						break;
					case CREATE:
					case CANCEL:
					case CREATE_SHOP:
					case CANCEL_SHOP:
						createOrCancel(persistence, intent);
						break;
					default:
						Log.e("GeofenceHelperService", "not supported action: " + currentAction.name());
						break;
				}
			}
		}
	}

	private void createOrCancel(IReminderPersistence persistence, Intent intent) {
		int id = intent.getIntExtra(REMINDER_ID, ReminderItem.INVALID_ID);
		ReminderItem item = persistence.load(id);
		if (item != null) {
			switch (currentAction) {
			case CREATE:
				geofencesToAdd = new ArrayList<Geofence>();
				geofencesToAdd.add(item.toGeofence());
				break;
			case CANCEL:
				geofenceIdsToRemove = new ArrayList<String>();
				geofenceIdsToRemove.add(Integer.toString(id));
				break;
			case CREATE_SHOP:
			case CANCEL_SHOP:
				IShopPersistence shopPersistence = new JsonFileShopPersistence(this);
				List<ShopItem> shopItems = shopPersistence.load(item.getShopName());
				createOrCancelShop(item, shopItems);
				break;
			default:
				break;
			}
			connectLocationClient();
		}
	}

	private void createOrCancelShop(ReminderItem item, List<ShopItem> shopItems) {
		switch (currentAction) {
		case CREATE_SHOP:
			geofencesToAdd = new ArrayList<Geofence>();
			for (ShopItem shop : shopItems) {
				geofencesToAdd.add(CreateShopGeofence(item, shop));
			}
			break;
		case CANCEL_SHOP:
			geofenceIdsToRemove = new ArrayList<String>();
			for (ShopItem shop : shopItems) {
				geofenceIdsToRemove.add(shop.getName() + "_" + item.getId());
			}
			break;
		default:
			break;
		}
	}

	private Geofence CreateShopGeofence(ReminderItem item, ShopItem shop) {
		return new Geofence.Builder()
				.setRequestId(ShopReminderId.getGeofenceId(item, shop))
				.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
				.setCircularRegion(shop.getLatitude(), shop.getLongitude(), item.getRadius())
				.setExpirationDuration(Geofence.NEVER_EXPIRE)
				.build();
	}

	private void addOrRemoveAll(IReminderPersistence persistence) {
		Collection<ReminderItem> reminderItems = persistence.getAll();
		if (reminderItems.size() > 0) {
			switch (currentAction) {
			case ADD_ALL:
				geofencesToAdd = new ArrayList<Geofence>();
				for (ReminderItem reminderItem : reminderItems) {
					geofencesToAdd.add(reminderItem.toGeofence());
				}
				break;
			case REMOVE_ALL:
				geofenceIdsToRemove = new ArrayList<String>();
				for (ReminderItem reminderItem : reminderItems) {
					geofenceIdsToRemove.add(Integer.toString(reminderItem.getId()));
				}
			default:
				break;
			}
			connectLocationClient();
		}
	}

	private void connectLocationClient() {
		locationClient = new LocationClient(this, this, this);
		locationClient.connect();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Toast.makeText(this, "Connection Failure : " + connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		switch (currentAction) {
			case ADD_ALL:
			case CREATE:
			case CREATE_SHOP:
				Intent intent = new Intent(this, ReceiveTransitionsIntentService.class);
				PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				locationClient.addGeofences(geofencesToAdd, pendingIntent, this);
				break;
			case REMOVE_ALL:
			case CANCEL:
			case CANCEL_SHOP:
				locationClient.removeGeofences(geofenceIdsToRemove, this);
			default:
				Log.e("GeofenceHelperService", "not supported action: " + currentAction.name());
				break;
		}
	}

	@Override
	public void onDisconnected() {
		Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
		if (statusCode != LocationStatusCodes.SUCCESS) {
			Toast.makeText(this, "could not add geofences", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent pendingIntent) {
		if (statusCode != LocationStatusCodes.SUCCESS) {
			Toast.makeText(this, "could not remove geofences", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
		if (statusCode != LocationStatusCodes.SUCCESS) {
			Toast.makeText(this, "could not remove geofences", Toast.LENGTH_SHORT).show();
		}
	}

}
