package mus2.locationbasedreminder.bl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocationManagerListenerSetter extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context, LocationManagerListenerService.class);
		context.startService(service);
	}

}
