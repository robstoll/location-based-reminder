package mus2.locationbasedreminder.bl;

import java.util.List;

import mus2.locationbasedreminder.MainActivity;
import mus2.locationbasedreminder.R;
import mus2.locationbasedreminder.ReminderFragment;
import mus2.locationbasedreminder.dal.IReminderPersistence;
import mus2.locationbasedreminder.dal.IShopPersistence;
import mus2.locationbasedreminder.dal.JsonFileReminderPersistence;
import mus2.locationbasedreminder.dal.JsonFileShopPersistence;
import mus2.locationbasedreminder.dto.ReminderItem;
import mus2.locationbasedreminder.dto.ShopItem;
import mus2.locationbasedreminder.dto.ShopReminderId;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

public class ReceiveTransitionsIntentService extends IntentService {

	/**
	 * Sets an identifier for the service
	 */
	public ReceiveTransitionsIntentService() {
		super("ReceiveTransitionsIntentService");
	}

	/**
	 * Handles incoming intents
	 * 
	 * @param intent
	 *            The Intent sent by Location Services. This Intent is provided
	 *            to Location Services (inside a PendingIntent) when you call
	 *            addGeofences()
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		if (!LocationClient.hasError(intent)) {
			int transitionType = LocationClient.getGeofenceTransition(intent);
			if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
				List<Geofence> triggerList = LocationClient.getTriggeringGeofences(intent);
				IReminderPersistence reminderPersistence = new JsonFileReminderPersistence(this);
				IShopPersistence shopPersistence = new JsonFileShopPersistence(this);
				for (int i = 0; i < triggerList.size(); ++i) {
					String requestId = triggerList.get(i).getRequestId();
					if (isNotShopReminder(requestId)) {
						handleNormalGeofence(reminderPersistence, requestId);
					} else {
						handleShopGeofence(reminderPersistence, requestId, shopPersistence);
					}
				}
			} else {
				Log.e("ReceiveTransitionsIntentService", "Geofence transition error: " + transitionType);
			}
		} else {
			Log.e("ReceiveTransitionsIntentService", "Location Services error: " + LocationClient.getErrorCode(intent));

		}
	}

	private void handleNormalGeofence(IReminderPersistence persistence, String requestId) {
		try {
			int id = Integer.parseInt(requestId);
			ReminderItem reminderItem = persistence.load(id);
			if (reminderItem != null) {
				createNotification(reminderItem.getTitle(),
						reminderItem.getDescription(),
						requestId,
						id);
			}
		} catch (NumberFormatException ex) {
			Log.e("ReceiveTransitionsIntentService", "Could not convert requestId into an int", ex);
		}
	}

	private void handleShopGeofence(IReminderPersistence persistence, String requestId, IShopPersistence shopPersistence) {
		try {
			ShopReminderId shopReminderId = ShopReminderId.splitGeofenceId(requestId);
			ShopItem shopItem = shopPersistence.load(shopReminderId.getShopName(), shopReminderId.getShopId());
			if (shopItem != null) {
				int id = Integer.parseInt(requestId);
				ReminderItem reminderItem = persistence.load(id);
				if (reminderItem != null) {
					createNotification(
							reminderItem.getTitle() + " - " + shopItem.getName(),
							reminderItem.getDescription(),
							requestId,
							id);
				}
			}
		} catch (NumberFormatException ex) {
			Log.e("ReceiveTransitionsIntentService", "Could not convert requestId into an int", ex);
		}
	}

	private boolean isNotShopReminder(String requestId) {
		return !requestId.contains("_");
	}

	private void createNotification(String title, String description, String requestId, int reminderId) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(ReminderFragment.requestIdKey, requestId);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(title)
				.setContentText(description)
				.setContentIntent(pendingIntent)
				.setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);

		notificationManager.notify("ReminderService", reminderId, builder.build());
	}

}
