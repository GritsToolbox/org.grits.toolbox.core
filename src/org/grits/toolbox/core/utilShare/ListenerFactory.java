package org.grits.toolbox.core.utilShare;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Text;

public class ListenerFactory {
	public static TraverseListener getTabTraverseListener()
	{
		TraverseListener listener = new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
					e.doit = true;
				}
			}
		};
		return listener;
	}
	
	public static KeyListener getCTRLAListener()
	{
		KeyListener keyListener = new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.stateMask == SWT.CTRL && e.keyCode == 97){     
					((Text)e.getSource()).selectAll();
			    }
			}
		};
		return keyListener;
	}
}
