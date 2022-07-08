/**
 * 
 */
package org.grits.toolbox.core.wizard.taskexport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.ProjectDetailsHandler;
import org.grits.toolbox.core.datamodel.property.project.ProjectCollaborator;
import org.grits.toolbox.core.datamodel.property.project.ProjectContribution;
import org.grits.toolbox.core.datamodel.property.project.ProjectContributor;
import org.grits.toolbox.core.datamodel.property.project.ProjectDetails;
import org.grits.toolbox.core.datamodel.property.project.ProjectTasklist;
import org.grits.toolbox.core.utilShare.CheckboxTableViewer;
import org.grits.toolbox.core.wizard.taskexport.pages.SelectColumnPage;
import org.grits.toolbox.core.wizard.taskexport.pages.TaskFilterPage;
import org.grits.toolbox.widgets.processDialog.GRITSProgressDialog;
import org.grits.toolbox.widgets.progress.CancelableThread;
import org.grits.toolbox.widgets.progress.IProgressThreadHandler;
import org.grits.toolbox.widgets.progress.IProgressListener.ProgressType;
import org.grits.toolbox.widgets.tools.GRITSProcessStatus;
import org.grits.toolbox.widgets.tools.GRITSWorker;

/**
 * 
 *
 */
public class TaskExportWizard extends Wizard
{
	private static Logger logger = Logger.getLogger(TaskExportWizard.class);
	private TaskFilterPage pageOne = new TaskFilterPage();
	private SelectColumnPage pageTwo = new SelectColumnPage();
	private ArrayList<ProjectContributor> contributors = null;
	private String savingLocation = null;
	private ArrayList<String> selectedColumns;

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
		try {
			prepareExport();
		} catch (Exception e1) {
			((WizardPage) 
					getContainer().getCurrentPage()).setErrorMessage(
							(e1.getMessage()));
			logger.error (e1.getMessage(), e1);
			return false;
		}
		GRITSProgressDialog progressDialog = new GRITSProgressDialog(getShell(), 1, true);
        progressDialog.open();
        progressDialog.getMajorProgressBarListener().setMaxValue(2);
        progressDialog.setGritsWorker(new GRITSWorker() {
        	
            @Override
            public int doWork() {
                try {
                    updateListeners("Exporting...", 1);
                   
                    CancelableThread t = new CancelableThread() {
                        @Override
                        public boolean threadStart(IProgressThreadHandler a_progressThreadHandler) throws Exception {
                            try {
                            	if (selectedColumns != null)
                            		return exportToExcel(selectedColumns, progressDialog);
                            	else 
                            		return false;
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                                return false;
                            }
                        }
                    };
                    t.setProgressThreadHandler(progressDialog);
                    progressDialog.setThread(t);
                    progressDialog.getMinorProgressBarListener(0).setProgressType(ProgressType.Determinant);
                    t.start();  
                    while ( ! t.isCanceled() && ! t.isFinished() && t.isAlive() ) 
                    {
                        if (!Display.getDefault().readAndDispatch()) 
                        {
                        //    Display.getDefault().sleep();
                        }
                    }
                    if( t.isCanceled() ) {
                        t.interrupt();
                        return GRITSProcessStatus.CANCEL;
                    } 
                } catch( Exception e ) {
                    logger.error(e.getMessage(), e);
                }
                updateListeners("Done", 2);
                return GRITSProcessStatus.OK;
            }
             
        });
        progressDialog.startWorker();
        return true;
		
	}
	
	private void prepareExport() throws Exception {
		boolean locationError = false;
		try
		{
			savingLocation  = pageOne.locationText.getText();
			File inputFile = new File(savingLocation);
			FileOutputStream fos = new FileOutputStream(inputFile);
			fos.close();
			locationError = 
					!(inputFile.getParentFile().exists() 
							&& inputFile.getParentFile().isDirectory());
		} catch (Exception ex)
		{
			logger.error(ex, ex);
			throw new Exception("The location is not correct. Please select a correct location", ex);
		}
		if(locationError)
		{
			String message = "The location is not correct. Please select a correct location";
			//((WizardPage) 
			//		getContainer().getCurrentPage()).setErrorMessage(
			//				(message));
			logger.error(message);
			throw new Exception (message);
		}
		if(pageOne.projectTableViewer.getCheckedElements().length > 0
				&& pageOne.statusTableViewer.getCheckedElements().length > 0)
		{
			exportContributors();
			if(getContainer().getCurrentPage() == pageTwo 
					&& pageTwo.isPageComplete())
			{
				saveExportColumnNames();
			}	
		}
		selectedColumns = new ArrayList<String>();
		int j = 0;
		for(int i = 0; i < pageTwo.columnsToExport.length ; i++)
		{
			if(pageTwo.columnsToExport[i].getSelection())
			{
				selectedColumns.add(j, pageTwo.columnsToExport[i].getText());
				j++;
			}
		}
	}

	private void saveExportColumnNames() 
	{

	}

	private void exportContributors() throws Exception 
	{
		contributors = new ArrayList<ProjectContributor>();
		CheckboxTableViewer projectTableViewer = pageOne.projectTableViewer;
		CheckboxTableViewer statusTableViewer = pageOne.statusTableViewer;

		List<String> alreadyAdded = new ArrayList<String>();
		String workspaceLocationFolder = PropertyHandler.getVariable("workspace_location");
		workspaceLocationFolder = workspaceLocationFolder.substring(0, workspaceLocationFolder.length());
		ProjectContributor contributor = null;
		ProjectContribution projectContribution = null;
		String collaborator = null;
		List<String> checkedStatus = new ArrayList<String>();
		for(Object status : statusTableViewer.getCheckedElements())
		{
			checkedStatus.add((String) status);
		}
		for(Object pj : projectTableViewer.getCheckedElements())
		{
			if(pj instanceof Entry)
			{
				Entry projectEntry = ((Entry) pj);
				ProjectDetails projectDetails = null;
				try
				{
					projectDetails = ProjectDetailsHandler.getProjectDetails(projectEntry);

					if(projectDetails != null)
					{
						collaborator = "";
						for(ProjectCollaborator coll : projectDetails.getCollaborators())
						{
							collaborator += coll.getName();
							if(coll.getAddress() != null)
							{
								collaborator += "\n" + coll.getAddress();
							}
							collaborator += "\n----x----\n";
						}
						if(!collaborator.isEmpty())
						{
							collaborator = collaborator.substring(0, collaborator.length()-2);
						}
						for(ProjectTasklist taskList: projectDetails.getTasklists())
						{
							if(checkedStatus.contains(taskList.getStatus()))
							{
								if(alreadyAdded.contains(taskList.getPerson()))
								{
									for(ProjectContributor contbtr : contributors)
									{
										if(contbtr.getName().equals(taskList.getPerson()))
										{
											contributor = contbtr;
										}
									}
								}
								else
								{
									contributor = new ProjectContributor();
									contributor.setName(taskList.getPerson());

									contributors.add(contributor);
									alreadyAdded.add(taskList.getPerson());
								}

								projectContribution = new ProjectContribution();

								projectContribution.setProjectName(projectEntry.getDisplayName());
								projectContribution.setGroupPI(taskList.getGroupOrPIName());
								projectContribution.setRole(taskList.getRole());
								projectContribution.setTask(taskList.getTask());
								projectContribution.setStatus(taskList.getStatus());
								projectContribution.setProjectCollaborator(collaborator);
								projectContribution.setDueDate(taskList.getDueDate());
								projectContribution.setNumberOfTasks(taskList.getNumberOfTasks());

								contributor.addContribution(projectContribution);
							}
						}
					}
				} catch (IOException e)
				{
					logger.error(e.getMessage(), e);
					throw e;
				}
			}
		}
	}

	@Override
	public boolean canFinish()
	{
		if(getContainer().getCurrentPage() == pageOne)
		{
			return pageOne.projectTableViewer.getCheckedElements().length > 0;
		}
		else if(getContainer().getCurrentPage() == pageTwo)
		{
			return pageTwo != null 
					&& pageTwo.isPageComplete();
		}
		return false;
	}

	public boolean exportToExcel(List<String> selectedColumns, GRITSProgressDialog progressDialog)
	{
		boolean exported = false;
		try 
		{
			FileOutputStream fos = new FileOutputStream(new File(savingLocation));
			XSSFWorkbook workbook = new XSSFWorkbook();

			XSSFCellStyle wrapStyle = workbook.createCellStyle();
			wrapStyle.setWrapText(true);

			XSSFCellStyle boldStyle = workbook.createCellStyle();
			XSSFFont boldFont = workbook.createFont();
			boldFont.setBold(true);
			boldStyle.setFont(boldFont);
			boldStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
			boldStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFCellStyle boldHeaderStyle = workbook.createCellStyle();
			boldHeaderStyle.setFont(boldFont);
			boldHeaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			boldHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			Sheet sheet = null;
			SimpleDateFormat dateFormat = new SimpleDateFormat(ProjectContribution.DATE_FORMAT);
			int num = 0;
			int totalNum = contributors.size();
			progressDialog.getMinorProgressBarListener(0).setMaxValue(totalNum);
			progressDialog.getMinorProgressBarListener(0).setProgressValue(0);
			for(ProjectContributor contributor : contributors)
			{
				if (progressDialog.isCanceled()) {
					fos.close();
					fos.getChannel().close();
					workbook.close();
					return false;
				}
				
				progressDialog.getMinorProgressBarListener(0).setProgressValue(num+1);
				progressDialog.getMinorProgressBarListener(0).setProgressMessage("Filtering row: " + (num+1) + " of " + totalNum);

				sheet = workbook.createSheet(contributor.getName());
				sheet.createRow(0).createCell(0).setCellValue(contributor.getName());
				sheet.getRow(0).getCell(0).setCellStyle(boldStyle);

				int indexRow = 1; 
				Row thisRow = null;
				thisRow = sheet.createRow(indexRow);
				

				for(int i = 0 ; i < selectedColumns.size() ; i++)
				{
					thisRow.createCell(i).setCellValue(selectedColumns.get(i));
				}

				for(int i = 0; i < selectedColumns.size(); i++)
				{
					thisRow.getCell(i).setCellStyle(boldHeaderStyle);
				}
				indexRow += 1;

				for(ProjectContribution contribution : contributor.getContributions())
				{
					thisRow = sheet.createRow(indexRow);
					thisRow.setRowStyle(wrapStyle);

					for(int i = 0 ; i < selectedColumns.size() ; i++)
					{
						Object value = null;
						switch (selectedColumns.get(i)) 
						{
						case SelectColumnPage.PROJECT :
							value = contribution.getProjectName();
							break;
						case SelectColumnPage.NAME_ADDRESS :
							value = contribution.getProjectCollaborator();
							break;
						case SelectColumnPage.GROUP_PI :
							value = contribution.getGroupPI();
							break;
						case SelectColumnPage.ROLE :
							value = contribution.getRole();
							break;
						case SelectColumnPage.TASK :
							value = contribution.getTask();
							break;
						case SelectColumnPage.STATUS :
							value = contribution.getStatus();
							break;
						case SelectColumnPage.NUMBER_OF_SAMPLES :
							value = contribution.getNumberOfTasks();
							break;
						case SelectColumnPage.COMPLETION_DATE :
							value = contribution.getDueDate() != null
							? dateFormat.format(contribution.getDueDate()) : null;
							break;
						default:
							break;
						}
						if(value != null)
						{
							if(value instanceof String)
								thisRow.createCell(i).setCellValue((String) value);
							else if(value instanceof Integer)
								thisRow.createCell(i).setCellValue((Integer) value);
							thisRow.getCell(i).setCellStyle(wrapStyle);
						}
					}

					indexRow += 2;
				}
				for(int i =0; i < selectedColumns.size(); i++)
				{
					sheet.autoSizeColumn(i);
				}
				num++;
			}
			progressDialog.getMinorProgressBarListener(0).setProgressValue(totalNum);
	        progressDialog.getMinorProgressBarListener(0).setProgressMessage("Done!");
			workbook.write(fos);
			fos.close();
			fos.getChannel().close();
			workbook.close();
			exported = true;
		} catch (FileNotFoundException ex) {
			logger.error(ex.getMessage(), ex);
			if(ex.getMessage().contains("The process cannot access the file "
					+ "because it is being used by another process"))
			{
				MessageDialog.openError(Display.getCurrent().getActiveShell(), 
						"Error", "The file is already in use. Please close it first and then try again");
			}
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}
		return exported;
	}
}
