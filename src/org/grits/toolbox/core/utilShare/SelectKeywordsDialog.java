/**
 * 
 */
package org.grits.toolbox.core.utilShare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.grits.toolbox.core.utilShare.provider.GenericListContentProvider;

/**
 * 
 *
 */
public class SelectKeywordsDialog extends Dialog
{
	private Set<String> inputKeywords = null;
	private Set<String> selectedKeywords = null;
	private CheckboxTableViewer checkboxTableViewer = null;

	public SelectKeywordsDialog(Shell parentShell, Set<String> inputKeywords)
	{
		super(parentShell);
		this.inputKeywords = inputKeywords;
	}

	public void setSelectedKeyWords(Set<String> keywords)
	{
		this.selectedKeywords = keywords;
	}

	public Set<String> getSelectedKeywords()
	{
		return selectedKeywords;
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		getShell().setText("Choose Keywords");

		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginRight = 2;
		layout.verticalSpacing = 10;
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		container.setLayout(layout);

		checkboxTableViewer = createKeywordsTableViewer(container);
		List<String> inputList = new ArrayList<>();
		inputList.addAll(inputKeywords);
		String[] selectedKeywords = new String[this.selectedKeywords.size()];
		int i = 0;
		for(String keyword : this.selectedKeywords)
		{
			if(!this.inputKeywords.contains(keyword))
			{
				inputList.add(keyword);
			}
		}
		for(String keyword : this.selectedKeywords)
		{
			selectedKeywords[i++] = keyword;
		}
		String[] availableSelection = new String[i];
		for(int j = 0 ; j < i ; j++)
		{
			availableSelection[j] = selectedKeywords[j];
		}

		Collections.sort(inputList, String.CASE_INSENSITIVE_ORDER);
		checkboxTableViewer.setInput(inputList);
		checkboxTableViewer.setCheckedElements(availableSelection);

		return container;
	}

	private CheckboxTableViewer createKeywordsTableViewer(Composite container)
	{
		Table keywordsTable = new Table(container, 
				SWT.FILL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | 
				SWT.FULL_SELECTION);
		GridData tableLayouData = new GridData(GridData.FILL_BOTH);
		tableLayouData.horizontalSpan = 2;
		tableLayouData.verticalSpan = 1;
		tableLayouData.minimumHeight = 260;
		tableLayouData.heightHint = 400;
		keywordsTable.setLayoutData(tableLayouData);
		CheckboxTableViewer tableViewer = new CheckboxTableViewer(keywordsTable);

		TableViewerColumn tableColumn = new TableViewerColumn(tableViewer, SWT.FILL, 1);
		tableColumn.getColumn().setText("Keywords");
		tableColumn.getColumn().setWidth(300);
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.setContentProvider(new GenericListContentProvider());
		tableViewer.setLabelProvider(new ITableLabelProvider()
		{
			@Override
			public void removeListener(ILabelProviderListener listener)
			{

			}

			@Override
			public boolean isLabelProperty(Object element, String property)
			{
				return false;
			}

			@Override
			public void dispose()
			{

			}

			@Override
			public void addListener(ILabelProviderListener listener)
			{

			}

			@Override
			public String getColumnText(Object element, int columnIndex)
			{
				if(columnIndex > 0 
						&& element instanceof String)
				{
					return (String) element;
				}
				return null;
			}

			@Override
			public Image getColumnImage(Object element, int columnIndex)
			{
				return null;
			}
		});
		return tableViewer;
	}

	@Override
	protected void okPressed()
	{
		List<?> list = (List<?>) checkboxTableViewer.getInput();
		for(Object element : list)
		{
			if(checkboxTableViewer.getChecked(element))
			{
				if(element instanceof String)
				{
					this.selectedKeywords.add((String) element);
				}
			}
			else
			{
				this.selectedKeywords.remove(element);
			}
		}
		super.okPressed();
	}

}
