package org.grits.toolbox.core.workspace;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

/**
 * Manage Workspace history file, which contains a list of workspace history. It is used the pop-up window before the workbench starts
 * @author kitaeUser
 *
 */
public class WorkspaceHistoryFileHandler {

	private static final String history_xml_location = PropertyHandler.getVariable("configuration_location") + "/history.xml";
	
	//log4J Logger
	private static final Logger logger = Logger.getLogger(WorkspaceHistoryFileHandler.class);
	
	/**
	 * Chech if the history.xml file exists or not
	 * @return
	 */
	public static boolean isHistoryExists()
	{
		File file = new File(history_xml_location);
		return file.exists();
	}

	/**
	 * Checked
	 * Create history.xml file. 
	 * @throws Exception 
	 * @throws IOException 
	 */
	public static void createHistoryXMLFile() throws Exception 
	{
		//set the root node
		Element workspaceList = new Element("workspace_list");
		Document doc = new Document(workspaceList);
		doc.setRootElement(workspaceList);
	
		XMLOutputter xmlOutput = new XMLOutputter();
	
		// display nice nice
		xmlOutput.setFormat(Format.getPrettyFormat());
		
		try {
			xmlOutput.output(doc, new FileWriter(history_xml_location));
		} catch (IOException e) {
			logger.fatal(e.getMessage(), e);
			throw new Exception("Unable to create history xml file: " + history_xml_location + ": " + e.getMessage());
		}
	}

	/**
	 * Get last_active user history value 
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
	public static String getLastActiveHistory() throws Exception 
	{
		String resultPath = null;
		if (isHistoryExists())
		{
			try {
				//initiates SAXBuilder
				SAXBuilder builder = new SAXBuilder();
				Document document = builder.build(new File(history_xml_location));
				XPath xpath = null;
				xpath = XPath.newInstance("//workspace[@last_active='true']");
				List resultList =  xpath.selectNodes( document );
				Iterator b = resultList.iterator();
				while (b.hasNext())
				{
				    Element oNode = (Element) b.next();
				    if (oNode.getAttribute("last_active").getValue().equals("true"))
					{
				    	resultPath = oNode.getAttribute("path").getValue();
					}
				}
			} catch (JDOMException e) {
				logger.fatal("Error occurred while reading history xml file: " + history_xml_location);
				throw new Exception("Error occurred while reading history xml file: " + history_xml_location);
			} catch (IOException e) {
				logger.fatal("Error occurred while reading history xml file: " + history_xml_location);
				throw new Exception("Error occurred while reading history xml file: " + history_xml_location);
			}
		}
		else
		{
			logger.fatal("workspace history file does not exist: " + history_xml_location);
			throw new Exception("workspace history file does not exist: " + history_xml_location);
		}
		return resultPath;
	}

	/**
	 * Read history xml file
	 * @return
	 * @throws IOException 
	 * @throws JDOMException 
	 * @throws Exception 
	 */
	public static List<WorkspaceHistoryEntry> readHistoryFile() throws IOException, JDOMException {
		//initiates SAXBuilder
		SAXBuilder builder = new SAXBuilder();
	
		//read .project file inside of _projectName folder 
		File xmlFile = new File(history_xml_location);
	
		List<WorkspaceHistoryEntry> resultList = new ArrayList<WorkspaceHistoryEntry>();
	
		//check if the configuration file exists!
		if (xmlFile.exists())
		{
			Document document = null;
			try {
				document = builder.build(xmlFile);
			} catch (JDOMException e) {
				logger.fatal(e);
				throw new JDOMException("Error occured while reading history xml file: " + history_xml_location,e);
			} catch (IOException e) {
				logger.fatal(e);
				throw new IOException("Error occured while reading history xml file: " + history_xml_location,e);
			}
			
			//get the root node project...
			Element rootNode = document.getRootElement();//workspace_list
			
			if(!rootNode.getName().equals("workspace_list"))
			{
				logger.fatal("Workspace History file does not start with workspace_list tag: " + history_xml_location);
				throw new IOException("Workspace History file does not start with workspace_list tag: " + history_xml_location);
			}
			
			//get child
			@SuppressWarnings("unchecked")
			List<Element> childrenNodes = rootNode.getChildren();
			
			//loop through each child where all names are same.
			for (Element child : childrenNodes)
			{
				if(!child.getName().equals("workspace"))
				{
					logger.fatal("Child element does not start with workspace tag in "+ history_xml_location);
					throw new IOException("Child element does not start with workspace tag in " + history_xml_location);
				}
				WorkspaceHistoryEntry con = new WorkspaceHistoryEntry();
				Attribute path = child.getAttribute("path");
				con.setPath(path.getValue());
				Attribute lastActive = child.getAttribute("last_active");
				con.setLastActive(lastActive.getValue());
				resultList.add(con);
			}
			
			return resultList;
		}
		else
		{
			logger.fatal("workspace history file does not exist: " + history_xml_location);
			throw new IOException("workspace history file does not exist: " + history_xml_location);
		}
	}

