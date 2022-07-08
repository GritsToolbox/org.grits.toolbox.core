 
package org.grits.toolbox.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.grits.toolbox.core.dataShare.IGritsConstants;
import org.grits.toolbox.core.projectexplorer.handler.NewProjectHandler;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.core.utilShare.ErrorUtils;

@SuppressWarnings("restriction")
public class GritsIntroPart
{
	private static final Logger logger = Logger.getLogger(GritsIntroPart.class);

	private Image gritsWelcomeImage = Activator.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, "icons/GRITS-Welcome.png").createImage();

	@Inject ECommandService commandService;
	@Inject EHandlerService handlerService;
	
	@Inject
	public GritsIntroPart()
	{
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent, IGritsUIService gritsUIService)
	{
		ScrolledComposite scrolledComposite = new ScrolledComposite(parent, 
				SWT.H_SCROLL | SWT.V_SCROLL|SWT.BORDER);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrolledComposite.setBackground(parent.getDisplay().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND));
		scrolledComposite.setBackgroundMode(SWT.INHERIT_FORCE);

		Composite outerContainer = new Composite(scrolledComposite, SWT.FILL);
		GridLayout gridLayout = new GridLayout(1, true);
		gridLayout.marginTop = 80;
		gridLayout.marginBottom = 80;
		gridLayout.marginRight = 80;
		gridLayout.marginLeft = 80;
		gridLayout.verticalSpacing = 40;
		outerContainer.setLayout(gridLayout);

		scrolledComposite.setContent(outerContainer);
		scrolledComposite.setMinWidth(1000);
		scrolledComposite.setMinHeight(900);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);

		String[] welcomeText = getHeaderAndBody();

		StyledText styledText = new StyledText(outerContainer, SWT.CENTER|SWT.WRAP);
		styledText.setText(welcomeText[0]);
		StyleRange style1 = new StyleRange();
		style1.start = 0;
		style1.length = styledText.getCharCount();
		style1.underline = true;
		styledText.setStyleRange(style1);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.horizontalAlignment = SWT.CENTER;
		gd.verticalAlignment = GridData.FILL;
		gd.verticalSpan = 2;
		styledText.setLayoutData(gd);

		Link link = new Link(outerContainer, SWT.CENTER|SWT.WRAP);
		link.setText(welcomeText[1]);
		gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.horizontalAlignment = SWT.CENTER;
		gd.verticalAlignment = GridData.FILL;
		gd.verticalSpan = 2;
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
				} catch (IllegalArgumentException e)
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

		Composite projectContainer = new Composite(outerContainer, SWT.FILL);
		GridLayout projectContainerLayout = new GridLayout(1, true);
		projectContainerLayout.verticalSpacing = 10;
		projectContainer.setLayout(projectContainerLayout);
		GridData projectGridData = new GridData();
		projectGridData.horizontalAlignment = GridData.CENTER;
		projectGridData.verticalAlignment = GridData.FILL;
		projectGridData.verticalSpan = 1;
		projectContainer.setLayoutData(projectGridData);

		Label label = new Label(projectContainer, SWT.CENTER|SWT.WRAP);
		label.setText("Your first step in GRITS Toolbox is to create"
				+ " a new project in your workspace. Do you want"
				+ " to create a project now?\n\n");
		gd = new GridData(GridData.GRAB_HORIZONTAL|GridData.GRAB_VERTICAL);
		gd.horizontalAlignment = GridData.CENTER;
		gd.verticalAlignment = GridData.BEGINNING;
		label.setLayoutData(gd);

		Button button = new Button(projectContainer, SWT.PUSH);
		button.setText("Yes, create a new Project");
		gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.horizontalAlignment = GridData.CENTER;
		gd.verticalAlignment = GridData.BEGINNING;
		gd.widthHint = 250;
		button.setLayoutData(gd);
		button.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				createProject();
			}

			private void createProject()
			{
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put(NewProjectHandler.PARAMETER_CLASS_NAME, GritsIntroPart.class.getName());
				try
				{
					gritsUIService.selectPerspective(
							IGritsConstants.ID_DEFAULT_PERSPECTIVE);
					handlerService.executeHandler(commandService.createCommand(
							NewProjectHandler.COMMAND_ID, parameters));
				} catch (Exception e)
				{
					logger.fatal(e.getMessage(), e);
					ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(),
							"Something went wrong. Please check with the GRITS"
									+ " developers team.\n", e);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				createProject();
			}
		});

		button = new Button(projectContainer, SWT.PUSH);
		button.setText("No, take me to the workspace");
		gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.horizontalAlignment = GridData.CENTER;
		gd.verticalAlignment = GridData.BEGINNING;
		gd.widthHint = 250;
		button.setLayoutData(gd);
		button.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				closeThis();
			}

			private void closeThis()
			{
				gritsUIService.selectPerspective(
						IGritsConstants.ID_DEFAULT_PERSPECTIVE);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				closeThis();
			}
		});

		Label imgLabel = new Label(outerContainer, SWT.NONE);
		imgLabel.setImage(gritsWelcomeImage);
		imgLabel.setSize(imgLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.horizontalAlignment = GridData.CENTER;
		gd.verticalAlignment = GridData.END;
		imgLabel.setLayoutData(gd);
	}

	private String[] getHeaderAndBody()
	{
		String[] welcomeMessage = new String[]{"", ""};
		String header = "";
		String body = "";
		BufferedInputStream bufferedInputStream = null;
		try
		{
			URL resourceURL = FileLocator.toFileURL(
					Platform.getBundle(Activator.PLUGIN_ID).getResource("preference"));
			String fileLocation = resourceURL.getPath() 
					+ File.separator + "display_text"
					+ File.separator + "welcomeMessage.txt";
			bufferedInputStream = new BufferedInputStream(new FileInputStream(new File(fileLocation)));
			byte[] contents = new byte[10*1024];
			int bytesRead=0;
			String message = "";
			while((bytesRead = bufferedInputStream.read(contents)) != -1)
			{ 
				message = new String(contents, 0, bytesRead);
			}
			header = message.substring(message.indexOf("<TITLE>") + 7, message.indexOf("</TITLE>"));
			body = message.substring(message.indexOf("<BODY>") + 6, message.indexOf("</BODY>"));
		} catch (FileNotFoundException ex)
		{
			logger.error(ex.getMessage(), ex);
		} catch (IOException ex)
		{
			logger.error(ex.getMessage(), ex);
		} catch (Exception ex)
		{
			logger.fatal(ex.getMessage(), ex);
		}
		welcomeMessage[0] = header.isEmpty() ? "WELCOME TO GRITS Toolbox" : header;
		welcomeMessage[1] = body;
		return welcomeMessage;
	}

	@PreDestroy
	void predestroy()
	{
		
	}
}