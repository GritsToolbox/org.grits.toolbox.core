/**
 * 
 */
package org.grits.toolbox.core.wizard.addcollaborator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.grits.toolbox.core.datamodel.property.project.ProjectCollaborator;
import org.grits.toolbox.core.part.toolitem.SaveCollaborator;
import org.grits.toolbox.core.wizard.addcollaborator.pages.AddNewCollaborator;
import org.grits.toolbox.core.wizard.addcollaborator.pages.SelectCollaborator;

/**
 * 
 *
 */
public class AddCollaboratorWizard extends Wizard
{
	private List<ProjectCollaborator> collaborators = null;
	private SelectCollaborator pageOne = new SelectCollaborator();
	private AddNewCollaborator pageTwo = new AddNewCollaborator();

	@Override
	public void addPages()
	{
		addPage(pageOne);
		addPage(pageTwo);
		super.addPages();
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page)
	{
		return page == pageOne ? pageTwo : null;
	}
	@Override
	public boolean performFinish()
	{
		collaborators = new ArrayList<ProjectCollaborator>();
		if(getContainer().getCurrentPage() == pageOne 
				&& pageOne.selectFromButton.getSelection())
		{
			collaborators.addAll(pageOne.getSelectedCollaborators());
		}
		else if(getContainer().getCurrentPage() == pageTwo 
				&& pageTwo.isPageComplete())
		{
			ProjectCollaborator newCollaborator = pageTwo.getCollaborator();
			collaborators.add(newCollaborator);
			if(pageTwo.addToPreference.getSelection())
			{
				addCollaboratorToPreference(newCollaborator);
			}
		}
		return true;
	}

	private void addCollaboratorToPreference(ProjectCollaborator newCollaborator)
	{
		SaveCollaborator.saveToPreference(newCollaborator, true);
	}

	@Override
	public boolean canFinish()
	{
		if(getContainer().getCurrentPage() == pageOne)
		{
			return pageOne.selectFromButton.getSelection() 
					&& !pageOne.getSelectedCollaborators().isEmpty();
		}
		else if(getContainer().getCurrentPage() == pageTwo)
		{
			return pageTwo != null 
			&& pageTwo.isPageComplete();
		}
		return false;
	}

	public List<ProjectCollaborator> getCollaborators()
	{
		return this.collaborators ;
	}
}
