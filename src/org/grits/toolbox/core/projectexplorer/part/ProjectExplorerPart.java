
package org.grits.toolbox.core.projectexplorer.part;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.ProjectDetailsHandler;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.WorkspaceProperty;
import org.grits.toolbox.core.datamodel.property.project.ProjectDetails;
import org.grits.toolbox.core.projectexplorer.filter.ClosedProjectsFilter;
import org.grits.toolbox.core.projectexplorer.filter.KeywordFilter;
import org.grits.toolbox.core.projectexplorer.filter.ProjectFilter;
import org.grits.toolbox.core.projectexplorer.handler.OpenProjectHandler;
import org.grits.toolbox.core.projectexplorer.handler.ViewSelectedHandler;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.utilShare.sort.EntryComparator;

public class ProjectExplorerPart
{
	private static final Logger logger = Logger.getLogger(ProjectExplorerPart.class);

	public static final String PART_ID = "org.grits.toolbox.core.part.projectexplorer";
	public static final String POPUP_MENU_ID = "org.grits.toolbox.core.popupmenu.projectexplorer";
	
	private TreeViewer treeViewer = null;
	
	List<ProjectFilter> currentFilters = new ArrayList<>();

	@Inject private ESelectionService selectionService = null;
	@Inject private EHandlerService eHandlerService = null;
	@Inject private ECommandService eCommandService = null;
	@Inject private IGritsDataModelService dataModelService = null;

	@Inject @Named (IGritsDataModelService.WORKSPACE_ENTRY) private Entry workspaceEntry = null;

	@Inject
	public ProjectExplorerPart()
	{

	}

