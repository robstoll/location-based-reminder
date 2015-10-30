package mus2.locationbasedreminder;

import java.net.URI;
import java.net.URISyntaxException;

import mus2.locationbasedreminder.bl.GeofenceHelperService;
import mus2.locationbasedreminder.bl.LocationManagerHelper;
import mus2.locationbasedreminder.bl.LocationManagerListenerService;
import mus2.locationbasedreminder.bl.SyncShops;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity {

	private MainFragment mainFragment = new MainFragment();
	private PropertiesFragment propertiesFragment = new PropertiesFragment();
	private NewItemFragment newItemFragment = new NewItemFragment();
	private ReminderFragment reminderFragment = new ReminderFragment();
	private boolean isLocatorRunning = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			openAllItems();
		}

		startLocationManagerListener();
		startGeofenceAddAllService();
		syncShops();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Bundle bundle = getIntent().getExtras();
		if (bundle != null && bundle.containsKey(ReminderFragment.requestIdKey)) {
			openReminder();
		}
	}

	private void startLocationManagerListener() {
		Intent service = new Intent(this, LocationManagerListenerService.class);
		startService(service);
	}

	private void startGeofenceAddAllService() {
		if (LocationManagerHelper.isLocationServiceEnabled(this)) {
			Intent service = createGeofenceHelperService();
			service.setAction(GeofenceHelperService.EAction.ADD_ALL.name());
			startService(service);
			isLocatorRunning = true;
		} else {
			isLocatorRunning = false;
			new AlertDialog.Builder(this)
					.setMessage(getString(R.string.msg_locationservice_disabled))
					.setPositiveButton(getString(R.string.btn_yes_goto_locationservice_settings),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface paramDialogInterface, int paramInt) {
									openLocationSettings();
								}

							}).setNegativeButton(getString(R.string.btn_no_thanks), null).show();
		}

	}

	private Intent createGeofenceHelperService() {
		return new Intent(this, GeofenceHelperService.class);
	}

	private void openLocationSettings() {
		Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(myIntent);
	}

	private void syncShops() {
		try {
			new SyncShops(this).execute(new URI(getString(R.string.url_rest_service)));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private void startGeofenceRemoveAllService() {
		Intent service = createGeofenceHelperService();
		service.setAction(GeofenceHelperService.EAction.REMOVE_ALL.name());
		startService(service);
		isLocatorRunning = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.getItem(0).setVisible(true);
		menu.getItem(1).setVisible(false);
		menu.getItem(2).setEnabled(isLocatorRunning);
		menu.getItem(3).setEnabled(!isLocatorRunning);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.

		switch (item.getItemId()) {
			case R.id.action_settings:
				openSettings();
				return true;
			case R.id.action_add:
				openNewItem();
				return true;
			case R.id.action_cancel_service:
				startGeofenceRemoveAllService();
				return true;
			case R.id.action_start_service:
				startGeofenceAddAllService();
				return true;
			case R.id.action_location_settings:
				openLocationSettings();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void openSettings() {
		getSupportFragmentManager().beginTransaction().replace(R.id.container, propertiesFragment).addToBackStack(null)
				.commit();
	}

	private void openNewItem() {
		getSupportFragmentManager().beginTransaction().replace(R.id.container, newItemFragment).addToBackStack(null)
				.commit();
	}

	private void openAllItems() {
		getSupportFragmentManager().beginTransaction().replace(R.id.container, mainFragment).commit();
	}

	private void openReminder() {
		getSupportFragmentManager().beginTransaction().replace(R.id.container, reminderFragment).commit();
	}
}
