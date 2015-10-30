package mus2.locationbasedreminder.dto;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SpeechResult {
	private Set<String> locations;
	private Set<String> descriptions;
	
	public SpeechResult() {
		this.locations = new LinkedHashSet<String>();
		this.descriptions = new LinkedHashSet<String>();
	}
	
	public List<String> getLocations() {
		return new ArrayList<String>(locations);
	}
	
	public List<String> getDescriptions() {
		return new ArrayList<String>(descriptions);
	}
	
	public void addLocation(String location) {
		if (!location.isEmpty())
			this.locations.add(location);
	}
	
	public void addDescription(String description) {
		if (!description.isEmpty())
			this.descriptions.add(description);
	}
	
	public void addLocations(List<String> locations) {
		locations.remove("");
		this.locations.addAll(locations);
	}
	
	public void addDescriptions(List<String> descriptions) {
		descriptions.remove("");
		this.descriptions.addAll(descriptions);
	}
}
