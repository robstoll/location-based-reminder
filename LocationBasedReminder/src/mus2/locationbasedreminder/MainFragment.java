package mus2.locationbasedreminder;

import java.util.Collection;

import mus2.locationbasedreminder.dal.IReminderPersistence;
import mus2.locationbasedreminder.dal.JsonFileReminderPersistence;
import mus2.locationbasedreminder.dto.ReminderItem;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainFragment extends Fragment {

	private IReminderPersistence persistence;
	private View rootView;
	
    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        persistence = new JsonFileReminderPersistence(getActivity());
        loadReminderItemsIntoListView();
		setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();
        return rootView;
    }
    
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
		menu.getItem(0).setVisible(true);
		menu.getItem(1).setVisible(false);
    	super.onPrepareOptionsMenu(menu);
    }
    
    private void loadReminderItemsIntoListView() {
    	Collection<ReminderItem> reminderItems = persistence.getAll();
    	String[] listItems = new String[reminderItems.size()];
    	
    	int i = 0;
    	for (ReminderItem reminderItem : reminderItems) {
			listItems[i] = reminderItem.getDescription();
			i++;
		}
    	ListView listView = (ListView) rootView.findViewById(R.id.listViewReminders);
    	
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(listView.getContext(), android.R.layout.simple_list_item_1, listItems);
    	listView.setAdapter(adapter);
    }
}