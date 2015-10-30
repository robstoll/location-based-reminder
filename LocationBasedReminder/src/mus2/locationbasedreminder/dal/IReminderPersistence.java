package mus2.locationbasedreminder.dal;

import java.util.Collection;

import mus2.locationbasedreminder.dto.ReminderItem;

public interface IReminderPersistence {
	int save(ReminderItem item);
	ReminderItem load(int id);
	Collection<ReminderItem> getAll();
	void delete(int reminderId);
}
