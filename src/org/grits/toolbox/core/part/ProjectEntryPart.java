
package org.grits.toolbox.core.part;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.ProjectDetailsHandler;
import org.grits.toolbox.core.datamodel.property.project.ProjectCollaborator;
import org.grits.toolbox.core.datamodel.property.project.ProjectDetails;
import org.grits.toolbox.core.datamodel.property.project.ProjectEvent;
import org.grits.toolbox.core.datamodel.property.project.ProjectTasklist;
import org.grits.toolbox.core.img.ImageShare;
import org.grits.toolbox.core.part.handler.AddHandler;
import org.grits.toolbox.core.part.handler.DeleteHandler;
import org.grits.toolbox.core.part.handler.ViewHandler;
import org.grits.toolbox.core.part.provider.CollaboratorsLabelProvider;
import org.grits.toolbox.core.part.provider.EventsLabelProvider;
import org.grits.toolbox.core.part.provider.TasklistsLabelProvider;
import org.grits.toolbox.core.preference.project.CollaboratorTableColumn;
import org.grits.toolbox.core.preference.project.TasklistTableColumn;
import org.grits.toolbox.core.preference.share.IGritsPreferenceStore;
import org.grits.toolbox.core.preference.share.MultiChoicePreference;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.projectexplorer.part.ProjectExplorerPart;
import org.grits.toolbox.core.utilShare.ListenerFactory;
import org.grits.toolbox.core.utilShare.SelectKeywordsDialog;
import org.grits.toolbox.core.utilShare.provider.GenericListContentProvider;
import org.grits.toolbox.core.utilShare.sort.TableColumnComparatorListener;
import org.grits.toolbox.core.utilShare.sort.TableViewerComparator;
import org.grits.toolbox.core.utils.MaintainTableColumnRatioListener;

@SuppressWarnings("restriction")
public class ProjectEntryPart
{
	private static final Logger logger = Logger.getLogger(ProjectEntryPart.class);

	public static final String PART_ID = "org.grits.toolbox.core.partdescriptor.entry.project";

	public static final String EVENT_TOPIC_FIELD_SELECTION = "grits_project_some_field_selected";

	private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
	public static final String ADD_NEW_TO_PREFERENCE = "Add New ...";
	private static final int TABLE_MIN_WIDTH = 600;

	private Entry projectEntry = null;
	private ProjectDetails projectDetails = null;

	protected Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
	private Color backgroundColor = null;

	@Inject private MDirtyable dirtyable;
	@Inject private IEventBroker eventBroker;
	@Inject private ESelectionService selectionService = null;
	@Inject private EHandlerService eHandlerService;
	@Inject private ECommandService eCommandService;

	private Text creationTimeText = null;
	private Text modificationTimeText = null;
	private Text descriptionText = null;
	private TableViewer collaboratorsTableViewer = null;
	private TableViewer tasklistsTableViewer = null;
	private TableViewer eventsTableViewer = null;
	private ControlListener[] controlListeners = null;
	private Text keywordText = null;
	private Button selectKeywordsButton = null;

	public ProjectCollaborator lastCollaboratorSelection = null;
	public ProjectTasklist lastProjectTaskSelection = null;
	public ProjectEvent lastProjectEventSelection = null;
	
	EPartService partService;

	@Inject
	public ProjectEntryPart(@Named(IServiceConstants.ACTIVE_SELECTION) Entry entry,
			@Named(IServiceConstants.ACTIVE_PART) MPart part, 
			EPartService partService, EModelService modelService, MApplication application)
	{
		this.partService = partService;
		if(entry != null && ProjectExplorerPart.PART_ID.equals(part.getElementId()))
		{
			this.projectEntry = entry;
			try
			{
				projectDetails = ProjectDetailsHandler.getProjectDetails(projectEntry);
				String projectPerspectiveId = "org.grits.toolbox.core.perspective.entry.project";
				MPerspective projectPerspective = (MPerspective)
						modelService.find(projectPerspectiveId, application);
				if(projectPerspective == null)
				{
					projectPerspectiveId = "org.grits.toolbox.core.perspective.entry.project.<Project Perspective>";
					projectPerspective = (MPerspective) modelService.find(projectPerspectiveId, application);
				}
				partService.switchPerspective(projectPerspective);
			} catch (IOException e)
			{
				MessageDialog.openError(Display.getCurrent().getActiveShell(), 
						"Error Creating Project Details", e.getMessage());
				logger.error(e.getMessage(), e);
			}
		}
	}

