package org.grits.toolbox.core.editor;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.widgets.processDialog.GRITSProgressDialog;
import org.grits.toolbox.widgets.tools.GRITSProcessStatus;

public abstract class CancelableMultiPageEditor implements ICancelableEditor {
	
	private static final Logger logger = Logger.getLogger(CancelableMultiPageEditor.class);
	protected int iStatus = GRITSProcessStatus.OK;
	protected GRITSProgressDialog dtpdThreadedDialog = null;
	
	/**
	 * The container widget.
	 */
	private CTabFolder container;
	protected MPart part;
	
	public MPart getPart() {
		return part;
	}

	public void setPart(MPart part) {
		this.part = part;
	}

	private Map<CTabItem, IEntryEditorPart> cTabItemToPartTabMap = new HashMap<>();
	
	
	
	@PostConstruct
	public void createPartControl(Composite parent,  final MPart part) {
		this.part = part;
		this.container = createContainer(parent, part);
		createPages();
		// set the active page (page 0 by default), unless it has already been
		// done
		if (getActivePage() == -1) {
			setActivePage(0);
		}
	}
	
	/**
	 * Returns the index of the currently active page, or -1 if there is no
	 * active page.
	 * <p>
	 * Subclasses should not override this method
	 * </p>
	 *
	 * @nooverride
	 *
	 * @return the index of the active page, or -1 if there is no active page
	 */
	public int getActivePage() {
		CTabFolder tabFolder = container;
		if (tabFolder != null && !tabFolder.isDisposed()) {
			return tabFolder.getSelectionIndex();
		}
		return -1;
	}
	
	/**
	 * Sets the currently active page.
	 *
	 * @param pageIndex
	 *            the index of the page to be activated; the index must be valid
	 */
	public void setActivePage(int pageIndex) {
		Assert.isTrue(pageIndex >= 0 && pageIndex < getPageCount());
		container.setSelection(pageIndex);
	}
	
	
	
	/**
	 * Creates an empty container. Creates a CTabFolder with no style bits set,
	 * and hooks a selection listener which sets the CTabItem to part's context>
	 * whenever the selected tab changes.
	 *
	 * @param parent
	 *            The composite in which the container tab folder should be
	 *            created; must not be <code>null</code>.
	 * @return a new container
	 */
	private CTabFolder createContainer(Composite parent,  final MPart part) {
		// use SWT.FLAT style so that an extra 1 pixel border is not reserved
		// inside the folder
		parent.setLayout(new FillLayout());
		final CTabFolder newContainer = new CTabFolder(parent, SWT.BOTTOM
				| SWT.FLAT);
		newContainer.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				changeTab(e);
			}

