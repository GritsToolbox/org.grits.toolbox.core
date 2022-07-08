package org.grits.toolbox.core.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * This PropertyView is a base view for properties that creates a scrolled composite and resizes appropriately
 * @author dbrentw
 *
 */
public abstract class ScrollableEntryEditorPart extends EntryEditorPart 
{

    private Composite comp = null;
    private ScrolledComposite sc1 = null;
    private Point pComputedSize = null;

    protected ModifyListener modListener = new ModifyListener()
    {
        public void modifyText(ModifyEvent event) 
        {
            setDirty(true);
        }
    };

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout()); 
        sc1 = new ScrolledComposite(parent,SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER); 
        sc1.addControlListener(new ControlListener() {

            @Override
            public void controlResized(ControlEvent e) {
                // TODO Auto-generated method stub

                Point p1 = sc1.getSize();
                Point p2 = comp.getSize();
                boolean bScrollVisible = p2.y > p1.y;
                int iAdder = bScrollVisible ? 25 : 10;

                // horizontal first	
                p1.x -= iAdder;
                if ( p2.x < p1.x ) { // expand the window
                    p2.x = p1.x;
                } else if ( p2.x > p1.x && pComputedSize.x < p1.x ) { // shrink window
                    p2.x = p1.x;
                } else if ( pComputedSize.x > p1.x ) { // set minimum size
                    p2.x = pComputedSize.x;
                }
                comp.setSize(p2);

            }

            @Override
            public void controlMoved(ControlEvent e) {
                // TODO Auto-generated method stub

            }
        });

        this.comp = new Composite(sc1, SWT.NONE); 

        initializeComponents();

        sc1.setContent(this.comp); 

        pComputedSize = this.comp.computeSize(SWT.DEFAULT, SWT.DEFAULT); 
        this.comp.setSize(pComputedSize);
    }	

    protected abstract void initializeComponents(); // must define and set layout, populate the page

    @Override
    protected Composite getParent() {
        return comp;
    }

    public ModifyListener getModListener() {
        return modListener;
    }
}
