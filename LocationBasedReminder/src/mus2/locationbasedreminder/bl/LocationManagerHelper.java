package mus2.locationbasedreminder.bl;

import android.content.Context;
import android.location.LocationManager;

public class LocationManagerHelper {
	private LocationManagerHelper(){}
	
	public static boolean isLocationServiceEnabled(Context context){
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		boolean gps_enabled=false;
		boolean network_enabled=false;
		try {
			gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
			//we don't bother, we just want to know if it is enabled, exception means not enabled
		}
		try {
			network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception ex) {
			//we don't bother, we just want to know if it is enabled, exception means not enabled
		}
		return gps_enabled || network_enabled;		
	}
}
