/**
 *
 */
package org.grits.toolbox.core.projectexplorer.dialog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.Activator;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.GeneralSettings;
import org.grits.toolbox.core.datamodel.SettingEntry;
import org.grits.toolbox.core.utils.SettingsHandler;

/**
 *
 *
 */
public class NewProjectInfoDialog extends Dialog
{
    private static final Logger logger = Logger.getLogger(NewProjectInfoDialog.class);
    private static final int WIDTH_HINT = 580;
    private String infoMessage = null;
    
	private Button doNotShow;
	private Boolean doNotShowValue = false;

    public NewProjectInfoDialog(Shell parentShell)
    {
        super(parentShell);
        infoMessage = getInfoText();
    }

    private String getInfoText()
    {
        String infoMessage = "No message to display. Something went wrong";
        BufferedInputStream bufferedInputStream = null;
        try
        {
            URL resourceURL = FileLocator.toFileURL(Platform.getBundle(Activator.PLUGIN_ID).getResource("preference"));
            String fileLocation = resourceURL.getPath() + File.separator + "display_text" + File.separator
                    + "newProjectMessage.txt";
            bufferedInputStream = new BufferedInputStream(new FileInputStream(new File(fileLocation)));
            byte[] contents = new byte[256 * 1024];
            int bytesRead = 0;
            while ((bytesRead = bufferedInputStream.read(contents)) != -1)
            {
                infoMessage = new String(contents, 0, bytesRead, PropertyHandler.GRITS_CHARACTER_ENCODING);
            }
        }
        catch (FileNotFoundException ex)
        {
            logger.error(ex.getMessage(), ex);
        }
        catch (IOException ex)
        {
            logger.error(ex.getMessage(), ex);
        }
        finally
        {
            if (bufferedInputStream != null)
            {
                try
                {
                    bufferedInputStream.close();
                }
                catch (IOException ex)
                {
                    logger.fatal(ex.getMessage(), ex);
                }
            }
        }
        return infoMessage;
    }

    @Override
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("GRITS Toolbox");
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        Composite outerContainer = new Composite(parent, SWT.FILL);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginTop = 10;
        gridLayout.marginBottom = 10;
        gridLayout.marginRight = 10;
        gridLayout.marginLeft = 10;
        gridLayout.verticalSpacing = 10;
        outerContainer.setLayout(gridLayout);

        List<String> parsedText = getParsedInfoText();
        Iterator<String> parsedTextIterator = parsedText.iterator();

        Link link = new Link(outerContainer, SWT.BEGINNING | SWT.WRAP);
        link.setText(parsedTextIterator.next());
        GridData gd = new GridData();
        gd.widthHint = WIDTH_HINT;
        gd.horizontalAlignment = GridData.BEGINNING;
        link.setLayoutData(gd);
        link.addSelectionListener(new SelectionListener()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                openWebPage(e.text);
            }

