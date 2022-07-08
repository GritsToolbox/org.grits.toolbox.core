/**
 * 
 */
package org.grits.toolbox.core.projectexplorer.dialog.importentry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.projectexplorer.dialog.ExportEntryDialog;
import org.grits.toolbox.core.utilShare.CheckboxTableViewer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * 
 *
 */
public class ImportEntryDialog extends TitleAreaDialog
{
	private static final Logger logger = Logger.getLogger(ImportEntryDialog.class);

	private ZipFile exportedZipFile = null;
	private List<Element> selectedEntryElements = null;

	private Text locationText = null;
	private CheckboxTableViewer projectTableViewer = null;
	private Button autoRenameButton = null;
	private HashSet<String> existingNames = null;
	private ImportEntryLabelProvider labelProvider = null;

	public ImportEntryDialog(Shell parentShell)
	{
		super(parentShell);
	}

	@Override
	public void create()
	{
		super.create();
		setTitle("Import Projects");
		setMessage("Import Projects From a File");
		getShell().setText("Import");
		getButton(OK).setText("Import");
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite comp = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(comp, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginBottom = 20;
		layout.verticalSpacing = 15;
		container.setLayout(layout);
		String workspaceLocationFolder = PropertyHandler.getVariable("workspace_location");
		File workspaceFolder = new File(workspaceLocationFolder);
		existingNames = new HashSet<String>();
		for(String name : workspaceFolder.list())
		{
			existingNames.add(name);
		}
		labelProvider  = new ImportEntryLabelProvider(existingNames);

		locationText =  new Text(container, SWT.BORDER);
		GridData textData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		textData.horizontalSpan = 2;
		locationText.setLayoutData(textData);
		locationText.setEnabled(false);

		Button browseButton = addAButton(container,"Browse File", GridData.HORIZONTAL_ALIGN_END);
		final FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
		fileDialog.setText("Select File");
		fileDialog.setFilterExtensions(new String[] {"*" + ExportEntryDialog.EXTENSION});
		browseButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				setErrorMessage(null);
				fileDialog.setFileName(null);
				String selected = fileDialog.open();
				if (selected != null)
				{
					try
					{
						exportedZipFile = new ZipFile(selected);
						ZipEntry exportEntry = exportedZipFile.getEntry("export_info.xml");
						if(exportEntry != null)
						{
							try
							{
								Element[] entryElements = exportEntryElements(exportEntry);
								projectTableViewer.setInput(entryElements);
								projectTableViewer.setCheckedElements(entryElements);
								labelProvider.resetAllNames();
								projectTableViewer.refresh();
								locationText.setText(selected);
								//								exportedZipFile.close();
							} catch (IOException ex)
							{
								setErrorMessage("Error loading Entry elements. Please upload another file.");
								exportedZipFile = null;
							}
						}
						else
						{
							setErrorMessage("Grits Export File is corrupt. Please upload another file.");
							exportedZipFile = null;
						}
					} catch (IOException e1)
					{
						logger.error(e1.getMessage(), e1);
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				// TODO Auto-generated method stub
			}
		});

		Table projectsTable = new Table(container, 
				SWT.FILL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | 
				SWT.FULL_SELECTION);
		GridData tableLayouData = new GridData(GridData.FILL_BOTH);
		tableLayouData.horizontalSpan = 3;
		tableLayouData.heightHint = 200;
		projectsTable.setLayoutData(tableLayouData);
		projectTableViewer = new CheckboxTableViewer(projectsTable, "Select");

		TableViewerColumn tableColumn2 = new TableViewerColumn(projectTableViewer, SWT.FILL, 1);
		tableColumn2.getColumn().setText("Projects");
		tableColumn2.getColumn().setWidth(550);
		tableColumn2.setEditingSupport(new ElementNameEditingSupport(projectTableViewer));

		projectTableViewer.getTable().setHeaderVisible(true);
		projectTableViewer.getTable().setLinesVisible(true);
		projectTableViewer.setContentProvider(new ArrayContentProvider());
		projectTableViewer.setLabelProvider(labelProvider);

		Button selectAllButton = addAButton(container, "Select All", GridData.BEGINNING);
		selectAllButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if(projectTableViewer.getInput() != null)
				{
					for(Element selectedEntry : (Element[]) projectTableViewer.getInput())
					{
						projectTableViewer.setChecked(selectedEntry, true);
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				// TODO Auto-generated method stub
			}
		});