	@PostConstruct
	public void postConstruct(Composite parent)
	{
		logger.debug("Creating Project Editor Part");
		if(projectDetails != null)
		{
			parent.setLayout(new FillLayout());

			ScrolledComposite scrolledComposite = new ScrolledComposite(parent, 
					SWT.H_SCROLL | SWT.V_SCROLL| SWT.BORDER);
			GridData layoutData = new GridData();
			scrolledComposite.setLayoutData(layoutData);
			backgroundColor = Display.getCurrent().getSystemColor(
					SWT.COLOR_LIST_BACKGROUND);
			scrolledComposite.setBackground(backgroundColor);
			scrolledComposite.setBackgroundMode(SWT.INHERIT_FORCE);
			scrolledComposite.setLayout(new FillLayout());

			Composite generalPartComposite  = new Composite(scrolledComposite, SWT.FILL);

			GridLayout generalPartLayout = new GridLayout();
			generalPartLayout.marginWidth = 10;
			generalPartLayout.marginHeight = 10;
			generalPartLayout.horizontalSpacing = 30;
			generalPartLayout.verticalSpacing = 10;
			generalPartLayout.numColumns = 2;
			generalPartComposite.setLayout(generalPartLayout);

			generalPartComposite.setLayoutData(
					new GridData());//SWT.FILL, SWT.BEGINNING, true, false, 2, 1

			Label creationTimeLabel = new Label(generalPartComposite, SWT.NONE);
			creationTimeLabel.setFont(boldFont);
			creationTimeLabel.setLayoutData(new GridData());
			creationTimeLabel.setText("Created");

			creationTimeText = new Text(generalPartComposite, SWT.READ_ONLY);
			creationTimeText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
			SimpleDateFormat sf = new SimpleDateFormat(DATE_FORMAT);
			creationTimeText.setText(sf.format(projectEntry.getCreationDate()));

			Label modificationTimeLabel = new Label(generalPartComposite, SWT.NONE);
			modificationTimeLabel.setFont(boldFont);
			modificationTimeLabel.setLayoutData(new GridData());
			modificationTimeLabel.setText("Modified");

			modificationTimeText = new Text(generalPartComposite, SWT.READ_ONLY);
			if(projectDetails.getModificationTime() != null)
				modificationTimeText.setText(sf.format(projectDetails.getModificationTime()));
			modificationTimeText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

			Label descriptionLabel = new Label(generalPartComposite, SWT.NONE);
			descriptionLabel.setFont(boldFont);
			descriptionLabel.setText("Description");
			GridData labelLayoutData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
			labelLayoutData.verticalSpan = 8;
			labelLayoutData.horizontalSpan = 1;
			descriptionLabel.setLayoutData(labelLayoutData);

			descriptionText = new Text(generalPartComposite, SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
			GridData gridData = new GridData(GridData.FILL_BOTH);
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.verticalSpan = 8;
			gridData.widthHint = 400;
			gridData.heightHint = 80;
			descriptionText.setLayoutData(gridData);

			controlListeners = new ControlListener[3];
			createCollaboratorsTable(generalPartComposite);
			createTasklistsTable(generalPartComposite);
			createEventsTable(generalPartComposite);
			createKeywordsPart(generalPartComposite);

			initializeValues();

			descriptionText.addTraverseListener(ListenerFactory.getTabTraverseListener());
			descriptionText.addKeyListener(ListenerFactory.getCTRLAListener());
			descriptionText.addModifyListener(new ModifyListener()
			{
				@Override
				public void modifyText(ModifyEvent e)
				{
					String newDescription = descriptionText.getText().trim();
					if(!newDescription.equals(projectDetails.getDescription()))
					{
						projectDetails.setDescription(newDescription);
						dirtyable.setDirty(true);
					}
				}
			});

			scrolledComposite.setContent(generalPartComposite);
			scrolledComposite.setMinSize(generalPartComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			scrolledComposite.setExpandVertical(true);
			scrolledComposite.setExpandHorizontal(true);

		}

		logger.debug("Finished creating Project Editor");
	}

	private void initializeValues()
	{
		if(projectDetails.getDescription() != null)
		{
			descriptionText.setText(projectDetails.getDescription());
		}
		collaboratorsTableViewer.setInput(projectDetails.getCollaborators());
		tasklistsTableViewer.setInput(projectDetails.getTasklists());
		eventsTableViewer.setInput(projectDetails.getEvents());
		keywordText.setText(getCSKeywords(projectDetails.getKeywords()));
	}

	private void createCollaboratorsTable(Composite parent)
	{
		logger.debug("Creating Collaborator's part");

		collaboratorsTableViewer = createTable(parent, "Collaborators", 2);

		Section section = (Section) collaboratorsTableViewer.getTable().getParent().getParent();
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		ToolBar toolbar = toolBarManager.createControl(section);
		section.setTextClient(toolbar);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(AddHandler.PARAMETER_ADD_TYPE,
				ProjectCollaborator.class.getName());
		addToolItem(toolbar, ImageShare.ADD_ICON.createImage(),
				"Add a new Collaborator", AddHandler.COMMAND_ID, parameters);
		addToolItem(toolbar, ImageShare.DELETE_ICON.createImage(),
				"Remove the selected Collaborator", DeleteHandler.COMMAND_ID, null);
		addToolItem(toolbar, ImageShare.VIEW_ICON.createImage(),
				"View the selected Collaborator", ViewHandler.COMMAND_ID, null);

		toolbar.setData(collaboratorsTableViewer);

		Set<Integer> visibleColumns = new HashSet<Integer>();
		visibleColumns.add(0);
		visibleColumns.addAll(ProjectPreferenceStore.getMultiChoiceInteger(
						ProjectPreferenceStore.IntegerPreference.COLLABORATORS_TABLE).getSelectedValues());
		int[] columnRatios = getColumnRatios(collaboratorsTableViewer, 
				CollaboratorTableColumn.COLUMNS, visibleColumns);

		collaboratorsTableViewer.getTable().setHeaderVisible(true);
		collaboratorsTableViewer.getTable().setLinesVisible(true);
		collaboratorsTableViewer.getTable().addControlListener(controlListeners[0] = 
				new MaintainTableColumnRatioListener(TABLE_MIN_WIDTH, columnRatios));
		collaboratorsTableViewer.setLabelProvider(
				new CollaboratorsLabelProvider(collaboratorsTableViewer));

		collaboratorsTableViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				lastCollaboratorSelection = 
						(ProjectCollaborator) ((StructuredSelection) event.getSelection()).getFirstElement();
				partService.bringToTop(partService.findPart(CollaboratorPart.PART_ID)); // sena - ticket #683: do not require double-click to display the part
				eventBroker.post(EVENT_TOPIC_FIELD_SELECTION, lastCollaboratorSelection);
//				selectionService.setSelection(
//						((StructuredSelection) event.getSelection()).getFirstElement());
			}
		});