            private void openWebPage(String webURL)
            {
                try
                {
                    Program.launch(webURL);
                }
                catch (IllegalArgumentException e)
                {
                    logger.fatal("Error opening browser");
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e)
            {
                openWebPage(e.text);
            }
        });

        ScrolledComposite scrolledComposite = new ScrolledComposite(outerContainer,
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        GridData layoutData = new GridData();
        layoutData.heightHint = 400;
        layoutData.widthHint = WIDTH_HINT + 20;
        scrolledComposite.setLayoutData(layoutData);
        scrolledComposite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        scrolledComposite.setBackgroundMode(SWT.INHERIT_FORCE);
        scrolledComposite.setLayout(new FillLayout());

        Composite infoContainer = new Composite(scrolledComposite, SWT.NONE);
        GridLayout infoContainerLayout = new GridLayout(1, false);
        infoContainerLayout.verticalSpacing = 20;
        infoContainer.setLayout(infoContainerLayout);

        generateInfoPart(infoContainer, parsedTextIterator);

        scrolledComposite.setContent(infoContainer);
        scrolledComposite.setMinSize(infoContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setExpandHorizontal(true);
        
        addDoNotShowCheckbox(outerContainer);

        return outerContainer;
    }

    private void generateInfoPart(Composite infoContainer, Iterator<String> parsedTextIterator)
    {
        while (parsedTextIterator.hasNext())
        {
            Composite paraContainer = new Composite(infoContainer, SWT.NONE);
            GridLayout paraContainerLayout = new GridLayout(1, false);
            paraContainerLayout.verticalSpacing = 5;
            paraContainer.setLayout(paraContainerLayout);
            GridData paraGridData = new GridData();
            paraContainer.setLayoutData(paraGridData);

            StyledText styledText = new StyledText(paraContainer, SWT.NONE);
            styledText.setText(parsedTextIterator.next());
            StyleRange style1 = getUnderLinedStyleRange(styledText.getCharCount());
            styledText.setStyleRange(style1);
            GridData gd = new GridData(GridData.FILL_BOTH);
            gd.verticalSpan = 1;
            styledText.setLayoutData(gd);

            if (parsedTextIterator.hasNext())
            {
                Label label = new Label(paraContainer, SWT.WRAP);
                label.setText(parsedTextIterator.next());
                gd = new GridData(GridData.FILL_BOTH);
                gd.verticalSpan = 1;
                gd.widthHint = WIDTH_HINT;
                label.setLayoutData(gd);
            }
        }
    }

    private void addDoNotShowCheckbox(Composite infoContainer) {
		doNotShow = new Button (infoContainer, SWT.CHECK);
		doNotShow.setText("Do not show again");
		doNotShow.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				doNotShowValue = doNotShow.getSelection();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private List<String> getParsedInfoText()
    {
        List<String> parsedTexts = new ArrayList<String>();
        parsedTexts.add(0,
                infoMessage.substring(infoMessage.indexOf("<TITLE>") + 7, infoMessage.indexOf("</TITLE>")).trim());
        String body = infoMessage.substring(infoMessage.indexOf("<BODY>") + 6, infoMessage.indexOf("</BODY>"));

        String para = "";
        String nextParsedText = null;
        while (body.contains("<p>"))
        {
            para = body.substring(body.indexOf("<p>") + 3, body.indexOf("</p>"));
            nextParsedText = "";
            if (para.contains("<h1>"))
            {
                nextParsedText = para.substring(para.indexOf("<h1>") + 4, para.indexOf("</h1>"));
                parsedTexts.add(nextParsedText.trim());
                parsedTexts.add(para.substring(para.indexOf("</h1>") + 5).trim());
            }
            else
                parsedTexts.add(para.trim());
            body = body.substring(body.indexOf("</p>") + 4);
        }
        return parsedTexts;
    }

    private StyleRange getUnderLinedStyleRange(int characterCount)
    {
        StyleRange style = new StyleRange();
        style.start = 0;
        style.length = characterCount;
        style.underline = true;
        return style;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent)
    {
        Button okButton = super.createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        okButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				//save "do not show" settings in the configuration directory
				GeneralSettings settings = null;
				try {
					settings = SettingsHandler.readSettings();
				} catch (Exception ex) {
					logger.warn("Settings file does not exist yet");
				}
				if (settings == null)
					settings = new GeneralSettings();
				SettingEntry se = new SettingEntry();
				se.setId (GeneralSettings.SHOWINFO_SETTING);
				se.setName(GeneralSettings.SHOWINFO_SETTING);
				se.setDescription("The dialog which gives information about the next steps after creating a project in GRITS");
				if (doNotShowValue)
					settings.addHiddenDialog(se);
				else
					settings.removeHiddenDialog (se);
				try {
					SettingsHandler.writeSettings(settings);
				} catch (Exception ex) {
					logger.error("Cannot write settings file", ex);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
    }
}
