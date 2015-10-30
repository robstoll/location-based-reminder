package mus2.locationbasedreminder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class PropertiesFragment extends Fragment {

	private Button save = null;
	private Button cancel = null;
	private EditText radius = null;
	private CheckBox allInOne = null;
	private View rootView = null;
	
	private SharedPreferences preferences;
	private int startRadius;
	private boolean startAllInOne;
	
	private static String SHARED_PREFERENCES_RADIUS = "LocationBasedReminder_Radius";
	private static String SHARED_PREFERENCES_ALLINONE = "LocationBasedReminder_AllInOne";
	
    public PropertiesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_properties, container, false);
        this.radius = (EditText)rootView.findViewById(R.id.txtRadius);
        this.allInOne = (CheckBox)rootView.findViewById(R.id.chkAllinOne);
        this.save = (Button)rootView.findViewById(R.id.btnSave);
        this.cancel = (Button)rootView.findViewById(R.id.btnCancel);
        
        preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        startRadius = preferences.getInt(SHARED_PREFERENCES_RADIUS, 500);
        startAllInOne = preferences.getBoolean(SHARED_PREFERENCES_ALLINONE, true);
        
        this.radius.setText(String.valueOf(startRadius));
        this.allInOne.setChecked(startAllInOne);
        
        this.save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SharedPreferences.Editor editor = preferences.edit();
				int newRadius = Integer.parseInt(radius.getText().toString());
				boolean newAllInOne = allInOne.isChecked();
				editor.putInt(SHARED_PREFERENCES_RADIUS, newRadius);
				editor.putBoolean(SHARED_PREFERENCES_ALLINONE, newAllInOne);
				editor.commit();
				close();
			}
		});
        
        this.cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				radius.setText(String.valueOf(startRadius));
				allInOne.setChecked(startAllInOne);
				close();
			}
		});

		setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();
        
        return rootView;
    }
    
    private void close() {
    	getActivity().getSupportFragmentManager().popBackStack();
    }
    
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    	menu.getItem(0).setVisible(false);
    	menu.getItem(1).setVisible(false);
    	super.onPrepareOptionsMenu(menu);
    }
}