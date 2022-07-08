package org.grits.toolbox.core.preference;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.grits.toolbox.core.datamodel.GeneralSettings;
import org.grits.toolbox.core.datamodel.SettingEntry;
import org.grits.toolbox.core.img.ImageShare;
import org.grits.toolbox.core.utils.SettingsHandler;
import org.grits.toolbox.core.workspace.WorkspaceHistory;
import org.grits.toolbox.core.workspace.WorkspaceHistoryEntry;
import org.grits.toolbox.core.workspace.WorkspaceHistoryFileHandler;

public class GlobalPreferencesPage extends PreferencePage {
	
	private static final Logger logger = Logger.getLogger(GlobalPreferencesPage.class);
	private static final Image CHECKED = ImageShare.CHECKBOX_ICON_YES.createImage();
	private static final Image UNCHECKED = ImageShare.CHECKBOX_ICON_NO.createImage();
	
	GeneralSettings settings;
	WorkspaceHistory history;
	private TableViewer workspaceTable;
	
	public GlobalPreferencesPage() {
		try {
			settings = SettingsHandler.readSettings();
		} catch (Exception e) {
			logger.warn("No settings file");
			settings = new GeneralSettings();
		}
		try {
			List<WorkspaceHistoryEntry> workspaceList = WorkspaceHistoryFileHandler.readHistoryFile();
			history = new WorkspaceHistory();
			history.setWorkspaceList(workspaceList);
		} catch (Exception e) {
			logger.error("workspace history file cannot be read", e);
			history = new WorkspaceHistory();
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginRight = 8;
		//layout.verticalSpacing = 20;
		layout.horizontalSpacing = 20;
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth=true;
		container.setLayout(layout);
		
		Label description = new Label (container, SWT.WRAP);
		description.setText("This page contains preferences that are applicable to all instances of GRITS\nand to all workspaces of the current user");
		description.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		Label separator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		Label settingsTitle = new Label (container, SWT.NONE);
		settingsTitle.setText("Do not show again");
		settingsTitle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		if (settings != null && settings.getHiddenDialogs() != null) {
			for (SettingEntry se: settings.getHiddenDialogs()) {
				Label prefLabel = new Label(container, SWT.NONE);
				prefLabel.setText(se.getName());
				prefLabel.setToolTipText(se.getDescription());
				Button valueControl = new Button(container, SWT.CHECK);
				valueControl.setSelection(true);
				valueControl.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (valueControl.getSelection())
							settings.addHiddenDialog(se);
						else
							settings.removeHiddenDialog(se);
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
				new Label(container, SWT.NONE);
			}
		}
		new Label(container, SWT.NONE);
		
		separator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		if (history != null) {
			addWorkspaceHistoryTable(container);
		}
		
		return container;
	}
	
	private void addWorkspaceHistoryTable(Composite parent) {
		//Composite comp = new Composite (parent, SWT.NONE);
		//comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 4));
		Label workspaceSelection = new Label (parent, SWT.NONE);
		workspaceSelection.setText("Used Workspaces");
		workspaceSelection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		workspaceTable = new TableViewer(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		workspaceTable.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 4));
		workspaceTable.getTable().setLinesVisible(true);
		workspaceTable.getTable().setHeaderVisible(true);
		TableViewerColumn column1 = new TableViewerColumn(workspaceTable, SWT.LEFT);
		column1.getColumn().setText("Path");
		column1.getColumn().setWidth(300);
		column1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof WorkspaceHistoryEntry)
					return ((WorkspaceHistoryEntry) element).getPath();
				return null;
			}
		});
		TableViewerColumn column2 = new TableViewerColumn(workspaceTable, SWT.CENTER);
		column2.getColumn().setText("Last Active");
		column2.getColumn().setWidth(80);
		
		column2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return null;
			}
	  
			@Override
			public Image getImage(Object element) {
				if (element instanceof WorkspaceHistoryEntry) {
					String lastActive = ((WorkspaceHistoryEntry) element).getLastActive();
					if (lastActive != null && lastActive.equals("true")) {
						return CHECKED;
					}
		            else 
		            	return UNCHECKED;  
				}
				return null;
				
			}
		});
		workspaceTable.setContentProvider(new ArrayContentProvider());
		workspaceTable.setInput(history.getWorkspaceList());
		
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		Button deleteButton = new Button(parent, SWT.PUSH);
		deleteButton.setText("Remove");
		deleteButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = workspaceTable.getTable().getSelection();
				for (TableItem selected : items) {
					WorkspaceHistoryEntry entry = (WorkspaceHistoryEntry) selected.getData();
					history.getWorkspaceList().remove(entry);
				}
				workspaceTable.refresh();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	@Override
	public boolean performOk() {
		return save();
	}
	
	private boolean save() {
		try {
			if (settings != null)
				SettingsHandler.writeSettings(settings);
			if (history != null && history.getWorkspaceList() != null) 
				WorkspaceHistoryFileHandler.writeHistoryFile (history);
			return true;
		} catch (Exception e) {
			logger.error("Could not update global settings", e);
			return false;
		}
	}

	@Override
	protected void performApply() {
		save();
	}
}
