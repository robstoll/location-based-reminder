package mus2.locationbasedreminder.dal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonHelper {
	public static JSONObject InputStreamtoJson(InputStream inputStream) throws IOException, JSONException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}
		String s = sb.toString();
		if(!s.equals("")){
			return new JSONObject(sb.toString());
		}else{
			return null;
		}
	}
}