	/**
	 * @throws JDOMException 
	 * update the history xml file
	 * @param path selected path from a user
	 * @param flag whether the current list contains the path or not
	 * @throws IOException 
	 * @throws  
	 */
	public static void updateHistoryXMLFile(String path, boolean flag) throws IOException, JDOMException
	{
			//initiates SAXBuilder
			SAXBuilder builder = new SAXBuilder();
	
			//read .project file inside of _projectName folder 
			File xmlFile = new File(history_xml_location);
			if (!xmlFile.exists())
			{
				logger.fatal("Cannot find history xml file: " + history_xml_location);
				throw new IOException("Cannot find history xml file:" + history_xml_location);
			}
			
			// Get the root element
			Document doc = null;
			Element rootNode;
			try {
				doc = builder.build(xmlFile);
				rootNode = doc.getRootElement();
			} catch (JDOMException e) {
				logger.fatal(e.getMessage(), e);
				throw new JDOMException("Error occured while reading history xml file: "+ history_xml_location,e);
			}
	
			//if the page is not in the history list
			if (!flag)
			{
				// append a new node to staff
				// add new age element
				Element workspace = new Element("workspace");
				//need to add new attributes path, last_active
				List<Attribute> atts = new ArrayList<Attribute>();
				atts.add(new Attribute("path", path));
				atts.add(new Attribute("last_active", "true"));
				workspace.setAttributes(atts);
				//add workspace to the root
				doc.getRootElement().addContent(workspace);
			}
			
			//Get all nodes and set the flag to false;
			@SuppressWarnings("rawtypes")
			List childrenNodes = rootNode.getChildren();
			//loop through each child 
			for (int i=0; i < childrenNodes.size(); i++)
			{
				Element attr = (Element)childrenNodes.get(i);
				if (attr.getAttributeValue("path").equals(path))
				{
					attr.getAttribute("last_active").setValue("true");
				}
				else
				{
					attr.getAttribute("last_active").setValue("false");
				}
			}
			
			XMLOutputter xmlOutput = new XMLOutputter();
			
			// display nice nice
			xmlOutput.setFormat(Format.getPrettyFormat());
			try {
				xmlOutput.output(doc, new FileWriter(history_xml_location));
			} catch (IOException e) {
				logger.fatal(e.getMessage(), e);
				throw new IOException("Error occurred while writing to xml file: " + history_xml_location,e);
			}
	}

	public static void writeHistoryFile(WorkspaceHistory history) throws Exception {
		//read .project file inside of _projectName folder 
		File xmlFile = new File(history_xml_location);
		if (!xmlFile.exists())
		{
			logger.fatal("Cannot find history xml file: " + history_xml_location);
			throw new IOException("Cannot find history xml file:" + history_xml_location);
		}
		ByteArrayOutputStream os = new ByteArrayOutputStream();   
        JAXBContext context = JAXBContext.newInstance(WorkspaceHistory.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
        marshaller.marshal(history, os);
	
		FileWriter fileWriter = new FileWriter(xmlFile);
		fileWriter.write(os.toString((String) marshaller.getProperty(Marshaller.JAXB_ENCODING)));
	    fileWriter.close();
	    os.close();
	}

}
