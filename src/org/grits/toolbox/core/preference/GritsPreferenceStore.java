/**
 * 
 */
package org.grits.toolbox.core.preference;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.share.IGritsPreferenceStore;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * 
 *
 */
@Singleton
public class GritsPreferenceStore implements IGritsPreferenceStore
{
	private static final Logger logger = Logger.getLogger(GritsPreferenceStore.class);

	@Inject IEclipseContext eclipseContext;

	/**
	 * {@inheritDoc}
	 * 
	 */
	public PreferenceEntity getPreferenceEntity(String preferenceName) throws UnsupportedVersionException
	{
		logger.info("Reading preference : " + preferenceName);

		PreferenceEntity preferenceEntity = null;
		Element preferenceElement = getPreferenceElement(preferenceName);
		if(preferenceElement != null)
		{
			try
			{
				JAXBContext context = JAXBContext.newInstance(PreferenceEntity.class);
				Unmarshaller unmarshaller = context.createUnmarshaller();
				preferenceEntity  = (PreferenceEntity) unmarshaller.unmarshal(
						new StringReader(new XMLOutputter().outputString(preferenceElement)));
				if(preferenceEntity.getVersion() == null)
				{
					logger.fatal("Version was not found for the preference : " + preferenceName);
					throw new UnsupportedVersionException("Version was not found for the preference : "
							+ preferenceName, null);
				}
			} catch (JAXBException e)
			{
				String errorMessage = "The preference object : " + preferenceName 
						+ " could not be read from xml.\n" + e.getMessage();
				logger.error(errorMessage, e);
				throw new UnsupportedVersionException(errorMessage, e, null);
			}
		}
		return preferenceEntity;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	public Element getPreferenceElement(String preferenceName)
	{
		logger.info("Reading preference xml element : " + preferenceName);

		if(preferenceName == null)
			return null;

		try
		{
			Document document = getDocument();
			Element preferenceElement = null;
			for(Object childElement : document.getRootElement().getChildren())
			{
				if(childElement instanceof Element
						&& preferenceName.equals(((Element) childElement).getAttributeValue("name")))
				{
					preferenceElement = (Element) childElement;
					break;
				}
			}
			return preferenceElement;
		} catch (Exception e)
		{
			logger.fatal(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	public boolean savePreference(PreferenceEntity preferenceEntity)
	{
		try
		{
			if(preferenceEntity != null
					&& preferenceEntity.getName() != null
					&& preferenceEntity.getVersion() != null)
			{
				logger.info("Saving preference : " + preferenceEntity.getName());

				Element preferenceElement = new Element("preference");
				List<Attribute> attributes = new ArrayList<Attribute>();
				attributes.add(new Attribute("name", preferenceEntity.getName()));
				attributes.add(new Attribute("version", preferenceEntity.getVersion()));
				preferenceElement.setAttributes(attributes);
				preferenceElement.setText(preferenceEntity.getValue());

				boolean saved = savePreference(preferenceElement);
				if(saved)
				{
					IEventBroker eventBroker = eclipseContext.get(IEventBroker.class);
					if (eventBroker != null) 
						eventBroker.post(
							EVENT_TOPIC_PREF_VALUE_CHANGED, preferenceEntity.getName());
				}
				else
					logger.error("Preference could not be saved : " + preferenceEntity.getName());
				return saved;
			}
		} catch (Exception e)
		{
			logger.fatal("The preference entity could not be serialized as xml." + e.getMessage(), e);
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private boolean savePreference(Element preferenceElement)
	{
		boolean saved = false;
		try
		{
			String preferenceName = preferenceElement == null 
					? null : preferenceElement.getAttributeValue("name");
			Document document = getDocument();
			if(document != null && preferenceName != null)
			{
				Element preferences = document.getRootElement();
				removeChildren(preferences, preferenceName);
				preferences.getChildren().add(preferenceElement);
				saveDocument(document);
				saved = true;
			}
		} catch (IOException e)
		{
			logger.error(e.getMessage(), e);
		}
		return saved;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	public boolean removePreference(String preferenceName)
	{
		logger.info("Removing preference : " + preferenceName);
		try
		{
			Document document = getDocument();
			if(document != null && preferenceName != null)
			{
				boolean removed = removeChildren(document.getRootElement(), preferenceName);
				saveDocument(document);
				return removed;
			}
		} catch (IOException e)
		{
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	private static boolean removeChildren(Element parentElement, String childName)
	{
		List<Element> elementsToRemove = new ArrayList<Element>();
		if(parentElement != null && childName != null)
		{
			for(Object ch : parentElement.getChildren())
			{
				if(ch instanceof Element 
						&& childName.equals(((Element) ch).getAttributeValue("name")))
				{
					elementsToRemove.add((Element) ch);
				}
			}
			for(Element ele : elementsToRemove)
			{
				parentElement.removeContent(ele);
			}
		}
		return !elementsToRemove.isEmpty();
	}

	private Document getDocument()
	{
		Document document = null;
		try
		{
			File preferenceFile = new File(PREFERENCE_LOCATION);
			if(preferenceFile.exists() && preferenceFile.isFile())
			{
				if(preferenceFile.isHidden()
						&& System.getProperty("os.name").startsWith("Windows"))
				{
					Files.setAttribute(preferenceFile.toPath(), "dos:hidden", false);
				}
				document = (new SAXBuilder()).build(preferenceFile);
			}
			// else create an empty preference file
			else
			{
				logger.info("Preference file does not exist. "
						+ "Creating new preference file : " + PREFERENCE_LOCATION);
				document = new Document(new Element("preferences"));
				saveDocument(document);
			}
		} catch (Exception ex)
		{
			logger.fatal("Error reading Preference file.\n" + ex.getMessage(), ex);
			document = null;
		}
		return document;
	}

	private void saveDocument(Document document) throws IOException
	{
		logger.info("Saving Preference file to : " + PREFERENCE_LOCATION);
		FileWriter fileWriter = null;
		try
		{
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
			fileWriter = new FileWriter(PREFERENCE_LOCATION);
			xmlOutput.output(document, fileWriter);
			fileWriter.close();
		} catch (IOException ex)
		{
			logger.error(ex.getMessage(), ex);
		} finally
		{
			IOUtils.closeQuietly(fileWriter);
		}
	}
}