		collaboratorsTableViewer.getTable().addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent event)
			{
				if (event.keyCode == SWT.DEL)
				{
					event.data = collaboratorsTableViewer;
					executeCommand(event, DeleteHandler.COMMAND_ID, null);
				}
			}
		});

		collaboratorsTableViewer.addDoubleClickListener(new IDoubleClickListener()
		{
			@Override
			public void doubleClick(DoubleClickEvent event)
			{
				executeCommand(event, ViewHandler.COMMAND_ID, null);
			}
		});

		addViewerComparator(collaboratorsTableViewer);

		logger.debug("Creating Collaborator's part");
	}

	private void createTasklistsTable(Composite parent)
	{
		logger.debug("Creating Tasklist part");

		tasklistsTableViewer  = createTable(parent, "Tasklist", 3);

		Section section = (Section) tasklistsTableViewer.getTable().getParent().getParent();
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		ToolBar toolbar = toolBarManager.createControl(section);
		section.setTextClient(toolbar);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(AddHandler.PARAMETER_ADD_TYPE, ProjectTasklist.class.getName());
		addToolItem(toolbar, ImageShare.ADD_ICON.createImage(),
				"Add a new Task", AddHandler.COMMAND_ID, parameters);
		addToolItem(toolbar, ImageShare.DELETE_ICON.createImage(),
				"Remove the selected Task", DeleteHandler.COMMAND_ID, null);
		addToolItem(toolbar, ImageShare.VIEW_ICON.createImage(),
				"View the selected Task", ViewHandler.COMMAND_ID, null);

		toolbar.setData(tasklistsTableViewer);

		Set<Integer> visibleColumns = new HashSet<Integer>();
		visibleColumns.add(0);
		visibleColumns.addAll(ProjectPreferenceStore.getMultiChoiceInteger(
						ProjectPreferenceStore.IntegerPreference.TASKLIST_TABLE).getSelectedValues());
		int[] columnRatios = getColumnRatios(tasklistsTableViewer, 
				TasklistTableColumn.COLUMNS, visibleColumns);

		tasklistsTableViewer.getTable().setHeaderVisible(true);
		tasklistsTableViewer.getTable().setLinesVisible(true);
		tasklistsTableViewer.getTable().addControlListener(controlListeners[1] = 
				new MaintainTableColumnRatioListener(TABLE_MIN_WIDTH, columnRatios));
		tasklistsTableViewer.setLabelProvider(new TasklistsLabelProvider(tasklistsTableViewer));

		tasklistsTableViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				lastProjectTaskSelection = 
						(ProjectTasklist) ((StructuredSelection) event.getSelection()).getFirstElement();
				partService.bringToTop(partService.findPart(TaskPart.PART_ID)); // sena - ticket #683: do not require double-click to display the part
				eventBroker.post(EVENT_TOPIC_FIELD_SELECTION, lastProjectTaskSelection);
//				selectionService.setSelection(
//						((StructuredSelection) event.getSelection()).getFirstElement());
			}
		});

		tasklistsTableViewer.getTable().addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(final KeyEvent event)
			{
				if (event.keyCode == SWT.DEL)
				{
					event.data = tasklistsTableViewer;
					executeCommand(event, DeleteHandler.COMMAND_ID, null);
				}
			}
		});

		tasklistsTableViewer.addDoubleClickListener(new IDoubleClickListener()
		{
			@Override
			public void doubleClick(DoubleClickEvent event)
			{
				executeCommand(event, ViewHandler.COMMAND_ID, null);
			}
		});

		addViewerComparator(tasklistsTableViewer);

		logger.debug("Creating Tasklist part");
	}

	private void createEventsTable(Composite parent)
	{
		logger.debug("Creating Event part");

		eventsTableViewer = createTable(parent, "Events", 2);

		TableViewerColumn nameColumnViewer = new TableViewerColumn(eventsTableViewer, SWT.LEFT);
		nameColumnViewer.getColumn().setText("Action");
		TableViewerColumn dateColumnViewer = new TableViewerColumn(eventsTableViewer, SWT.LEFT);
		dateColumnViewer.getColumn().setText("Date");
		TableViewerColumn descriptionColumnViewer = new TableViewerColumn(eventsTableViewer, SWT.LEFT);
		descriptionColumnViewer.getColumn().setText("Description");

		Section section = (Section) eventsTableViewer.getTable().getParent().getParent();
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		ToolBar toolbar = toolBarManager.createControl(section);
		section.setTextClient(toolbar);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(AddHandler.PARAMETER_ADD_TYPE, ProjectEvent.class.getName());
		addToolItem(toolbar, ImageShare.ADD_ICON.createImage(),
				"Add a new Event", AddHandler.COMMAND_ID, parameters);
		addToolItem(toolbar, ImageShare.DELETE_ICON.createImage(),
				"Remove the selected Event", DeleteHandler.COMMAND_ID, null);
		addToolItem(toolbar, ImageShare.VIEW_ICON.createImage(),
				"View the selected Event", ViewHandler.COMMAND_ID, null);

		toolbar.setData(eventsTableViewer);

		eventsTableViewer.getTable().addControlListener(controlListeners[2] = 
				new MaintainTableColumnRatioListener(TABLE_MIN_WIDTH, 1, 1, 4));
		eventsTableViewer.getTable().setHeaderVisible(true);
		eventsTableViewer.getTable().setLinesVisible(true);
		eventsTableViewer.setContentProvider(new GenericListContentProvider());
		eventsTableViewer.setLabelProvider(new EventsLabelProvider());

		eventsTableViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				lastProjectEventSelection =
						(ProjectEvent) ((StructuredSelection) event.getSelection()).getFirstElement();
				partService.bringToTop(partService.findPart(EventPart.PART_ID));  // sena - ticket #683: do not require double-click to display the part
				eventBroker.post(EVENT_TOPIC_FIELD_SELECTION, lastProjectEventSelection);
			}
		});

		eventsTableViewer.getTable().addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(final KeyEvent event)
			{
				if (event.keyCode == SWT.DEL)
				{
					event.data = eventsTableViewer;
					executeCommand(event, DeleteHandler.COMMAND_ID, null);
				}
			}
		});

		eventsTableViewer.addDoubleClickListener(new IDoubleClickListener()
		{

			@Override
			public void doubleClick(DoubleClickEvent event)
			{
				executeCommand(event, ViewHandler.COMMAND_ID, null);
			}
		});

		addViewerComparator(eventsTableViewer);

		logger.debug("Creating Event part");
	}

	@Optional
	@Inject
	void refreshCollaboratorsTable(
			@UIEventTopic(CollaboratorPart.EVENT_TOPIC_VALUE_MODIFIED) 
			ProjectCollaborator collaborator)
	{
		if(collaborator != null &&
				((List<?>) this.collaboratorsTableViewer.getInput()).contains(collaborator))
		{
			collaboratorsTableViewer.refresh(collaborator);
			dirtyable.setDirty(true);
		}
	}

	@Optional
	@Inject
	void refreshTaskTable(
			@UIEventTopic(TaskPart.EVENT_TOPIC_VALUE_MODIFIED) 
			ProjectTasklist projectTasklist)
	{
		if(projectTasklist != null &&
				((List<?>) this.tasklistsTableViewer.getInput()).contains(projectTasklist))
		{
			tasklistsTableViewer.refresh(projectTasklist);
			dirtyable.setDirty(true);
		}
	}

	@Optional
	@Inject
	void refreshEventsTable(
			@UIEventTopic(EventPart.EVENT_TOPIC_VALUE_MODIFIED) ProjectEvent projectEvent)
	{
		if(projectEvent != null 
				&& ((List<?>) this.eventsTableViewer.getInput()).contains(projectEvent))
		{
			eventsTableViewer.refresh(projectEvent);
			dirtyable.setDirty(true);
		}
	}

	@Optional
	@Inject
	void resetTableColumn(
			@UIEventTopic(IGritsPreferenceStore.EVENT_TOPIC_PREF_VALUE_CHANGED) String preferenceName)
	{
		if(ProjectPreferenceStore.IntegerPreference.COLLABORATORS_TABLE.getPreferenceName().equals(preferenceName))
		{
			logger.debug("Resetting columns for collaborators table");

			collaboratorsTableViewer.getTable().removeControlListener(controlListeners[0]);
			removeColumns(collaboratorsTableViewer);
			Set<Integer> visibleColumns = new HashSet<Integer>();
			visibleColumns.addAll(ProjectPreferenceStore.getMultiChoiceInteger(
							ProjectPreferenceStore.IntegerPreference.COLLABORATORS_TABLE).getSelectedValues());
			int[] columnRatios = getColumnRatios(collaboratorsTableViewer, 
					CollaboratorTableColumn.COLUMNS, visibleColumns);
			collaboratorsTableViewer.getTable().addControlListener(controlListeners[0] = 
					new MaintainTableColumnRatioListener(TABLE_MIN_WIDTH, columnRatios));
			collaboratorsTableViewer.setLabelProvider(
					new CollaboratorsLabelProvider(collaboratorsTableViewer));
			resizeTable(collaboratorsTableViewer, controlListeners[0]);
			addViewerComparator(collaboratorsTableViewer);
		}
		if(ProjectPreferenceStore.IntegerPreference.TASKLIST_TABLE.getPreferenceName().equals(preferenceName))
		{
			logger.debug("Resetting columns for tasklist table");

			tasklistsTableViewer.getTable().removeControlListener(controlListeners[1]);
			removeColumns(tasklistsTableViewer);
			Set<Integer> visibleColumns = new HashSet<Integer>();
			visibleColumns.addAll(ProjectPreferenceStore.getMultiChoiceInteger(
							ProjectPreferenceStore.IntegerPreference.TASKLIST_TABLE).getSelectedValues());
			int[] columnRatios = getColumnRatios(tasklistsTableViewer, 
					TasklistTableColumn.COLUMNS, visibleColumns);
			tasklistsTableViewer.getTable().addControlListener(controlListeners[1] = 
					new MaintainTableColumnRatioListener(TABLE_MIN_WIDTH, columnRatios));
			tasklistsTableViewer.setLabelProvider(
					new TasklistsLabelProvider(tasklistsTableViewer));
			resizeTable(tasklistsTableViewer, controlListeners[1]);
			addViewerComparator(tasklistsTableViewer);
		}
		
	}

	private void removeColumns(TableViewer tableViewer)
	{
		TableColumn[] columns = tableViewer.getTable().getColumns();
		for(int i = 0; i < columns.length; i++)
		{
			columns[i].dispose();
		}
	}

	private void resizeTable(TableViewer tableViewer,
			ControlListener controlListener)
	{
		Event event = new Event();
		event.widget = tableViewer.getTable();
		controlListener.controlResized(new ControlEvent(event));
	}

	private void executeCommand(Object eventObject,
			String commandId, Map<String, Object> commandParameters)
	{
		selectionService.setSelection(eventObject);
		eHandlerService.executeHandler(
				eCommandService.createCommand(commandId, commandParameters));
	}

	private ToolItem addToolItem(ToolBar toolbar, Image icon,
			String toolTipText, String commandId, Map<String, Object> commandParameters)
	{
		ToolItem toolItem = new ToolItem(toolbar, SWT.PUSH);
		toolItem.setImage(icon);
		toolItem.setToolTipText(toolTipText);
		toolItem.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				executeCommand(e, commandId, commandParameters);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				executeCommand(e, commandId, commandParameters);
			}
		});
		return toolItem;
	}

	private void createKeywordsPart(Composite parent)
	{
		logger.debug("Creating Keywords Part");

		Label placeholderLabel = new Label(parent, SWT.NONE);
		GridData placeHolderData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		placeHolderData.horizontalSpan = 2;
		placeHolderData.verticalSpan = 2;
		placeholderLabel.setLayoutData(placeHolderData);

		Label kewywordLabel = new Label(parent, SWT.FILL);
		kewywordLabel.setFont(boldFont);
		kewywordLabel.setBackground(backgroundColor);
		kewywordLabel.setText("Keywords");
		GridData keywordLabelLayoutData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		keywordLabelLayoutData.verticalSpan = 6;
		kewywordLabel.setLayoutData(keywordLabelLayoutData);

		keywordText = new Text(parent, SWT.READ_ONLY | SWT.WRAP 
				| SWT.BORDER);
		GridData keywordTextData = new GridData(GridData.FILL_BOTH);
		keywordTextData.horizontalAlignment = GridData.FILL;
		keywordTextData.grabExcessHorizontalSpace = true;
		keywordTextData.verticalSpan = 6;
		keywordTextData.widthHint = 400;
		keywordTextData.heightHint = 70;
		keywordText.setLayoutData(keywordTextData);

		keywordText.addTraverseListener(ListenerFactory.getTabTraverseListener());
		keywordText.addKeyListener(ListenerFactory.getCTRLAListener());
		keywordText.setEnabled(false);

		selectKeywordsButton = new Button(parent, SWT.NONE);
		selectKeywordsButton.setText("Modify Keywords");
		selectKeywordsButton.addSelectionListener(new SelectionListener()
		{

			private SelectKeywordsDialog selectKeywordsDialog;

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Set<String> allKeywords = new HashSet<String>();
				MultiChoicePreference<String> keywordPreference =
						ProjectPreferenceStore.getMultiChoicePreference(
								ProjectPreferenceStore.ParameterizedPreference.KEYWORD);
				allKeywords.addAll(keywordPreference.getSelectedValues());
				allKeywords.addAll(keywordPreference.getOtherValues());
				// Collections.sort(keywords, String.CASE_INSENSITIVE_ORDER);
				selectKeywordsDialog = new SelectKeywordsDialog(Display.getCurrent().getActiveShell(), allKeywords); 
				selectKeywordsDialog.setSelectedKeyWords(projectDetails.getKeywords());
				if(selectKeywordsDialog.open() == Window.OK)
				{
					Set<String> keywords = new HashSet<String>();
					keywords.addAll(selectKeywordsDialog.getSelectedKeywords());
					keywordText.setText(getCSKeywords(keywords));
					projectDetails.setKeywords(keywords);
					dirtyable.setDirty(true);
					// setDirty(true);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				if(((Button) e.getSource()).getSelection())
				{
					if(selectKeywordsDialog.open() == Window.OK)
					{
						Set<String> keywords = projectDetails.getKeywords();
						keywords.addAll(selectKeywordsDialog.getSelectedKeywords());
						keywordText.setText(getCSKeywords(keywords));
						projectDetails.setKeywords(keywords);
						dirtyable.setDirty(true);
						//						setDirty(true);
					}
				}
			}
		});
		GridData selectKeywordsButtonData = new GridData();
		selectKeywordsButtonData.horizontalAlignment = SWT.CENTER;
		selectKeywordsButtonData.horizontalSpan = 2;
		selectKeywordsButtonData.widthHint = 200;
		selectKeywordsButtonData.verticalSpan = 2;
		selectKeywordsButton.setLayoutData(selectKeywordsButtonData);

		logger.debug("Creating Keywords Part");
	}

	private String getCSKeywords(Set<String> keywordSet)
	{
		String keywords = "";
		final String separator = ", ";
		for(String keyword : keywordSet)
		{
			keywords += keyword + separator;
		}
		if(!keywords.isEmpty())
			keywords = keywords.substring(0, keywords.length() - separator.length());
		return keywords;
	}

	private int[] getColumnRatios(TableViewer tableViewer,
			String[] allColumns, Set<Integer> visibleColumns)
	{
		int i = 0;
		TableViewerColumn columnViewer = null;
		int columnNumber = 0;
		int[] columnRatios = new int[visibleColumns.size()];
		while(i < allColumns.length)
		{
			if(visibleColumns.contains(i))
			{
				columnViewer = new TableViewerColumn(tableViewer, SWT.LEFT, columnNumber);
				columnViewer.getColumn().setText(allColumns[i]);
				columnRatios[columnNumber] = 2;
				columnNumber++;
			}
			i++;
		}
		columnRatios[0] = 3;
		return columnRatios;
	}

	private void addViewerComparator(TableViewer tableViewer) 
	{
		tableViewer.setComparator(new TableViewerComparator());
		int totalColumns = tableViewer.getTable().getColumns().length;
		for(int i = 0 ; i < totalColumns ; i++)
		{
			tableViewer.getTable().getColumn(i).addSelectionListener(
					new TableColumnComparatorListener(tableViewer));
		}
	}

	private TableViewer createTable(Composite parent, String tableTitle, int verticalSpan)
	{
		Composite sectionParentComposite = new Composite(parent, SWT.FILL);
		sectionParentComposite.setLayout(new TableWrapLayout());
		Section section = new Section(sectionParentComposite, Section.TREE_NODE 
				| Section.EXPANDED | Section.TITLE_BAR);
		section.setText(tableTitle);
		Color sectionColor = new Color(Display.getCurrent(), 20, 199, 255);
		section.setTitleBarBackground(sectionColor);
		section.setBackground(backgroundColor);
		section.setTitleBarForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE));
		section.setLayout(new TableWrapLayout());

		Composite infoComposite = new Composite(section, SWT.NONE);
		infoComposite.setLayout(new GridLayout());
		infoComposite.setBackground(backgroundColor);
		infoComposite.setBackgroundMode(SWT.INHERIT_FORCE);
		TableViewer tableViewer = new TableViewer(infoComposite, 
				SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL|SWT.SINGLE|SWT.FULL_SELECTION);
		tableViewer.setContentProvider(new GenericListContentProvider());
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 1;
		gridData.heightHint = verticalSpan*50;
		gridData.widthHint = TABLE_MIN_WIDTH;
		tableViewer.getTable().setLayoutData(gridData);

		TableWrapData sectionLayoutData = new TableWrapData(TableWrapData.FILL, TableWrapData.BOTTOM);
		sectionLayoutData.grabHorizontal = true;
		sectionLayoutData.grabVertical = true;
		section.setLayoutData(sectionLayoutData);

		section.setClient(infoComposite);
		GridData compositeLayoutData = new GridData(GridData.FILL_BOTH);

		compositeLayoutData.verticalSpan = 1;
		compositeLayoutData.horizontalSpan = 2;
		sectionParentComposite.setLayoutData(compositeLayoutData);

		return tableViewer;
	}

	@PreDestroy
	public void preDestroy()
	{
		
	}

	@Focus
	public void onFocus()
	{
		descriptionText.setFocus();
	}

	@Persist
	public void save()
	{
		logger.info("saving project entry");
		boolean changesSaved = false;
		try
		{
			Date date = new Date();
			SimpleDateFormat sf = new SimpleDateFormat(DATE_FORMAT);
			projectDetails.setModificationTime(date);
			changesSaved = ProjectDetailsHandler.writeProjectDetails(projectDetails);
			modificationTimeText.setText(sf.format(date));
		} catch (IOException e)
		{
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error saving details.", 
					"The project information could not be saved to the file.");
		}
		dirtyable.setDirty(!changesSaved);
	}

	public ProjectCollaborator getLastCollaboratorSelection()
	{
		return lastCollaboratorSelection;
	}

	public ProjectTasklist getLastProjectTaskSelection()
	{
		return lastProjectTaskSelection;
	}

	public ProjectEvent getLastProjectEventSelection()
	{
		return lastProjectEventSelection;
	}
}