			private void changeTab(SelectionEvent e)
			{
				int selectionIndex = container.getSelectionIndex();

				logger.info("Tab changed to " + selectionIndex);
				selectionIndex = selectionIndex < 0 ? 0 : selectionIndex;
				IEntryEditorPart currentTab = getcTabItemToPartTabMap().get(container.getSelection());
				part.getContext().set(IEntryEditorPart.class, currentTab);

				// an added notification for the selected tab for specialized action
				container.getSelection().notifyListeners(SWT.Selection, new Event());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				changeTab(e);
			}
			
		});
		newContainer.addTraverseListener(new TraverseListener() {
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=199499 : Switching tabs by Ctrl+PageUp/PageDown must not be caught on the inner tab set
			@Override
			public void keyTraversed(TraverseEvent e) {
				switch (e.detail) {
					case SWT.TRAVERSE_PAGE_NEXT:
					case SWT.TRAVERSE_PAGE_PREVIOUS:
						int detail = e.detail;
						e.doit = true;
						e.detail = SWT.TRAVERSE_NONE;
						Control control = newContainer.getParent();
						do {
							if (control.traverse(detail))
								return;
							if (control.getListeners(SWT.Traverse).length != 0)
								return;
							if (control instanceof Shell)
								return;
							control = control.getParent();
						} while (control != null);
				}
			}
		});
		return newContainer;
	}
		
	public Map<CTabItem, IEntryEditorPart> getcTabItemToPartTabMap() {
		return cTabItemToPartTabMap;
	}

	public int getStatus() {
		return iStatus;
	}	
	
	public void setStatus( int _iStatus ) {
		this.iStatus = _iStatus;
	}
	
	public GRITSProgressDialog getThreadedDialog() {
		return dtpdThreadedDialog;
	}
	
	public void setThreadedDialog(GRITSProgressDialog dtpdThreadedDialog) {
		this.dtpdThreadedDialog = dtpdThreadedDialog;
	}
	
	/**
	 * Returns the control for the given page index, or <code>null</code> if
	 * no control has been set for the page. The page index must be valid.
	 * <p>
	 * Subclasses should not override this method
	 * </p>
	 *
	 * @param pageIndex
	 *            the index of the page
	 * @return the control for the specified page, or <code>null</code> if
	 *         none has been set
	 */
	protected Control getControl(int pageIndex) {
		return getItem(pageIndex).getControl();
	}
	
	/**
	 * Returns the tab item for the given page index (page index is 0-based).
	 * The page index must be valid.
	 *
	 * @param pageIndex
	 *            the index of the page
	 * @return the tab item for the given page index
	 */
	private CTabItem getItem(int pageIndex) {
		return container.getItem(pageIndex);
	}
	
	/**
	 * returns the entryeditorpart (page) at the given index
	 * @param pageIndex
	 * @return
	 */
	public IEntryEditorPart getPageItem(int pageIndex) {
		CTabItem item = container.getItem(pageIndex);
		if (item != null)
			return cTabItemToPartTabMap.get(item);
		return null;
	}

	/**
	 * Returns the number of pages in this multi-page editor.
	 *
	 * @return the number of pages
	 */
	public int getPageCount() {
		CTabFolder folder = container;
		// May not have been created yet, or may have been disposed.
		if (folder != null && !folder.isDisposed()) {
			return folder.getItemCount();
		}
		return 0;
	}

	public Composite getContainer() {
		return container;
	}
	
	/**
	 * Creates a tab item at the given index and places the given control in the
	 * new item. The item is a CTabItem with no style bits set.
	 *
	 * @param index
	 *            the index at which to add the control
	 * @param control
	 *            is the control to be placed in an item
	 * @return a new item
	 */
	private CTabItem createItem(int index, Control control) {
		CTabItem item = new CTabItem(container, SWT.NONE, index);
		item.setControl(control);
		return item;
	}

	/**
	 * Creates and adds a new page containing the given control to this
	 * multi-page editor. The control may be <code>null</code>, allowing it
	 * to be created and set later using <code>setControl</code>.
	 *
	 * @param control
	 *            the control, or <code>null</code>
	 * @return the index of the new page
	 *
	 * @see MultiPageEditorPart#setControl(int, Control)
	 */
	public int addPage(Control control) {
		int index = getPageCount();
		addPage(index, control);
		return index;
	}
	
	/**
	 * Creates and adds a new page containing the given control to this
	 * multi-page editor. The page is added at the given index. The control may
	 * be <code>null</code>, allowing it to be created and set later using
	 * <code>setControl</code>.
	 *
	 * @param index
	 *            the index at which to add the page (0-based)
	 * @param control
	 *            the control, or <code>null</code>
	 *
	 * @see MultiPageEditorPart#setControl(int, Control)
	 */
	public void addPage(int index, Control control) {
		createItem(index, control);
	}
	
	/**
	 * 
	 * @param editor
	 * @param input
	 * @return the page index of the newly created tab
	 * @throws Exception
	 */
	public int addPage(IEntryEditorPart editor, Entry input) throws Exception {
		int index = getPageCount();
		addPage(index, editor, input);
		return index;
	}
	
	/**
	 * Creates a new tab and inserts into the given index for this multi-page editor
	 * it also adds the newly created tab to the map of tab->editor (cTabItemToPartTabMap)
	 * 
	 * @param index at which the new tab should be inserted
	 * @param editor the part that will be added as a new tab
	 * @param input is the Entry to be set on the editor part
	 * @throws Exception
	 */
	public void addPage(int index, IEntryEditorPart editor, Entry input) throws Exception {
		editor.setEntry (input);
		Composite parent2 = new Composite(getContainer(),SWT.NONE);
		parent2.setLayout(new FillLayout());
		editor.createPartControl(parent2);
		// create item for page only after createPartControl has succeeded
		Item item = createItem(index, parent2);
		// remember the editor as data on the item
		item.setData(editor);
		
		cTabItemToPartTabMap.put((CTabItem)item, editor);
	}
	
	public boolean isCanceled() {
		return this.iStatus == GRITSProcessStatus.CANCEL;
	}
	
	protected abstract void createPages();
	
	/**
	 * Sets the image for the page with the given index, or <code>null</code>
	 * to clear the image for the page. The page index must be valid.
	 *
	 * @param pageIndex
	 *            the index of the page
	 * @param image
	 *            the image, or <code>null</code>
	 */
	protected void setPageImage(int pageIndex, Image image) {
		getItem(pageIndex).setImage(image);
	}

	/**
	 * Sets the text label for the page with the given index. The page index
	 * must be valid. The text label must not be null.
	 *
	 * @param pageIndex
	 *            the index of the page
	 * @param text
	 *            the text label
	 */
	protected void setPageText(int pageIndex, String text) {
		getItem(pageIndex).setText(text);
	}
	
	protected void setPartName (String partName) {
		part.setLabel(partName);
	}
	
	protected String getTitle() {
		return part.getLabel();
	}
	
	/**
	 * Removes the page with the given index from this multi-page editor. The
	 * controls for the page are disposed of; if the page has an editor, it is
	 * disposed of too. The page index must be valid.
	 *
	 * @param pageIndex
	 *            the index of the page
	 * @see MultiPageEditorPart#addPage(Control)
	 * @see MultiPageEditorPart#addPage(IEditorPart, IEditorInput)
	 */
	public void removePage(int pageIndex) {
		if (pageIndex >= 0 && pageIndex < getPageCount()) {
			// get control for the item if it's not an editor
			CTabItem item = getItem(pageIndex);
			Control pageControl = item.getControl();
	
			// dispose item before disposing editor, in case there's an exception
			// in editor's dispose
			item.dispose();
	
			if (pageControl != null) {
				pageControl.dispose();
			}
			
			cTabItemToPartTabMap.remove(item);
		}
	}

}