	@PostConstruct
	public void postConstruct(Composite parent, EMenuService menuService, MPart part)
	{
		logger.info("Creating Project Explorer part");
		
		// clear the toggle button (show annotated)'s status since the view's contents have been changed
		List<MToolBarElement> items = part.getToolbar().getChildren();
		for (MToolBarElement mToolBarElement : items) {
			if (mToolBarElement instanceof MToolItem) {
				((MToolItem) mToolBarElement).setSelected(false);    // clear toggle button
			}
		}
		
		PatternFilter filter = new PatternFilter();
		filter.setIncludeLeadingWildcard(true);
		FilteredTree tree = new FilteredTree(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL, filter, true, true);

		//treeViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		treeViewer = tree.getViewer();
		treeViewer.setContentProvider(new ProjectExplorerViewContentProvider());
		treeViewer.setLabelProvider(new ProjectExplorerLabelProvider());
		treeViewer.setInput(workspaceEntry);
		treeViewer.setComparator(new EntryComparator());

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				setSelection((StructuredSelection) event.getSelection());
			}
		});

		treeViewer.addDoubleClickListener(new IDoubleClickListener()
		{
			@Override
			public void doubleClick(DoubleClickEvent event)
			{
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				setSelection(selection);

				String commandId = ViewSelectedHandler.COMMAND_ID;
				if((selection.size() == 1)
						&& selection.getFirstElement() instanceof Entry
						&& ((Entry) selection.getFirstElement()).getProperty() instanceof ProjectProperty
						&& !((ProjectProperty) ((Entry) selection.getFirstElement()).getProperty()).isOpen())
				{
					commandId = OpenProjectHandler.COMMAND_ID; 
				}

				eHandlerService.executeHandler(
						eCommandService.createCommand(commandId, null));
			}
		});

		menuService.registerContextMenu(treeViewer.getControl(),
				POPUP_MENU_ID);

		logger.info("Creating Project Explorer part");
	}

	protected void setSelection(StructuredSelection structuredSelection)
	{
		if(structuredSelection  != null)
		{
			if(structuredSelection.size() == 1)
			{
				selectionService.setSelection(structuredSelection.getFirstElement());
			}
			else
			{
				selectionService.setSelection(structuredSelection);
			}
		}
		dataModelService.setLastSelection(structuredSelection);
	}

	@PreDestroy
	public void preDestroy()
	{

	}

	@Focus
	public void onFocus()
	{
		treeViewer.getControl().setFocus();
	}

	@Optional
	@Inject
	public void refresh(@UIEventTopic
			(IGritsDataModelService.EVENT_DATA_MODEL_CHANGED) Entry entry)
	{
		if(entry != null)
		{
			if(ProjectProperty.TYPE.equals(entry.getProperty().getType())
					&& !((ProjectProperty) entry.getProperty()).getOpen())
			{
				treeViewer.collapseToLevel(entry, 1);
			}
			Object[] expandedElements = treeViewer.getExpandedElements();
			treeViewer.refresh(entry);
			treeViewer.setExpandedElements(expandedElements);
		}
		else
			treeViewer.refresh();
	}

	@Optional @Inject
	public void resetSelection(@UIEventTopic
			(IGritsDataModelService.EVENT_SELECT_ENTRY) Object object)
	{
		if(object instanceof Entry)
		{
			treeViewer.setSelection(new StructuredSelection(object));
		}
		else if(object instanceof StructuredSelection)
		{
			treeViewer.setSelection((StructuredSelection) object);
		}
	}
	
	/**
	 * apply given filter or revert back the filter based on the apply argument. 
	 * 
	 * If there are other filters already applied, their changes will be kept
	 * @param filter filter to apply or revert
	 * @param apply if true, apply the filter, if false revert back to the list before this filter was applied
	 */
	public void filter (ProjectFilter filter, boolean apply) {
		if (apply) {
			// show progress dialog and apply filter to select the ones with an annotation
			class FilterProcess implements IRunnableWithProgress {
				Entry filteredEntry;
				
				@Override
		        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
		        {
					Entry currentInput = (Entry) treeViewer.getInput();
		            int totalWork = 1;
		            if (currentInput != null && currentInput.getChildren() != null) {
		            	totalWork = currentInput.getChildren().size();
		            }
		            monitor.beginTask("Filtering...", totalWork);
		            if (filter instanceof ClosedProjectsFilter)
		            	filteredEntry = filterChildrenForOpen (currentInput, monitor);
		            else if (filter instanceof KeywordFilter) 
		            	filteredEntry = filterChildrenByKeyword (currentInput, ((KeywordFilter) filter).getKeywords(), monitor);
			    	monitor.done();
			    }
			    Entry getFilteredEntry () {
			    	return filteredEntry;
			    }
			};
			FilterProcess p = new FilterProcess();
			try {
				new ProgressMonitorDialog(new Shell()).run(true, false, p);
				treeViewer.setInput(p.getFilteredEntry());
				treeViewer.refresh();
				filter.setApplied(true);
				if (!currentFilters.contains(filter))
					currentFilters.add(filter);
			} catch (InvocationTargetException e) {
				logger.error("Error while filtering", e);
			} catch (InterruptedException e) {
				logger.error("Error while filtering", e);
			}
		} else {
			// back to original
			treeViewer.setInput(workspaceEntry);
			filter.setApplied(false);
			for (ProjectFilter f: currentFilters) {
				if (f != filter) {
					if (f.isApplied()) {   // keep it applied
						filter (f, true);
					}
				}
			}
			treeViewer.refresh();
		}
	}

	/**
	 * filter out closed projects
	 * @param workspaceEntry current top level entry
	 * 
	 * @param monitor progress monitor 
	 * @return return an entry with all the open projects as children
	 */
	public Entry filterChildrenForOpen(Entry workspaceEntry, IProgressMonitor monitor) {
		Entry newEntry = new Entry();
		WorkspaceProperty prop = new WorkspaceProperty();
		newEntry.setProperty(prop);
		
		if (workspaceEntry != null && workspaceEntry.getChildren() != null) {
			for (Entry childEntry : workspaceEntry.getChildren()) {
				if (childEntry.getProperty() != null && childEntry.getProperty().getType().equals(ProjectProperty.TYPE)) {
					if (((ProjectProperty)childEntry.getProperty()).isOpen()) {
						newEntry.addChild(childEntry);
					}
				}
				monitor.worked(1);
			}
		}
		return newEntry;
	}
	

	/**
	 * create a new workspace entry with only the projects that has a keyword from the given list
	 * @param workspaceEntry current top level entry
	 * @param keywords list of keywords to match
	 * @param monitor progress monitor
	 * @return a new workspace entry with filtered projects
	 */
	public Entry filterChildrenByKeyword(Entry workspaceEntry, List<String> keywords, IProgressMonitor monitor) {
		Entry newEntry = new Entry();
		WorkspaceProperty prop = new WorkspaceProperty();
		newEntry.setProperty(prop);
		
		if (workspaceEntry != null && workspaceEntry.getChildren() != null) {
			for (Entry childEntry : workspaceEntry.getChildren()) {
				if (childEntry.getProperty() != null && childEntry.getProperty().getType().equals(ProjectProperty.TYPE)) {
					try {
						ProjectDetails projectDetails = ProjectDetailsHandler.getProjectDetails(childEntry);
						boolean keyWordMatch = false;
						for (String keyword: keywords) {
							for (String projectKeyword: projectDetails.getKeywords()) {
								if (keyword.equals(projectKeyword)) {
									keyWordMatch = true;
									break;
								}
							} 
							if (keyWordMatch) {
								newEntry.addChild(childEntry);
								break;
							}
						}
					} catch (IOException e) {
						logger.error("Cannot load project details for project: " + childEntry.getDisplayName(), e);
					}
				}
				monitor.worked(1);
			}
		}
		return newEntry;
	}
}