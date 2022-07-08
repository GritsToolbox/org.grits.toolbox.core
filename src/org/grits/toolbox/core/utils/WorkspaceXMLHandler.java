package org.grits.toolbox.core.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.DataModelHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.WorkspaceProperty;
import org.grits.toolbox.core.workspace.WorkspaceHistoryFileHandler;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

/**
 * Manage workspace.xml file which contains which projects belong to a user selected workspace
 * @author kitaeUser
 *
 */
public class WorkspaceXMLHandler {

	//log4J Logger
	private static final Logger logger = Logger.getLogger(WorkspaceHistoryFileHandler.class);
		
	/**
	 * returns project folders of File type.
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
	public static ProjectEntry[] getProjectFolders() throws Exception 
	{
		ProjectEntry[] resultFiles = new ProjectEntry[0];
		String lastActiveHistory = null;
		try {
			lastActiveHistory = WorkspaceHistoryFileHandler.getLastActiveHistory();
		} catch (Exception e) {
			logger.fatal(e.getMessage(), e);
			throw e;
		}
		
		boolean exist;
		try {
			exist = CheckWorkspaceXMLExist();
		} catch (Exception e1) {
			logger.fatal(e1.getMessage(), e1);
			throw e1;
		}
		
		if(!exist)
		{
			logger.fatal("workspace.xml does not exist: " + lastActiveHistory + "/.workspace.xml");
			throw new Exception("workspace.xml does not exist: " + lastActiveHistory + "/.workspace.xml");
		}
		else
		{
			try {
				//initiates SAXBuilder
				SAXBuilder builder = new SAXBuilder();
				Document document = builder.build(new File(lastActiveHistory + "/.workspace.xml"));
				XPath xpath = null;
				xpath = XPath.newInstance("//project");
				List resultList = xpath.selectNodes( document );
				resultFiles = new ProjectEntry[resultList.size()];
				Iterator b = resultList.iterator();
				int i = 0;
				while (b.hasNext())
				{
					Element oNode = (Element) b.next();
					Attribute openAttr = oNode.getAttribute("open");
					Boolean open = true;
					if (openAttr != null)
						open = oNode.getAttribute("open").getValue().equals("yes") ? true: false;
					resultFiles[i] = new ProjectEntry(oNode.getAttribute("folder").getValue(), open);
					i++;
				}
			} catch (JDOMException e) {
				logger.fatal(e);
				throw new JDOMException("Error occured while reading workspace xml file: " + lastActiveHistory + "/.workspace.xml",e);
			} catch (IOException e) {
				logger.fatal(e);
				throw new IOException("Error occured while reading workspace xml file: " + lastActiveHistory + "/.workspace.xml",e);
			}
		}
		
		return resultFiles;
	}

	/**
	 * Checked
	 * Create a new workspace xml file
	 * @throws Exception 
	 */
	public static void createNewWorkspaceXMLFile() throws Exception {
		//set the root node
		Element workspaceList = new Element("workspace");
		Document doc = new Document(workspaceList);
		doc.setRootElement(workspaceList);

		try
		{
			String lastActiveHistory = WorkspaceHistoryFileHandler.getLastActiveHistory();
			try
			{
				writeXmlFile(doc, new File(lastActiveHistory + File.separator + ".workspace.xml"));
			} catch (IOException e) {
				logger.fatal(e.getMessage(), e);
				throw new IOException("Error occurred while writing to workspace xml file: " + lastActiveHistory + "/.workspace.xml", e);
			}
		} catch (Exception e) {
			logger.fatal(e.getMessage(), e);
			throw e;
		} 
	}

	/**
	 * Check if .workspace.xml file exist or not
	 * @return
	 * @throws Exception 
	 */
	public static boolean CheckWorkspaceXMLExist() throws Exception
	{
		File workspaceXML;
		String lastActiveHistory = null;
		try {
			lastActiveHistory = WorkspaceHistoryFileHandler.getLastActiveHistory();
		} catch (Exception e) {
			logger.fatal(e.getMessage(), e);
			throw e;
		}
		
		try {
			workspaceXML = new File(lastActiveHistory + "/.workspace.xml");
		} catch (Exception e) {
			logger.fatal(e.getMessage(), e);
			throw e;
		}
		return workspaceXML.exists();
	}

