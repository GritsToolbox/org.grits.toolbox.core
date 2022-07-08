/**
 * 
 */
package org.grits.toolbox.core.wizard.addcollaborator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.log4j.Logger;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.typeahead.NamespaceHandler;
import org.grits.toolbox.core.typeahead.PatriciaTrieContentProposalProvider;

/**
 * 
 *
 */
public class ProjectCollaboratorUI
{
    private static Logger logger = Logger.getLogger(ProjectCollaboratorUI.class);

    private ProjectCollaborator collaborator = null;
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
    private NamespaceHandler handler = null;
    private PatriciaTrie<String> trie = null;
    private PatriciaTrieContentProposalProvider contentProposalProvider = null;
    private ContentProposalAdapter contentProposalAdapter = null;
    
    private CollaboratorEditListener collaboratorEditListener = null;

	private Composite composite;

	private String errorMessage;

	protected ControlDecoration nameErrorDecoration = null;

	private String[] positions;
	private String[] fundings;
    
    /**
     * creates the Project Collaborator UI
     * @param collaboratorEditListener listener which performs tasks after edit
     * this parameter cannot be null
     * @throws Exception 
     */
    public ProjectCollaboratorUI(
    		CollaboratorEditListener collaboratorEditListener)
    {
    	this.collaboratorEditListener = collaboratorEditListener;
        List<String> sortedPositions = 
        		new ArrayList<String>(ProjectPreferenceStore.getSingleChoicePreference(
					ProjectPreferenceStore.Preference.POSITION).getAllValues());
        Collections.sort(sortedPositions);
        positions = getArrayFromList(sortedPositions);

        List<String> sortedFundingAgencies = 
        		new ArrayList<String>(ProjectPreferenceStore.getSingleChoicePreference(
					ProjectPreferenceStore.Preference.FUNDING).getAllValues());
        Collections.sort(sortedFundingAgencies);
        fundings = getArrayFromList(sortedFundingAgencies);
	}
    
    private String[] getArrayFromList(List<String> listOfValue) {
    	String[] arrayOfValue = new String[listOfValue.size()];
    	int i = 0;
    	for(String value : listOfValue)
    	{
    		arrayOfValue[i] = value;
    		i++;
    	}
    	return arrayOfValue;
	}

	public ProjectCollaborator getCollaborator()
    {
		return this.collaborator;
	}

    public void setCollaborator(ProjectCollaborator collaborator)
    {
        clearAll();
        this.collaborator = collaborator;
        nameText.setText(collaborator.getName());
        if(collaborator.getGroupOrPIName() != null)
            groupPIText.setText(collaborator.getGroupOrPIName());
        if(collaborator.getDepartment() != null)
            departmentText.setText(collaborator.getDepartment());
        if(collaborator.getInstitution() != null)
            institutionText.setText(collaborator.getInstitution());
        if(collaborator.getAddress() != null)
            addressText.setText(collaborator.getAddress());
        if(collaborator.getCountry() != null)
            countryText.setText(collaborator.getCountry());
        if(collaborator.getEmail() != null)
            emailText.setText(collaborator.getEmail());
        if(collaborator.getPhone() != null)
            phoneText.setText(collaborator.getPhone());
        if(collaborator.getFax() != null)
            faxText.setText(collaborator.getFax());
        if(collaborator.getGrantNumber() != null)
            grantText.setText(collaborator.getGrantNumber());

        String positionSelection = (collaborator.getPosition() != null 
                && !collaborator.getPosition().isEmpty()) ?
                		collaborator.getPosition() : null;

        String fundingSelection = (collaborator.getFundingAgency() != null 
                && !collaborator.getFundingAgency().isEmpty()) ?
                		collaborator.getFundingAgency() : null;

        if(positions.length > 0 && positionSelection != null)
        {
            int i = 0;
            int selectionIndex = -1;
            for(String position : positions)
            {
                if(positionSelection != null 
                        && position.equals(positionSelection))
                {
                    selectionIndex = i;
                }
                i++;
            }
            positionCombo.setInput(positions);
            if(selectionIndex >= 0)
                positionCombo.getCombo().select(selectionIndex);
            positionCombo.setSelection(positionCombo.getSelection());
        }

        if(fundings.length > 0 && fundingSelection != null)
        {
            int i = 0;
            int selectionIndex = -1;
            for(String fundingAgency : fundings)
            {
                if(fundingSelection != null 
                        && fundingAgency.equals(fundingSelection))
                {
                    selectionIndex = i;
                }
                i++;
            }
            fundingCombo.setInput(fundings);
            if(selectionIndex >= 0)
                fundingCombo.getCombo().select(selectionIndex);
            fundingCombo.setSelection(fundingCombo.getSelection());
        }

        nameText.selectAll();
        nameText.setFocus();
    }

