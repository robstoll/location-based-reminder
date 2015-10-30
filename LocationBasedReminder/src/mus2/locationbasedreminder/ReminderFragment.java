package mus2.locationbasedreminder;

import java.util.Locale;

import mus2.locationbasedreminder.bl.GeofenceHelperService;
import mus2.locationbasedreminder.dal.IReminderPersistence;
import mus2.locationbasedreminder.dal.IShopPersistence;
import mus2.locationbasedreminder.dal.JsonFileReminderPersistence;
import mus2.locationbasedreminder.dal.JsonFileShopPersistence;
import mus2.locationbasedreminder.dto.ReminderItem;
import mus2.locationbasedreminder.dto.ShopItem;
import mus2.locationbasedreminder.dto.ShopReminderId;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class ReminderFragment extends Fragment implements OnClickListener {

	private View rootView;

	private double latitude;
	private double longitude;

	public static String requestIdKey = "requestId";

	@Override
    public void onPrepareOptionsMenu(Menu menu) {
    	menu.getItem(0).setVisible(false);
    	menu.getItem(1).setVisible(false);
    	super.onPrepareOptionsMenu(menu);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final FragmentActivity activity = getActivity();

		Bundle bundle = activity.getIntent().getExtras();
		if (bundle != null && bundle.containsKey(requestIdKey)) {
			String requestId = (String) bundle.get(requestIdKey);
			if (isNotShopReminder(requestId)) {
				handleNormalReminder(inflater, container, requestId);
			} else {
				handleShopReminder(inflater, container, requestId);
			}

		} else {
			new AlertDialog.Builder(activity)
					.setMessage(getString(R.string.msg_unexpected_error))
					.setPositiveButton(getString(R.string.btn_ok),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface paramDialogInterface, int paramInt) {
									activity.finish();
								}

							}).show();
		}

		setHasOptionsMenu(true);
		getActivity().invalidateOptionsMenu();
        
		return rootView;
	}

	private void handleNormalReminder(LayoutInflater inflater, ViewGroup container, String requestId) {
		int reminderId = Integer.parseInt(requestId);
		IReminderPersistence persistence = new JsonFileReminderPersistence(getActivity());
		ReminderItem reminderItem = persistence.load(reminderId);
		if (reminderItem != null) {
			latitude = reminderItem.getLatitude();
			longitude = reminderItem.getLongitude();
			initLayout(inflater, container,
					reminderItem.getTitle(),
					reminderItem.getDescription(),
					Double.toString(latitude),
					Double.toString(longitude));
			persistence.delete(reminderItem.getId());
		}
	}

	private void initLayout(LayoutInflater inflater, ViewGroup container, String title, String description,
			String latitude, String longitude) {
		rootView = inflater.inflate(R.layout.fragment_reminder, container, false);
		setTextForTextView(R.id.txtTitle, title);
		setTextForTextView(R.id.txtDescription, description);
		setTextForTextView(R.id.txtLatitude, latitude);
		setTextForTextView(R.id.txtLongitude, longitude);
		rootView.findViewById(R.id.btnOpenGoogleMaps).setOnClickListener(this);
	}

	private void handleShopReminder(LayoutInflater inflater, ViewGroup container, String requestId) {
		ShopReminderId shopReminderId = ShopReminderId.splitGeofenceId(requestId);
		IShopPersistence shopPersistence = new JsonFileShopPersistence(getActivity());
		ShopItem shopItem = shopPersistence.load(shopReminderId.getShopName(), shopReminderId.getShopId());
		if (shopItem != null) {
			IReminderPersistence reminderPersistence = new JsonFileReminderPersistence(getActivity());
			latitude = shopItem.getLatitude();
			longitude = shopItem.getLongitude();
			ReminderItem reminderItem = reminderPersistence.load(shopReminderId.getReminderId());
			if (reminderItem != null) {
				initLayout(
						inflater, container, reminderItem.getTitle() + " - " + shopItem.getName(),
						shopItem.getAddress() + ", " + shopItem.getZip() + " " + shopItem.getCity()
								+ "\n" + reminderItem.getDescription(),
						Double.toString(latitude),
						Double.toString(longitude));

				Intent intent = new Intent(getActivity(), GeofenceHelperService.class);
				intent.setAction(GeofenceHelperService.EAction.CANCEL_SHOP.name());
				getActivity().startService(intent);
				reminderPersistence.delete(reminderItem.getId());
			}
		}
	}

	private boolean isNotShopReminder(String requestId) {
		return !requestId.contains("_");
	}

	private void setTextForTextView(int id, String text) {
		TextView txtView = (TextView) rootView.findViewById(id);
		txtView.setText(text);
	}

	@Override
	public void onClick(View v) {
		String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f", latitude, longitude, latitude, longitude);
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
		getActivity().startActivity(intent);

	}
}
