package mus2.locationbasedreminder.bl;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

public class LocationManagerListenerService extends Service {

	private LocationManager locationManager;
	private Listener listener;

	private boolean isGpsEnabled;
	private boolean isNetEnabled;
	private boolean wasEnabled = true;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		execute();
		return START_STICKY;
	}

	private void execute() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		listener = new Listener();
		// we don't really need the location updates, we are only interested in the onProvider events
		// thus only every 1 hour and 5000m difference
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60*60*1000, 5000, listener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60*60*1000, 5000, listener);
	}

	@Override
	public void onDestroy() {
		locationManager.removeUpdates(listener);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private class Listener implements LocationListener {
		@Override
		public void onProviderDisabled(String provider) {
			if (LocationManager.GPS_PROVIDER.equals(provider)) {
				isGpsEnabled = false;
			} else if (LocationManager.NETWORK_PROVIDER.equals(provider)) {
				isNetEnabled = false;
			}
			
			if (!isGpsEnabled && !isNetEnabled && wasEnabled) {
				wasEnabled = false;
				//cannot remove Geofences if no provider is available - I suppose google handles this scenario already correct
//				Intent service = new Intent(LocationManagerListenerService.this, GeofenceHelperService.class);
//				service.setAction(GeofenceHelperService.EAction.REMOVE_ALL.name());
//				startService(service);
			}
		}

		@Override
		public void onProviderEnabled(String provider) {
			if (LocationManager.GPS_PROVIDER.equals(provider)) {
				isGpsEnabled = true;
			} else if (LocationManager.NETWORK_PROVIDER.equals(provider)) {
				isNetEnabled = true;
			}
			
			if ((isGpsEnabled || isNetEnabled) && !wasEnabled) {
				wasEnabled = true;
				//Wait 10 seconds in the case the user has to accept google apps settings
				try {
					Thread.sleep(10*1000L);
				} catch (InterruptedException e) {
					//we don't care
				}
				Intent service = new Intent(LocationManagerListenerService.this, GeofenceHelperService.class);
				service.setAction(GeofenceHelperService.EAction.ADD_ALL.name());
				startService(service);
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onLocationChanged(Location location) {
		}
	}
}
