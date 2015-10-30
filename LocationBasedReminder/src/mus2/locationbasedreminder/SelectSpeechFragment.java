
package mus2.locationbasedreminder;

import mus2.locationbasedreminder.bl.ISelectSpeechChanged;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class SelectSpeechFragment extends DialogFragment {
	
	private String title = null;
	private String[] items = null;
	
	public SelectSpeechFragment() {
		
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setItems(String[] items) {
		this.items = items;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(title).setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				((ISelectSpeechChanged)getTargetFragment()).onSelectionChanged(items[id]);
			}
		});
		return builder.create();
	}
}