	/**
	 * getOpen value in a workspace xml file
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
	public static boolean getOpen(Entry projectEntry) throws Exception 
	{
		String open = null;
		//create a child node
		//initiates SAXBuilder
		SAXBuilder builder = new SAXBuilder();

		String lastActiveHistory = null;
		try {
			lastActiveHistory = WorkspaceHistoryFileHandler.getLastActiveHistory();
		} catch (Exception e) {
			logger.fatal(e.getMessage(), e);
			throw e;
		}
		
		try {
			//read .workspace.xml file  
			File xmlFile = new File(lastActiveHistory + "/.workspace.xml");
			if(!xmlFile.exists())
			{
				logger.fatal("workspace xml file does not exist: " + lastActiveHistory + "/.workspace.xml");
				throw new Exception("workspace xml file does not exist: " + lastActiveHistory + "/.workspace.xml");
			}
			// Get the root element
			Document doc = builder.build(xmlFile);

			//need to look for projectName in the list
			//if it is, then do not need to do anything.
			XPath xpath = null;
			xpath = XPath.newInstance("//project[@folder='"+ projectEntry.getDisplayName() +"']");
			List resultList = xpath.selectNodes( doc );
			Iterator b = resultList.iterator();
			while (b.hasNext())
			{
				Element oNode = (Element) b.next();
				open = oNode.getAttribute("open").getValue();
			}
		} catch (JDOMException e) {
			logger.fatal("Error occurred while reading workspace xml file: " + lastActiveHistory + "/.workspace.xml", e);
			throw e;
		} catch (IOException e) {
			logger.fatal("Error occurred while reading workspace xml file: " + lastActiveHistory + "/.workspace.xml", e);
			throw e;
		} catch (Exception e) {
			throw e;
		}
		
		if(open.equals("yes"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static void removeEntry(String entry) throws Exception 
	{
		String lastActiveHistory = null;
		try {
			lastActiveHistory = WorkspaceHistoryFileHandler.getLastActiveHistory();
		} catch (Exception e) {
			logger.fatal(e.getMessage(), e);
			throw e;
		}
		
		//create a child node
		//initiates SAXBuilder
		SAXBuilder builder = new SAXBuilder();
		
		//read .workspace.xml file  
		File xmlFile = new File(lastActiveHistory + "/.workspace.xml");
		try {
			if(!xmlFile.exists())
			{
				logger.fatal("workspace xml file does not exist: " + lastActiveHistory+ "/.workspace.xml");
				throw new Exception("workspace xml file does not exist: " + lastActiveHistory + "/.workspace.xml");
			}
		}catch(Exception e)
		{
			logger.fatal("workspace xml file does not exist: " + lastActiveHistory+ "/.workspace.xml");
			throw e;
		}
		
		// Get the root element
		Document doc;
		try {
			doc = builder.build(xmlFile);
		} catch (JDOMException e) {
			logger.fatal("Error occurred while reading workspace xml file: " + lastActiveHistory + "/.workspace.xml", e);
			throw e;
		} catch (IOException e) {
			logger.fatal("Error occurred while reading workspace xml file: " + lastActiveHistory + "/.workspace.xml", e);
			throw e;
		}

		//need to look for projectName in the list
		List resultList;
		try {
			XPath xpath = null;
			xpath = XPath.newInstance("//project[@folder='"+ entry +"']");
			resultList = xpath.selectNodes( doc );
		} catch (JDOMException e) {
			logger.fatal("Error occurred while reading workspace xml file: " + lastActiveHistory + "/.workspace.xml", e);
			throw e;
		}
		Iterator b = resultList.iterator();
		
		while (b.hasNext())
		{
			Element oNode = (Element) b.next();
			oNode.detach();
		}

		try
		{
			writeXmlFile(doc, new File(lastActiveHistory + File.separator + ".workspace.xml"));
		} catch (IOException e)
		{
			logger.fatal("Error occurred while writing to workspace xml file: " 
					+ lastActiveHistory + File.separator + ".workspace.xml", e);
			throw e;
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	public static void setOpenClosed(Entry projectEntry) throws Exception 
	{
		String lastActiveHistory = null;
		try {
			lastActiveHistory = WorkspaceHistoryFileHandler.getLastActiveHistory();
		} catch (Exception e) {
			logger.fatal(e.getMessage(), e);
			throw e;
		}

		//create a child node
		//initiates SAXBuilder
		SAXBuilder builder = new SAXBuilder();

		//read .workspace.xml file  
		File xmlFile = new File(lastActiveHistory + "/.workspace.xml");
		if(!xmlFile.exists())
		{
			logger.fatal("workspace xml file does not exist: " + lastActiveHistory + "/.workspace.xml");
			throw new Exception("workspace xml file does not exist: " + lastActiveHistory + "/.workspace.xml");
		}
		// Get the root element
		Document doc;
		try {
			doc = builder.build(xmlFile);
		} catch (JDOMException e) {
			logger.fatal("Error occurred while reading workspace xml file: " + lastActiveHistory + "/.workspace.xml", e);
			throw e;
		} catch (IOException e) {
			logger.fatal("Error occurred while reading workspace xml file: " + lastActiveHistory + "/.workspace.xml", e);
			throw e;
		}

		//need to look for projectName in the list
		//if it is, then do not need to do anything.
		Iterator b;
		try {
			XPath xpath = null;
			xpath = XPath.newInstance("//project[@folder='"+ projectEntry.getDisplayName() +"']");
			List resultList = xpath.selectNodes( doc );
			b = resultList.iterator();
		} catch (JDOMException e) {
			logger.fatal("Error occurred while reading workspace xml file: " + lastActiveHistory + "/.workspace.xml", e);
			throw e;
		}
		
		ProjectProperty pp = (ProjectProperty)projectEntry.getProperty();
		while (b.hasNext())
		{
			Element oNode = (Element) b.next();
			if(pp.getOpen())
			{
				oNode.getAttribute("open").setValue("yes");
			}
			else
			{
				oNode.getAttribute("open").setValue("no");
			}
		}

		try
		{
			writeXmlFile(doc, new File(lastActiveHistory + File.separator + ".workspace.xml"));
		} catch (IOException e) {
			logger.fatal("Error occurred while writing to workspace xml file: " 
					+ lastActiveHistory + File.separator + ".workspace.xml", e);
			throw e;
		}
	}
	
	@SuppressWarnings("rawtypes")
	/**
	 * update the workspace xml file
	 * @param projectName the name of a new project
	 */
	public static void updateWorkspaceXMLFile(Entry projectEntry) throws FileNotFoundException, IOException, Exception 
	{
		//create a child node
		//initiates SAXBuilder
		SAXBuilder builder = new SAXBuilder();

		String lastActiveHistory = null;
		try {
			lastActiveHistory = WorkspaceHistoryFileHandler.getLastActiveHistory();
		} catch (Exception e) {
			logger.fatal(e.getMessage(), e);
			throw e;
		}
		
		//read .workspace.xml file  
		File xmlFile = new File(lastActiveHistory + "/.workspace.xml");
		if(!xmlFile.exists())
		{
			logger.fatal("workspace xml file does not exist: " + lastActiveHistory + "/.workspace.xml");
			throw new FileNotFoundException("workspace xml file does not exist: " + lastActiveHistory + "/.workspace.xml");
		}
		// Get the root element
		Document doc;
		try {
			doc = builder.build(xmlFile);
		} catch (JDOMException e) {
			logger.fatal("Error occurred while reading workspace xml file: " + lastActiveHistory + "/.workspace.xml", e);
			throw new IOException(e.getMessage(), e);
		} catch (IOException e) {
			logger.fatal("Error occurred while reading workspace xml file: " + lastActiveHistory + "/.workspace.xml", e);
			throw e;
		}

		//need to look for projectName in the list
		//if it is, then do not need to do anything.
		List resultList = null;
		XPath xpath = null;
		try {
			xpath = XPath.newInstance("//project[@folder='"+ projectEntry.getDisplayName() +"']");
			resultList = xpath.selectNodes( doc );
		} catch (JDOMException e) {
			logger.fatal("Error occurred while reading workspace xml file: " + lastActiveHistory + "/.workspace.xml", e);
			throw new IOException(e.getMessage(), e);
		}
		
		if (resultList.size() == 0)
		{
			// append a new node to staff
			// add new age element
			Element workspace = new Element("project");
			// need to add new attributes path, last_active
			List<Attribute> atts = new ArrayList<Attribute>();
			atts.add(new Attribute("folder", projectEntry.getDisplayName()));
			//when updating a project, it should be open
			atts.add(new Attribute("open", "yes"));
			workspace.setAttributes(atts);

			// add workspace to the root
			doc.getRootElement().addContent(workspace);
		}

		try
		{
			writeXmlFile(doc, new File(lastActiveHistory + File.separator + ".workspace.xml"));
		} catch (IOException e) {
			logger.fatal("Error occurred while writing to workspace xml file: " 
					+ lastActiveHistory + File.separator + ".workspace.xml", e);
			throw e;
		}
	}

