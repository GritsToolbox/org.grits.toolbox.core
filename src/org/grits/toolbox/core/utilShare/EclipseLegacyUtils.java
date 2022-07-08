package org.grits.toolbox.core.utilShare;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;

@Deprecated
public class EclipseLegacyUtils {
	//log4J Logger
	private static final Logger logger = Logger.getLogger(EclipseLegacyUtils.class);

	public static StructuredSelection getCurrentSelection( ExecutionEvent event ) {
		logger.debug("E3 to E4 legacy method.");
		if( event != null ) {
			// first see if we have a current selection (called from parent page)
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			if(selection instanceof StructuredSelection) {
				return (StructuredSelection) selection;
			}
		} else {
			return PropertyHandler.getDataModel().getLastSelection();
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public static Entry getCurrentEntry( StructuredSelection selection ) {
		logger.debug("E3 to E4 legacy method.");
		Entry entry = null;
		if( selection != null ) {
			if(selection.getFirstElement() instanceof Entry) {
				entry = (Entry) selection.getFirstElement();
			}
		} else {
			if( PropertyHandler.getDataModel().getLastSelection() != null
					&& PropertyHandler.getDataModel().getLastSelection().getFirstElement() instanceof Entry) {
				entry = (Entry) PropertyHandler.getDataModel().getLastSelection().getFirstElement();
			}
		}
		return entry;
	}
	
	public static Entry getClickedEntry( ExecutionEvent event ) {
		logger.debug("E3 to E4 legacy method.");
		
		StructuredSelection selection = EclipseLegacyUtils.getCurrentSelection(event);
		Entry entry = EclipseLegacyUtils.getCurrentEntry(selection);
		return entry;
	}
}
