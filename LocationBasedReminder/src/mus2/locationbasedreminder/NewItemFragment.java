package mus2.locationbasedreminder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mus2.locationbasedreminder.bl.GeofenceHelperService;
import mus2.locationbasedreminder.bl.GeofenceHelperService.EAction;
import mus2.locationbasedreminder.bl.ISelectSpeechChanged;
import mus2.locationbasedreminder.bl.ProcessSpeech;
import mus2.locationbasedreminder.dal.IReminderPersistence;
import mus2.locationbasedreminder.dal.JsonFileReminderPersistence;
import mus2.locationbasedreminder.dto.ReminderItem;
import mus2.locationbasedreminder.dto.SpeechResult;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class NewItemFragment extends Fragment implements OnClickListener, ISelectSpeechChanged, OnMapClickListener, OnMapLongClickListener {
	protected static final int REQUEST_OK = 1;

	private View rootView;
	private SpeechResult speechResult = null;
	private Button btnDescription = null;
	private Button btnLocation = null;
	private ImageButton btnSpeechRecognition = null;
	private ImageButton btnSpeechRecognitionLocation = null;
	private ImageButton btnSpeechRecognitionDescription = null;
	private TextView txtDescription = null;
	private TextView txtLocation = null;
	private GoogleMap map = null;
	
	private SharedPreferences preferences;
	
	private enum CurrentButton {
		location,
		description
	}
	
	private enum CurrentSpeechRecognitionMode {
		location,
		description,
		allInOne
	}
	
	private CurrentButton currentButton;
	private CurrentSpeechRecognitionMode currentSpeechRecognitionMode;
	
	private IReminderPersistence persistence;
	
	private Geocoder coder;

	private double longitude;
	private double latitude;
	
	private static String SHARED_PREFERENCES_RADIUS = "LocationBasedReminder_Radius";
	private static String SHARED_PREFERENCES_ALLINONE = "LocationBasedReminder_AllInOne";
	
	public NewItemFragment() {
	}
	
	@Override
    public void onPrepareOptionsMenu(Menu menu) {
		menu.getItem(0).setVisible(false);
		menu.getItem(1).setVisible(true);
    	super.onPrepareOptionsMenu(menu);
    }

	@Override
	public void onDestroyView() {
		   super.onDestroyView(); 
		   Fragment fragment = (getFragmentManager().findFragmentById(R.id.map));   
		   getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_new_item, container, false);
		btnDescription = (Button)rootView.findViewById(R.id.btnDescription);
		btnLocation = (Button)rootView.findViewById(R.id.btnLocation);
		btnSpeechRecognition = (ImageButton)rootView.findViewById(R.id.btnSpeechRecognition);
		btnSpeechRecognitionLocation = (ImageButton)rootView.findViewById(R.id.btnSpeechRecognitionLocation);
		btnSpeechRecognitionDescription = (ImageButton)rootView.findViewById(R.id.btnSpeechRecognitionDescription);
		txtDescription = (TextView)rootView.findViewById(R.id.txtDescription);
		txtLocation = (TextView)rootView.findViewById(R.id.txtLocation);
		map = ((SupportMapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
			map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(48.305047,14.286958)));
			map.animateCamera(CameraUpdateFactory.zoomTo(15));
			map.setOnMapClickListener(this);
	        map.setOnMapLongClickListener(this);
		
		preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
		
		btnSpeechRecognition.setOnClickListener(this);
		
		final NewItemFragment fragment = this;
		
		btnSpeechRecognitionDescription.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startSpeechRecognition(CurrentSpeechRecognitionMode.description);
			}
		});
		
		btnSpeechRecognitionLocation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startSpeechRecognition(CurrentSpeechRecognitionMode.location);
			}
		});
		
		btnDescription.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				currentButton = CurrentButton.description;
				SelectSpeechFragment selectSpeech = new SelectSpeechFragment();
				selectSpeech.setTitle("Select Description");
				selectSpeech.setItems(speechResult.getDescriptions().toArray(new String[speechResult.getDescriptions().size()]));
				selectSpeech.setTargetFragment(fragment, REQUEST_OK);
				selectSpeech.show(getFragmentManager(), "selectDescription");
			}
		});
		
		btnLocation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				currentButton = CurrentButton.location;
				SelectSpeechFragment selectSpeech = new SelectSpeechFragment();
				selectSpeech.setTitle("Select Location");
				selectSpeech.setItems(speechResult.getLocations().toArray(new String[speechResult.getDescriptions().size()]));
				selectSpeech.setTargetFragment(fragment, REQUEST_OK);
				selectSpeech.show(getFragmentManager(), "selectLocation");
			}
		});
		
		txtLocation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					changeLocation(txtLocation.getText().toString());
				}
			}
		});
		
		btnDescription.setEnabled(false);
		btnLocation.setEnabled(false);
	
		boolean allInOne = preferences.getBoolean(SHARED_PREFERENCES_ALLINONE, true);
		if (allInOne) {
			btnSpeechRecognition.setVisibility(View.VISIBLE);
			btnSpeechRecognitionDescription.setVisibility(View.GONE);
			btnSpeechRecognitionLocation.setVisibility(View.GONE);
		}
		else {
			btnSpeechRecognition.setVisibility(View.GONE);
			btnSpeechRecognitionDescription.setVisibility(View.VISIBLE);
			btnSpeechRecognitionLocation.setVisibility(View.VISIBLE);
		}

		this.persistence = new JsonFileReminderPersistence(getActivity());
		
		coder = new Geocoder(getActivity());
		
		speechResult = new SpeechResult();
		
		setHasOptionsMenu(true);
		getActivity().invalidateOptionsMenu();
		
		
		return rootView;
	}
	
	@Override
	public void onSelectionChanged(String selectedItem) {
		if (selectedItem != null) {
			switch (currentButton) {
			case location:
				changeLocation(selectedItem);
				break;
			case description: 
				txtDescription.setText(selectedItem);
				break;
			}
		}
	}

	@Override
	public void onClick(View v) {
		startSpeechRecognition(CurrentSpeechRecognitionMode.allInOne);
	}
	
	private void startSpeechRecognition(CurrentSpeechRecognitionMode currentSpeechRecognitionMode) {
		this.currentSpeechRecognitionMode = currentSpeechRecognitionMode;
		Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "de-AT");
		try {
			startActivityForResult(i, REQUEST_OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_OK) {
			ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (thingsYouSaid.size() > 0) {
				switch (currentSpeechRecognitionMode) {
				case location:
					speechResult.addLocations(thingsYouSaid);
					break;
				case description:
					speechResult.addDescriptions(thingsYouSaid);
					break;
				case allInOne:
					ProcessSpeech speechProcessor = new ProcessSpeech(thingsYouSaid);
					speechResult = speechProcessor.getSpeechResult();
					break;
				}
				if (speechResult.getDescriptions().size() > 0) {
					txtDescription.setText(speechResult.getDescriptions().get(0));
					btnDescription.setEnabled(true);
				}
				else {
					btnDescription.setEnabled(false);
				}
				if (speechResult.getLocations().size() > 0) {
					changeLocation(speechResult.getLocations().get(0));
					btnLocation.setEnabled(true);
				}
				else {
					btnLocation.setEnabled(false);
				}
			}
			else {
				btnDescription.setEnabled(false);
				btnLocation.setEnabled(false);
				new AlertDialog.Builder(getActivity())
				.setMessage("Sorry, I did not understand you. Please try again or type in the text manually.")
				.setPositiveButton("Try again", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						startSpeechRecognition(currentSpeechRecognitionMode);
					}
				})
				.setNegativeButton("Type text", null).show();
			}
		}
	}
	
	private void changeLocation(String locationText) {
		txtLocation.setText(locationText);
		map.clear();
		if (locationText != "Billa") {
			try {
				List<Address> addresses = coder.getFromLocationName(txtLocation.getText().toString(), 1);
				if (addresses != null && addresses.size() > 0) {
					navigateMapToAddress(addresses.get(0));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void navigateMapToAddress(Address address) {
		latitude = address.getLatitude();
		longitude = address.getLongitude();
		LatLng position = new LatLng(latitude, longitude);
		
		map.addMarker(new MarkerOptions().position(position).title(getMarkerTitle(address)));
		map.moveCamera(CameraUpdateFactory.newLatLng(position));
		map.animateCamera(CameraUpdateFactory.zoomTo(15));
	}

	private String getMarkerTitle(Address address) {
		int max = address.getMaxAddressLineIndex();
		StringBuilder sbTitle = new StringBuilder();
		for(int i=0; i<max; ++i){
			if(i!=0){
				sbTitle.append(", ");
			}
			sbTitle.append(address.getAddressLine(i));				
		}
		return sbTitle.toString();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_save) {
	        int radius = preferences.getInt(SHARED_PREFERENCES_RADIUS, 500);
			ReminderItem reminderItem = null;
			Intent intent = new Intent(getActivity(), GeofenceHelperService.class);
			if (txtLocation.getText().toString() == "Billa") {
				reminderItem = new ReminderItem(getString(R.string.default_title), txtDescription.getText().toString(), "Billa", radius);
				intent.setAction(EAction.CREATE_SHOP.name());
			}
			else {
				reminderItem = new ReminderItem(getString(R.string.default_title), txtDescription.getText().toString(), latitude, longitude, radius);
				intent.setAction(EAction.CREATE.name());
			}
			if (reminderItem != null){
				int id = persistence.save(reminderItem);
				intent.putExtra(GeofenceHelperService.REMINDER_ID, id);
				getActivity().startService(intent);
			}
			
			close();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void close() {
    	getActivity().getSupportFragmentManager().popBackStack();
    }

	@Override
	public void onMapLongClick(LatLng point) {
		try {
			List<Address> addresses = coder.getFromLocation(point.latitude, point.longitude, 1);
			if (addresses != null && addresses.size() > 0) {
				Address address = addresses.get(0);
				navigateMapToAddress(address);
				txtLocation.setText(getMarkerTitle(address));
}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onMapClick(LatLng point) {
		//only support long click
	}
}