	/**
	 * Check whether a correct .workspace.xml file 
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public static void checkWorkspaceXMLfile() throws Exception {
		SAXBuilder builder = new SAXBuilder();
		
		String lastActiveHistory = null;
		try {
			lastActiveHistory = WorkspaceHistoryFileHandler.getLastActiveHistory();
		} catch (Exception e) {
			logger.fatal(e.getMessage(), e);
			throw e;
		}
		
		//read .workspace.xml file 
		File xmlFile = new File(lastActiveHistory + "/.workspace.xml");
		
		// Get the root element
		Document doc = null;
		try {
			doc = builder.build(xmlFile);
		} catch (JDOMException e) {
			logger.fatal("Error occurred while reading workspace xml file: " + lastActiveHistory + "/.workspace.xml", e);
			throw e;
		} catch (IOException e) {
			logger.fatal("Error occurred while reading workspace xml file: " + lastActiveHistory + "/.workspace.xml", e);
			throw e;
		}
		
		//get the root node project...
		Element rootNode = doc.getRootElement();//workspace_list
		
		if(!rootNode.getName().equals("workspace"))
		{
			logger.fatal("Workspace file does not start with workspace tag: " + lastActiveHistory + "/.workspace.xml");
			throw new IOException("Workspace file does not start with workspace tag: " + lastActiveHistory + "/.workspace.xml");
		}
		
		List<Element> children = rootNode.getChildren();
		
		//need to check if children does not start with project tag
		for(Element child : children)
		{
			if(!child.getName().equals("project"))
			{
				logger.fatal("Child tag in Workspace file does not start with project tag: " + lastActiveHistory + "/.workspace.xml");
				throw new IOException("Child tag Workspace file does not start with project tag: " + lastActiveHistory + "/.workspace.xml");
			}
		}
		xmlFile.delete();
		try
		{
			writeXmlFile(doc, new File(lastActiveHistory + File.separator + ".workspace.xml"));
		} catch (IOException e) {
			logger.fatal("Error occurred while writing to workspace xml file: " 
					+ lastActiveHistory + File.separator + ".workspace.xml", e);
			throw e;
		}
	}

	/**
	 * renames a project entry in the workspace file
	 * @param entry project entry
	 * @param newName of the project entry
	 * @throws Exception
	 */
	public static void renameEntry(Entry entry, String newName) throws Exception
	{
		String workspaceLocation = 
				((WorkspaceProperty) DataModelHandler.instance().getRoot().getProperty()).getLocation();
		logger.debug("Workspace location is : " + workspaceLocation);
		File workspaceXmlFile = new File(workspaceLocation.substring(0, workspaceLocation.length()-1)
				+ File.separator + ".workspace.xml");
		if(!workspaceXmlFile.exists())
		{
			logger.fatal("workspace xml file does not exist: " + workspaceXmlFile.getAbsolutePath());
			throw new Exception("workspace xml file does not exist: " + workspaceXmlFile.getAbsolutePath());
		}
		SAXBuilder builder = new SAXBuilder();
		Document document = null;
		try
		{
			document = builder.build(workspaceXmlFile);
		} catch (JDOMException e) {
			logger.fatal("Error occurred while reading workspace xml file: " + workspaceXmlFile.getAbsolutePath(), e);
			throw e;
		} catch (IOException e) {
			logger.fatal("Error occurred while reading workspace xml file: " + workspaceXmlFile.getAbsolutePath(), e);
			throw e;
		}
		try
		{
			XPath xpath = XPath.newInstance("//project[@folder='"+ entry.getDisplayName() +"']");
			List<?> resultList = xpath.selectNodes(document);
			if (resultList.size() != 0)
			{
				Element element = (Element) resultList.get(0);
				if(element != null)
				{
					element.setAttribute("folder", newName);
					try
					{
						writeXmlFile(document, workspaceXmlFile);
					} catch (IOException e) {
						logger.fatal("Error occurred while writing to workspace xml file: " 
								+ workspaceXmlFile.getAbsolutePath(), e);
						throw e;
					}
				}
			}
		} catch (JDOMException e) {
			logger.fatal("Error occurred while reading workspace xml file: " + workspaceXmlFile.getAbsolutePath(), e);
			throw e;
		}
	}

