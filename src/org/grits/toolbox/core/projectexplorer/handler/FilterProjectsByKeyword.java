package org.grits.toolbox.core.projectexplorer.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.grits.toolbox.core.preference.share.MultiChoicePreference;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.projectexplorer.filter.KeywordFilter;
import org.grits.toolbox.core.projectexplorer.part.ProjectExplorerPart;

public class FilterProjectsByKeyword {
	
	KeywordFilter filter = new KeywordFilter();
	
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell,
			@Named(IServiceConstants.ACTIVE_PART) MPart part, final MToolItem item) {
		ProjectExplorerPart curView = null;
		if (part != null && part.getObject() instanceof ProjectExplorerPart ) {
			curView = (ProjectExplorerPart) part.getObject();
			if (item.isSelected()) {
				// open a dialog to list available keywords and let the user choose
				MultiChoicePreference<String>keywordPreference  =
						ProjectPreferenceStore.getMultiChoicePreference(
								ProjectPreferenceStore.ParameterizedPreference.KEYWORD);
				Set<String> allKeywords = new TreeSet<>();
				allKeywords.addAll(keywordPreference.getSelectedValues());
				allKeywords.addAll (keywordPreference.getOtherValues());
				ListSelectionDialog dialog = new ListSelectionDialog(shell, allKeywords, 
						new ArrayContentProvider(), new LabelProvider(), "Select one or more keywords for filter");
				if (dialog.open() == Window.OK) {
					Object[] selected = dialog.getResult();
					List<String> keywords = new ArrayList<>();
					for(Object sel: selected) {
						keywords.add((String)sel);
					}
					filter.setApplied(false);
					filter.setKeywords(keywords);
					curView.filter(filter, true);
				}
				
			} else {
				filter.setApplied(true);
				curView.filter(filter, false);
			}
		}
	}
}