		Button deselectAllButton = addAButton(container, "Deselect All", GridData.BEGINNING);
		deselectAllButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if(projectTableViewer.getInput() != null)
				{
					for(Element selectedEntry : (Element[]) projectTableViewer.getInput())
					{
						projectTableViewer.setChecked(selectedEntry, false);
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{

			}
		});

		autoRenameButton = new Button(container, SWT.CHECK);
		autoRenameButton.setText("Auto-Rename");
		GridData buttonGridData = new GridData(GridData.END);
		buttonGridData.horizontalSpan = 1;
		buttonGridData.widthHint = 150;
		autoRenameButton.setLayoutData(buttonGridData);
		autoRenameButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if(projectTableViewer.getInput() != null)
				{
					if(((Button) e.getSource()).getSelection())
					{
						Set<String> allNames = new HashSet<String>(existingNames);
						for(Element entryElement : (Element[]) projectTableViewer.getInput())
						{
							if(allNames.contains(entryElement.getAttributeValue("name")))
							{
								renameElement(allNames, entryElement);
							}
							allNames.add(entryElement.getAttributeValue("name"));
						}
						labelProvider.resetAllNames();
						projectTableViewer.refresh();
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				// TODO Auto-generated method stub
			}
		});

		Label legendLabel = new Label(container, SWT.NONE);
		legendLabel.setText(" * duplicate name");
		GridData labelGridData = new GridData(GridData.FILL);
		labelGridData.horizontalSpan = 3;
		labelGridData.verticalSpan = 1;
		legendLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		legendLabel.setLayoutData(labelGridData);

		autoRenameButton.setSelection(true);
		browseButton.setFocus();

		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		return comp;
	}

	private Element[] exportEntryElements(ZipEntry exportEntry) throws IOException
	{
		try
		{
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(exportedZipFile.getInputStream(exportEntry));
			Element rootNode = document.getRootElement();
			if(rootNode.getName().equals("export") 
					&& rootNode.getAttributeValue("version").equals("1.0"))
			{
				Element[] entryElements = new Element[rootNode.getChildren().size()];
				int i = 0;
				boolean autoRename = autoRenameButton.getSelection();
				Set<String> allNames = new HashSet<String>(existingNames);
				for(Object node : rootNode.getChildren("entry"))
				{
					Element entryElement = (Element) node;
					if(Objects.equals(entryElement.getAttributeValue("type"), ProjectProperty.TYPE))
					{
						entryElements[i++] = entryElement;
						if(autoRename && allNames.contains(entryElement.getAttributeValue("name")))
						{
							renameElement(allNames, entryElement);
						}
						allNames.add(entryElement.getAttributeValue("name"));
					}
				}
				return entryElements;
			}
			else throw new IOException("Root node is not \"export\" or its version is not\"1.0\"");
		} catch (JDOMException e1)
		{
			logger.error(e1.getMessage(), e1);
			throw new IOException(e1.getMessage(), e1);
		}
	}

	private void renameElement(Set<String> allNames, Element entryElement)
	{
		String currentName = entryElement.getAttributeValue("name");
		int suffixCounter = 2;
		while(allNames.contains(currentName))
		{
			currentName = entryElement.getAttributeValue("name") 
					+ "_GR_" + suffixCounter++;
		}

		entryElement.setAttribute("name", currentName);
	}

	private Button addAButton(Composite container, String label, int horizontalAlignment)
	{
		Button button = new Button(container, SWT.PUSH);
		button.setText(label);
		GridData buttonGridData = new GridData(horizontalAlignment);
		buttonGridData.horizontalSpan = 1;
		buttonGridData.widthHint = 150;
		button.setLayoutData(buttonGridData);
		return button;
	}

	@Override
	protected void okPressed()
	{
		setErrorMessage(null);
		if(projectTableViewer.getCheckedElements().length > 0)
		{
			boolean renameRequired = false;
			selectedEntryElements = new ArrayList<Element>();
			for(Object pj : projectTableViewer.getCheckedElements())
			{
				if(pj instanceof Element)
				{
					if(labelProvider.getDuplicateElements().contains(pj))
					{
						renameRequired = true;
						break;
					}
					selectedEntryElements.add((Element) pj);
				}
			}
			if(renameRequired) 
				setErrorMessage("Rename selected duplicate entries before exporting.");
			else
				super.okPressed();

		}
		else
		{
			setErrorMessage("No Entry selected for export.");
		}
	}

	public ZipFile getExportedZipFile()
	{
		return exportedZipFile;
	}

	public List<Element> getSelectedEntryElements()
	{
		return selectedEntryElements;
	}
}
