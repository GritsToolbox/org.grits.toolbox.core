 
package org.grits.toolbox.core.part;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.log4j.Logger;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.datamodel.property.project.ProjectCollaborator;
import org.grits.toolbox.core.handler.OpenPreferenceHandler;
import org.grits.toolbox.core.part.toolitem.SaveCollaborator;
import org.grits.toolbox.core.preference.project.FundingPreference;
import org.grits.toolbox.core.preference.project.PositionPreference;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.typeahead.NamespaceHandler;
import org.grits.toolbox.core.typeahead.PatriciaTrieContentProposalProvider;

@SuppressWarnings("restriction")
public class CollaboratorPart
{
	private static Logger logger = Logger.getLogger(CollaboratorPart.class);

	public static final String PART_ID = "org.grits.toolbox.core.part.project.collaborator";

	public static final String EVENT_TOPIC_VALUE_MODIFIED = "EventTopic_CollabPart_Modified";

	private static final String ADD_NEW_TO_PREFERENCE = "Add New ...";

	private Text nameText = null;
	private Text groupPIText = null;
	private ComboViewer positionCombo = null;
	private Text departmentText = null;
	private Text institutionText = null;
	private Text addressText = null;
	private Text countryText = null;
	private Text emailText = null;
	private Text phoneText = null;
	private Text faxText = null;
	private ComboViewer fundingCombo = null;
	private Text grantText = null;

	protected ControlDecoration nameErrorDecoration = null;
	private NamespaceHandler handler = null;
	private PatriciaTrie<String> trie = null;
	private PatriciaTrieContentProposalProvider contentProposalProvider = null;
	private ContentProposalAdapter contentProposalAdapter = null;

	@Inject IEventBroker eventBroker;
	@Inject ECommandService commandService;
	@Inject EHandlerService handlerService;

	private ProjectCollaborator collaborator = null;
	private MPart collabPart = null;

	@Inject
	public CollaboratorPart()
	{

	}

	@Optional
	@Inject
	void setCollaborator(@UIEventTopic
			(ProjectEntryPart.EVENT_TOPIC_FIELD_SELECTION) ProjectCollaborator collaborator,
			EPartService partService, EModelService modelService)
	{
		MPart collabPart = partService == null ? null : partService.findPart(PART_ID);
		if(collabPart != null && collabPart.getObject() != null)
		{
			clearAll();
			if(collaborator != null)
			{
				nameText.setText(collaborator.getName());
				String value = collaborator.getGroupOrPIName() == null ? "" : collaborator.getGroupOrPIName();
				groupPIText.setText(value);
				value = collaborator.getDepartment() == null ? "" : collaborator.getDepartment();
				departmentText.setText(value);
				value = collaborator.getInstitution() == null ? "" : collaborator.getInstitution();
				institutionText.setText(value);
				value = collaborator.getAddress() == null ? "" : collaborator.getAddress();
				addressText.setText(value);
				value = collaborator.getCountry() == null ? "" : collaborator.getCountry();
				countryText.setText(value);
				value = collaborator.getEmail() == null ? "" : collaborator.getEmail();
				emailText.setText(value);
				value = collaborator.getPhone() == null ? "" : collaborator.getPhone();
				phoneText.setText(value);
				value = collaborator.getFax() == null ? "" : collaborator.getFax();
				faxText.setText(value);
				value = collaborator.getGrantNumber() == null ? "" : collaborator.getGrantNumber();
				grantText.setText(value);
				selectValueInCombo(positionCombo, collaborator.getPosition());
				selectValueInCombo(fundingCombo, collaborator.getFundingAgency());

				this.collabPart = collabPart;
				this.collaborator = collaborator;
				enableToolItem();
				makeEditable(true);
				modelService.bringToTop(collabPart);
			}
		}
	}

	private void enableToolItem()
	{
		((MDirectToolItem) collabPart.getToolbar()
				.getChildren().iterator().next()).setEnabled(
						SaveCollaborator.isUnique(collaborator));
	}

	private void clearAll()
	{
		this.collaborator = null;

		nameText.setText("");
		groupPIText.setText("");
		departmentText.setText("");
		institutionText.setText("");
		addressText.setText("");
		countryText.setText("");
		emailText.setText("");
		phoneText.setText("");
		faxText.setText("");
		grantText.setText("");
		positionCombo.getCombo().deselectAll();
		fundingCombo.getCombo().deselectAll();

		makeEditable(false);
	}