    public void clearAll()
    {
        collaborator = null;
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
    }
    
    public Composite getComposite() {
		return composite;
	}

    public void createPartControl(Composite parent)
    {
        ScrolledComposite scrolledComposite = 
        		new ScrolledComposite(parent, SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setLayout(new GridLayout());

        GridData scrolledCompositeData = new GridData(GridData.FILL_BOTH);
        scrolledCompositeData.horizontalSpan = 1;
        scrolledCompositeData.verticalSpan = 1;
        scrolledComposite.setLayoutData(scrolledCompositeData);

        scrolledComposite.setMinSize(400, 580);

        composite = new Composite(scrolledComposite, SWT.FILL);
        GridLayout layout = new GridLayout();
        layout.marginRight = 10;
        layout.marginLeft = 10;
        layout.marginTop = 20;
        layout.marginBottom = 20;
        layout.verticalSpacing = 20;
        layout.horizontalSpacing = 10;
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = false;
        composite.setLayout(layout);
        scrolledComposite.setContent(composite);

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
                if(collaborator != null)
                {
                    String name = ((Text) e.getSource()).getText().trim();
                    String invalidMessage = validate(name);
                    if(invalidMessage == null)
                    {
                        collaborator.setName(name);
                    }
                    else
                    {
                    	nameErrorDecoration.show();
                    }
                    nameErrorDecoration.setDescriptionText(invalidMessage);
                	errorMessage = invalidMessage;
                    collaboratorEditListener.valueEdited();
                }
            }
        });

        groupPIText = createTextLine(composite, "Group/P.I.");
        groupPIText.addModifyListener(new ModifyListener()
        {
            @Override
            public void modifyText(ModifyEvent e)
            {
                if(collaborator != null)
                {
                    String groupPI = ((Text) e.getSource()).getText().trim();
                    collaborator.setGroupOrPIName(groupPI);
                    collaboratorEditListener.valueEdited();
                }
            }
        });

        positionCombo = createComboLine(composite, "Position");
        positionCombo.setContentProvider(new ArrayContentProvider());
        positionCombo.addSelectionChangedListener(new ISelectionChangedListener()
        {
            @Override
            public void selectionChanged(SelectionChangedEvent event)
            {
                int selectionIndex = positionCombo.getCombo().getSelectionIndex();
                if(selectionIndex >= 0)
                {
                    if(collaborator != null)
                    {
                        String position = positionCombo.getCombo().getItem(selectionIndex);
                        collaborator.setPosition(position);
                        collaboratorEditListener.valueEdited();
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
                if(collaborator != null)
                {
                    String department = ((Text) e.getSource()).getText().trim();
                    collaborator.setDepartment(department);
                    collaboratorEditListener.valueEdited();
                }
            }
        });

        institutionText = createTextLine(composite, "Institution");
        institutionText.addModifyListener(new ModifyListener()
        {

            @Override
            public void modifyText(ModifyEvent e)
            {
                if(collaborator != null)
                {
                    String institution = ((Text) e.getSource()).getText().trim();
                    collaborator.setInstitution(institution);
                    collaboratorEditListener.valueEdited();
                }
            }
        });

        addressText = createTextLine(composite, "Address", SWT.MULTI|SWT.V_SCROLL);
        ((GridData) addressText.getLayoutData()).heightHint = 50;
        addressText.addModifyListener(new ModifyListener()
        {

            @Override
            public void modifyText(ModifyEvent e)
            {
                if(collaborator != null)
                {
                    String address = ((Text) e.getSource()).getText().trim();
                    collaborator.setAddress(address);
                    collaboratorEditListener.valueEdited();
                }
            }
        });

        countryText = createTextLine(composite, "Country");
        countryText.addModifyListener(new ModifyListener()
        {

            @Override
            public void modifyText(ModifyEvent e)
            {
                if(collaborator != null)
                {
                    String country = ((Text) e.getSource()).getText().trim();
                    collaborator.setCountry(country);
                    collaboratorEditListener.valueEdited();
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
            public void keyTraversed(TraverseEvent e) {
                if (e.keyCode == SWT.ESC) {
                    e.doit = false;
                    if(collaborator != null && contentProposalAdapter != null 
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
                if(collaborator != null)
                {
                    String email = ((Text) e.getSource()).getText().trim();
                    collaborator.setEmail(email);
                    collaboratorEditListener.valueEdited();
                }
            }
        });

        phoneText = createTextLine(composite, "Phone");
        phoneText.addModifyListener(new ModifyListener()
        {

            @Override
            public void modifyText(ModifyEvent e)
            {
                if(collaborator != null)
                {
                    String phone = ((Text) e.getSource()).getText().trim();
                    collaborator.setPhone(phone);
                    collaboratorEditListener.valueEdited();
                }
            }
        });

        faxText = createTextLine(composite, "Fax");
        faxText.addModifyListener(new ModifyListener()
        {

            @Override
            public void modifyText(ModifyEvent e)
            {
                if(collaborator != null)
                {
                    String fax = ((Text) e.getSource()).getText().trim();
                    collaborator.setFax(fax);
                    collaboratorEditListener.valueEdited();
                }
            }
        });

        fundingCombo = createComboLine(composite, "Funding");
        fundingCombo.setContentProvider(new ArrayContentProvider());
        fundingCombo.addSelectionChangedListener(new ISelectionChangedListener()
        {
            @Override
            public void selectionChanged(SelectionChangedEvent event)
            {
                if(collaborator != null)
                {
                    int selectionIndex = fundingCombo.getCombo().getSelectionIndex();
                    if(selectionIndex >= 0)
                    {
                        String funding = fundingCombo.getCombo().getItem(selectionIndex);
                        collaborator.setFundingAgency(funding);
                        collaboratorEditListener.valueEdited();
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
                if(collaborator != null)
                {
                    String grantNumber = ((Text) e.getSource()).getText().trim();
                    collaborator.setGrantNumber(grantNumber);
                    collaboratorEditListener.valueEdited();
                }
            }
        });

        GridData compositeLayoutData = new GridData(GridData.FILL_BOTH);
        compositeLayoutData.horizontalSpan = 1;
        compositeLayoutData.verticalSpan = 1;
        composite.setLayoutData(compositeLayoutData);
    }

    protected String validate(String value)
    {
		return value.isEmpty() ? "Value is empty" : null;
	}

	private Text createTextLine(Composite composite, String label)
    {
    	return createTextLine(composite, label, SWT.NONE);
    }

    private Text createTextLine(Composite composite, String label, int style)
    {
        Label textLabel = new Label(composite, SWT.NONE);
        textLabel.setText(label);
        GridData createNewData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING 
        		| GridData.VERTICAL_ALIGN_BEGINNING);
        createNewData.horizontalSpan = 1;
        createNewData.verticalSpan = 1;
        textLabel.setLayoutData(createNewData);

        Text text = new Text(composite, SWT.BORDER | style);
        GridData newcollaboratorData = new GridData(GridData.FILL_HORIZONTAL);
        newcollaboratorData.grabExcessHorizontalSpace = true;
        newcollaboratorData.horizontalSpan = 1;
        newcollaboratorData.verticalSpan = 1;
        text.setLayoutData(newcollaboratorData);
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
        GridData newcollaboratorData = new GridData(GridData.FILL_HORIZONTAL);
        newcollaboratorData.grabExcessHorizontalSpace = true;
        newcollaboratorData.horizontalSpan = 1;
        newcollaboratorData.verticalSpan = 1;
        comboViewer.getCombo().setLayoutData(newcollaboratorData);
        return comboViewer;
    }

    public void setFocus()
    {
        nameText.setFocus();
    }

	public String getErrorMessage()
	{
		return errorMessage;
	}

}