	public void removeProject(String projectName) throws Exception
	{
		try {
			WorkspaceXMLHandler.removeEntry(projectName);
		} catch (Exception e) {
			logger.fatal(e.getMessage(), e);
			throw e;
		}
	}

	public static void removeProjectEntry(String projectName)
	{
		if(projectName != null)
		{
			try
			{
				String workspaceFolder = PropertyHandler.getVariable("workspace_location");
				workspaceFolder = workspaceFolder.substring(0, workspaceFolder.length()-1);
				File workspaceFile = new File(
						workspaceFolder + File.separator
						+ ".workspace.xml");
				if(workspaceFile.exists())
				{
					SAXBuilder builder = new SAXBuilder();
					Document doc = builder.build(workspaceFile);

					Element workspace = doc.getRootElement();
					List<Element> nonExistentElements = new ArrayList<Element>(); 
					Element childElement = null;
					if (workspace.getName().equals("workspace"))
					{
						List<?> children = workspace.getChildren("project");
						for(Object child : children)
						{
							if(child instanceof Element)
							{
								childElement = (Element) child;
								if(childElement.getAttribute("folder") != null)
								{
									if(childElement.getAttribute("folder").getValue().equals(projectName))
									{
										nonExistentElements.add(childElement);
									}
								}
							}

						}
						for(Element nEE : nonExistentElements)
						{
							children.remove(nEE);
						}
						writeXmlFile(doc, new File(workspaceFolder, ".workspace.xml"));
					}
				}
				else
				{
					logger.error("Unable to read workspace file.");
				}
			} catch (Exception e)
			{
				logger.error("Something went wrong while reading workspace file." + e.getMessage(), e);
			}
		}
	}

