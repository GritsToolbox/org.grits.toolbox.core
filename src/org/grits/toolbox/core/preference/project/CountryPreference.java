/**
 * 
 */
package org.grits.toolbox.core.preference.project;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener2;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore.StringPreference;
import org.grits.toolbox.core.typeahead.NamespaceHandler;
import org.grits.toolbox.core.typeahead.PatriciaTrieContentProposalProvider;

/**
 * 
 * @author sena
 *
 */
//Modified (Sena) March 2017 to inform the user for errors (ticket #826)
public class CountryPreference extends PreferencePage
{
	private static final Logger logger = Logger.getLogger(CountryPreference.class);

	private Text text = null;
	private NamespaceHandler handler = null;
	private PatriciaTrie<String> trie = null;
	private PatriciaTrieContentProposalProvider contentProposalProvider = null;
	private ContentProposalAdapter contentProposalAdapter = null;

	private StringPreference countryPreference = null;

	protected IContentProposal[] proposals = null;

	public void initializeValue()
	{
		logger.info("Loading preference " + ProjectPreferenceStore.StringPreference.COUNTRY);

		countryPreference  = ProjectPreferenceStore.StringPreference.COUNTRY;
		String defaultCountry = countryPreference.getValue() == null ? "" : countryPreference.getValue();
		text.setText(defaultCountry);
	}

	@Override
	protected Control createContents(Composite parent)
	{
		Composite container = new Composite(parent, SWT.FILL);
		GridLayout layout = new GridLayout();
		layout.marginRight = 8;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 10;
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		container.setLayout(layout);

		Label label = new Label(container, SWT.NONE);
		label.setText("Default Country");
		GridData selectExistingData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		selectExistingData.horizontalSpan = 1;
		selectExistingData.verticalSpan = 1;
		label.setLayoutData(selectExistingData);

		text = new Text(container, SWT.BORDER);
		GridData newcollaboratorData = new GridData(GridData.FILL_HORIZONTAL);
		newcollaboratorData.grabExcessHorizontalSpace = true;
		newcollaboratorData.horizontalSpan = 1;
		newcollaboratorData.verticalSpan = 1;
		text.setLayoutData(newcollaboratorData);

		try
		{
			handler = new NamespaceHandler("country", "preference", "countries.txt");
			trie = handler.getTrieForNamespace();
			contentProposalProvider = 
					new PatriciaTrieContentProposalProvider(trie);
			contentProposalAdapter = 
					new ContentProposalAdapter(text, 
							new TextContentAdapter(), contentProposalProvider, null, null);
			contentProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
			// Listen for popup open/close events to be able to handle focus events correctly
			contentProposalAdapter.addContentProposalListener(new IContentProposalListener2()
			{
				@Override
				public void proposalPopupOpened(ContentProposalAdapter adapter)
				{
					proposals = contentProposalAdapter.getContentProposalProvider().getProposals(
							contentProposalAdapter.getControlContentAdapter().getControlContents(text), 
							contentProposalAdapter.getControlContentAdapter().getCursorPosition(text));
				}
				@Override
				public void proposalPopupClosed(ContentProposalAdapter adapter)
				{

				}
			});
		} catch (Exception ex)
		{
			logger.fatal(ex.getMessage(), ex);
		}

		text.addTraverseListener(new TraverseListener()
		{
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.keyCode == SWT.ESC) {
					e.doit = false;
					if(contentProposalAdapter != null 
							&& contentProposalAdapter.isProposalPopupOpen())
						text.setText("");
					e.detail = SWT.TRAVERSE_NONE;
				}
			}
		});

		initializeValue();
		return container;
	}

	@Override
	protected void performDefaults()
	{
		text.setText("");
	}

	@Override
	protected void performApply()
	{
		String errorMessage = validateInput();
		setErrorMessage(errorMessage);
		if(errorMessage == null)
		{
			save();
		}
	}

	private String validateInput()
	{
		String country = text.getText();
		String errorMessage = "Not a valid country";
		if(proposals != null)
		{
			for (IContentProposal proposal : proposals) 
			{
				if (proposal.getContent().equals(country))
				{
					errorMessage = null;
					break;
				}
			}
		}
		else
			errorMessage = country.trim().isEmpty() ? null : errorMessage;
		return errorMessage;
	}

	@Override
	public boolean performOk()
	{
		String errorMessage = validateInput();
		setErrorMessage(errorMessage);
		if (errorMessage != null) MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", "There is an error in \"Country\" preference values. Please fix before saving! Error: " + errorMessage);
		return errorMessage == null ? save() : false;
	}

	private boolean save()
	{
		String defaultCountry = text.getText().trim();
		defaultCountry = defaultCountry.isEmpty() ? null : defaultCountry;
		countryPreference.setValue(defaultCountry);
		return countryPreference.savePreference();
	}
}
