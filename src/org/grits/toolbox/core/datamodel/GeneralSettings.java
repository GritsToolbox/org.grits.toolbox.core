package org.grits.toolbox.core.datamodel;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="settings")
public class GeneralSettings {
	
	public static final String SHOWINFO_SETTING = "GRITS Workflow Dialog";

	List<SettingEntry> hiddenDialogs = new ArrayList<>();
	
	@XmlElement(name="hiddenDialog")
	public List<SettingEntry> getHiddenDialogs() {
		return hiddenDialogs;
	}
	
	public void setHiddenDialogs(List<SettingEntry> hiddenDialogs) {
		this.hiddenDialogs = hiddenDialogs;
	}
	
	public void addHiddenDialog (SettingEntry e) {
		if (!hiddenDialogs.contains(e))
			this.hiddenDialogs.add(e);
	}

	public void removeHiddenDialog(SettingEntry se) {
		SettingEntry toBeRemoved = null;
		for (SettingEntry e: hiddenDialogs) {
			if (e.getId().equals(se.getId())) {
				toBeRemoved = e;
				break;
			}
		}
		if (toBeRemoved != null)
			hiddenDialogs.remove(toBeRemoved);
	}

	public boolean isHiddenDialog(String settingId) {
		for (SettingEntry e: hiddenDialogs) 
			if (e.getId().equals(settingId)) 
				return true;
		return false;
	}
}