	@PostConstruct
	public void postConstruct(Composite parent)
	{
		logger.debug("START : Creating Collaborator View");
		Composite composite = new Composite(parent, SWT.BORDER);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 20;
		layout.horizontalSpacing = 10;
		layout.marginTop = 30;
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		composite.setLayout(layout);

		nameText = createTextLine(composite, "Name*");
		nameErrorDecoration = new ControlDecoration(nameText, SWT.LEFT);
		nameErrorDecoration.setImage(FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
				.getImage());
		nameErrorDecoration.hide();
		nameText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				nameErrorDecoration.hide();
				if(collaborator!= null)
				{
					String name = ((Text) e.getSource()).getText().trim();
					if(!Objects.equals(name, collaborator.getName()))
					{
						String invalidMessage = validate(name);
						if(invalidMessage == null)
						{
							collaborator.setName(name);
							enableToolItem();
						}
						else
						{
							nameErrorDecoration.show();
						}
						nameErrorDecoration.setDescriptionText(invalidMessage);
						eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, collaborator);
					}
				}
			}
		});

		groupPIText = createTextLine(composite, "Group/P.I.");
		groupPIText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				if(collaborator!= null)
				{
					String groupPI = ((Text) e.getSource()).getText().trim();
					if(!Objects.equals(groupPI, collaborator.getGroupOrPIName()))
					{
						collaborator.setGroupOrPIName(groupPI);
						eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, collaborator);
					}
				}
			}
		});

		positionCombo = createComboLine(composite, "Position");
		positionCombo.setContentProvider(new ArrayContentProvider());
		setInputInCombo(positionCombo, ProjectPreferenceStore.getSingleChoicePreference(
				ProjectPreferenceStore.Preference.POSITION).getAllValues());
		positionCombo.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				if(collaborator!= null)
				{
					StructuredSelection selection = (StructuredSelection) event.getSelection();
					if(selection.getFirstElement() != null)
					{
						String position = (String) selection.getFirstElement();
						if(position.equals(ADD_NEW_TO_PREFERENCE))
						{
							HashMap<String, Object> preferencePageParams = new HashMap<String, Object>();
							preferencePageParams.put(OpenPreferenceHandler.PARAM_PREFERENCE_PAGE_ID,
									PositionPreference.PREFERENCE_PAGE_ID);
							handlerService.executeHandler(commandService.createCommand(
									OpenPreferenceHandler.COMMAND_ID, preferencePageParams));
							setInputInCombo(positionCombo, ProjectPreferenceStore.getSingleChoicePreference(
									ProjectPreferenceStore.Preference.POSITION).getAllValues());

							positionCombo.getCombo().deselectAll();
							int comboSelection = getIndexOf(positionCombo, PositionPreference.lastSelection);
							if(comboSelection >= 0)
								positionCombo.setSelection(new StructuredSelection(PositionPreference.lastSelection));
						}
						else if(!Objects.equals(position, collaborator.getPosition()))
						{
							collaborator.setPosition(position);
							eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, collaborator);
						}
					}
				}
			}
		});

		departmentText = createTextLine(composite, "Department");
		departmentText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				if(collaborator!= null)
				{
					String department = ((Text) e.getSource()).getText().trim();
					if(!Objects.equals(department, collaborator.getDepartment()))
					{
						collaborator.setDepartment(department);
						eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, collaborator);
					}
				}
			}
		});

		institutionText = createTextLine(composite, "Institution");
		institutionText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				if(collaborator!= null)
				{
					String institution = ((Text) e.getSource()).getText().trim();
					if(!Objects.equals(institution, collaborator.getInstitution()))
					{
						collaborator.setInstitution(institution);
						eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, collaborator);
					}
				}
			}
		});

		addressText = createTextLine(composite, "Address", SWT.MULTI|SWT.V_SCROLL|SWT.BORDER);
		((GridData) addressText.getLayoutData()).heightHint = 50;
		addressText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				if(collaborator!= null)
				{
					String address = ((Text) e.getSource()).getText().trim();
					if(!Objects.equals(address, collaborator.getAddress()))
					{
						collaborator.setAddress(address);
						eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, collaborator);
					}
				}
			}
		});

		countryText = createTextLine(composite, "Country");
		countryText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				if(collaborator!= null)
				{
					String country = ((Text) e.getSource()).getText().trim();
					if(!Objects.equals(country, collaborator.getCountry()))
					{
						collaborator.setCountry(country);
						eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, collaborator);
					}
				}
			}
		});

		try
		{
			handler = new NamespaceHandler("country", "preference", "countries.txt");
			trie = handler.getTrieForNamespace();
			contentProposalProvider = 
					new PatriciaTrieContentProposalProvider(trie);
			contentProposalAdapter = 
					new ContentProposalAdapter(countryText, 
							new TextContentAdapter(), contentProposalProvider, null, null);
			contentProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		} catch (Exception ex)
		{
			logger.fatal(ex.getMessage(), ex);
		}

		countryText.addTraverseListener(new TraverseListener()
		{
			@Override
			public void keyTraversed(TraverseEvent e)
			{
				if (e.keyCode == SWT.ESC)
				{
					e.doit = false;
					if(collaborator != null
							&& contentProposalAdapter != null 
							&& contentProposalAdapter.isProposalPopupOpen())
					{
						countryText.setText("");
					}
					e.detail = SWT.TRAVERSE_NONE;
				}
			}
		});

		emailText = createTextLine(composite, "Email");
		emailText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				if(collaborator!= null)
				{
					String email = ((Text) e.getSource()).getText().trim();
					if(!Objects.equals(email, collaborator.getEmail()))
					{
						collaborator.setEmail(email);
						eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, collaborator);
					}
				}
			}
		});

		phoneText = createTextLine(composite, "Phone");
		phoneText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				if(collaborator!= null)
				{
					String phone = ((Text) e.getSource()).getText().trim();
					if(!Objects.equals(phone, collaborator.getPhone()))
					{
						collaborator.setPhone(phone);
						eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, collaborator);
					}
				}
			}
		});

		faxText = createTextLine(composite, "Fax");
		faxText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				if(collaborator!= null)
				{
					String fax = ((Text) e.getSource()).getText().trim();
					if(!Objects.equals(fax, collaborator.getFax()))
					{
						collaborator.setFax(fax);
						eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, collaborator);
					}
				}
			}
		});

		fundingCombo = createComboLine(composite, "Funding");
		fundingCombo.setContentProvider(new ArrayContentProvider());
		setInputInCombo(fundingCombo, ProjectPreferenceStore.getSingleChoicePreference(
				ProjectPreferenceStore.Preference.FUNDING).getAllValues());
		fundingCombo.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				if(collaborator!= null)
				{
					StructuredSelection selection = (StructuredSelection) event.getSelection();
					if(selection.getFirstElement() != null)
					{
						String funding = (String) selection.getFirstElement();
						if(funding.equals(ADD_NEW_TO_PREFERENCE))
						{
							HashMap<String, Object> preferencePageParams = new HashMap<String, Object>();
							preferencePageParams.put(OpenPreferenceHandler.PARAM_PREFERENCE_PAGE_ID,
									FundingPreference.PREFERENCE_PAGE_ID);
							handlerService.executeHandler(commandService.createCommand(
									OpenPreferenceHandler.COMMAND_ID, preferencePageParams));
							setInputInCombo(fundingCombo, ProjectPreferenceStore.getSingleChoicePreference(
									ProjectPreferenceStore.Preference.FUNDING).getAllValues());

							fundingCombo.getCombo().deselectAll();
							int comboSelection = getIndexOf(fundingCombo, FundingPreference.lastSelection);
							if(comboSelection >= 0)
								fundingCombo.setSelection(new StructuredSelection(FundingPreference.lastSelection));
						}
						else if(!Objects.equals(funding, collaborator.getFundingAgency()))
						{
							collaborator.setFundingAgency(funding);
							enableToolItem();
							eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, collaborator);
						}
					}
				}
			}
		});

		grantText = createTextLine(composite, "Grant No.");
		grantText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				if(collaborator!= null)
				{
					String grantNumber = ((Text) e.getSource()).getText().trim();
					if(!Objects.equals(grantNumber, collaborator.getGrantNumber()))
					{
						collaborator.setGrantNumber(grantNumber);
						enableToolItem();
						eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, collaborator);
					}
				}
			}
		});

		GridData compositeLayoutData = new GridData(GridData.FILL_BOTH);
		compositeLayoutData.grabExcessHorizontalSpace = true;
		compositeLayoutData.grabExcessVerticalSpace = false;
		compositeLayoutData.horizontalSpan = 3;
		compositeLayoutData.verticalSpan = 1;
		composite.setLayoutData(compositeLayoutData);

		makeEditable(false);

		logger.debug("END   : Creating Collaborator View");
	}

	private Text createTextLine(Composite composite, String label)
	{
		return createTextLine(composite, label, SWT.BORDER);
	}

	private Text createTextLine(Composite composite, String label, int style)
	{
		Label textLabel = new Label(composite, SWT.NONE);
		textLabel.setText(label);
		GridData createNewData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		createNewData.horizontalSpan = 1;
		createNewData.verticalSpan = 1;
		textLabel.setLayoutData(createNewData);

		Text text = new Text(composite, style);
		GridData newCollaboratorData = new GridData(GridData.FILL_HORIZONTAL);
		newCollaboratorData.grabExcessHorizontalSpace = true;
		newCollaboratorData.horizontalSpan = 1;
		newCollaboratorData.verticalSpan = 1;
		//		newCollaboratorData.heightHint = heightHint;// , SWT.BORDER, 20
		text.setLayoutData(newCollaboratorData);
		return text;
	}

	private ComboViewer createComboLine(Composite composite, String label)
	{
		Label textLabel = new Label(composite, SWT.NONE);
		textLabel.setText(label);
		GridData createNewData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		createNewData.horizontalSpan = 1;
		createNewData.verticalSpan = 1;
		textLabel.setLayoutData(createNewData);

		ComboViewer comboViewer = new ComboViewer(composite, SWT.READ_ONLY);
		GridData newCollaboratorData = new GridData(GridData.FILL_HORIZONTAL);
		newCollaboratorData.grabExcessHorizontalSpace = true;
		newCollaboratorData.horizontalSpan = 1;
		newCollaboratorData.verticalSpan = 1;
		comboViewer.getCombo().setLayoutData(newCollaboratorData);
		return comboViewer;
	}

	private void setInputInCombo(ComboViewer comboViewer, Set<String> values) 
	{
		List<String> valueList = new ArrayList<String>();
		valueList.addAll(values);
		Collections.sort(valueList);
		String[] items = new String[valueList.size() + 1];
		int i = 0;
		for(String value : valueList)
		{
			items[i++] = value;
		}
		items[i] = ProjectEntryPart.ADD_NEW_TO_PREFERENCE;
		comboViewer.setInput(items);
	}

	private void selectValueInCombo(ComboViewer comboViewer, String value)
	{
		comboViewer.getCombo().deselectAll();
		if(value != null)
		{
			int selectionIndex = getIndexOf(comboViewer, value);
			if(selectionIndex >= 0)
			{
				comboViewer.getCombo().select(selectionIndex);
				comboViewer.setSelection(comboViewer.getSelection());
			}
		}
	}

	private int getIndexOf(ComboViewer comboViewer, String value)
	{
		int selectionIndex = -1;
		if(comboViewer != null 
				&& comboViewer.getCombo().getItems() != null && value != null)
		{
			selectionIndex = comboViewer.getCombo().indexOf(value);
		}
		return selectionIndex;
	}

	private void makeEditable(boolean editable)
	{
		nameText.setEnabled(editable);
		groupPIText.setEnabled(editable);
		departmentText.setEnabled(editable);
		institutionText.setEnabled(editable);
		addressText.setEnabled(editable);
		countryText.setEnabled(editable);
		emailText.setEnabled(editable);
		phoneText.setEnabled(editable);
		faxText.setEnabled(editable);
		grantText.setEnabled(editable);

		positionCombo.getCombo().setEnabled(editable);
		fundingCombo.getCombo().setEnabled(editable);
	}

	private String validate(String value)
	{
		return value.isEmpty() ? "Value is empty" : null;
	}

	@Focus
	public void onFocus()
	{
		
	}

	@Persist
	public void save()
	{
		
	}

	public ProjectCollaborator getCollaborator()
	{
		return collaborator;
	}
}