	public static void closeProject(String projectName) {
		if(projectName != null)
		{
			try
			{
				String workspaceFolder = PropertyHandler.getVariable("workspace_location");
				workspaceFolder = workspaceFolder.substring(0, workspaceFolder.length()-1);
				File workspaceFile = new File(
						workspaceFolder + File.separator
						+ ".workspace.xml");
				if(workspaceFile.exists())
				{
					SAXBuilder builder = new SAXBuilder();
					Document doc = builder.build(workspaceFile);

					Element workspace = doc.getRootElement();
					Element childElement = null;
					if (workspace.getName().equals("workspace"))
					{
						List<?> children = workspace.getChildren("project");
						for(Object child : children)
						{
							if(child instanceof Element)
							{
								childElement = (Element) child;
								if(childElement.getAttribute("folder") != null)
								{
									if(childElement.getAttribute("folder").getValue().equals(projectName))
									{
										childElement.setAttribute("open", "no");
										break;
									}
								}
							}
						}
						writeXmlFile(doc, new File(workspaceFolder, ".workspace.xml"));
					}
				}
				else
				{
					logger.error("Unable to locate workspace file.");
				}
			} catch (Exception e)
			{
				logger.error("Something went wrong while modifying workspace file." + e.getMessage(), e);
			}
		}
		
	}

	private static void writeXmlFile(Document doc, File file) throws IOException
	{
		FileWriter fileWriter = null;
		try 
		{
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
			logger.debug("Operating System is " + System.getProperty("os.name"));
			if(file.exists() && file.isHidden() 
					&& System.getProperty("os.name").startsWith("Windows"))
			{
				Files.setAttribute(file.toPath(), "dos:hidden", false);
			}
			fileWriter = new FileWriter(file);
			xmlOutput.output(doc, fileWriter);
			fileWriter.close();
		} 
		catch (IOException e) 
		{
			logger.fatal(e.getMessage(), e);
			throw e;
		} finally
		{
			IOUtils.closeQuietly(fileWriter);
		}
	}

}
