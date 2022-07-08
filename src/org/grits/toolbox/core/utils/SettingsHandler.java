package org.grits.toolbox.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.GeneralSettings;

public class SettingsHandler {
	
	public static final String settingsFile = "settings.xml";
	
	public static void writeSettings(GeneralSettings settings) throws Exception {
		String file = PropertyHandler.getVariable("configuration_location") + File.separator + settingsFile;
		ByteArrayOutputStream os = new ByteArrayOutputStream();   
        JAXBContext context = JAXBContext.newInstance(GeneralSettings.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
        marshaller.marshal(settings, os);
	
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(os.toString((String) marshaller.getProperty(Marshaller.JAXB_ENCODING)));
	    fileWriter.close();
	    os.close();
	}
	
	public static GeneralSettings readSettings() throws Exception {
		GeneralSettings settings= null;
		FileInputStream inputStream;
		String file = PropertyHandler.getVariable("configuration_location") + File.separator + settingsFile;
		inputStream = new FileInputStream(file);
		InputStreamReader reader = new InputStreamReader(inputStream, PropertyHandler.GRITS_CHARACTER_ENCODING);
        JAXBContext context = JAXBContext.newInstance(GeneralSettings.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        settings = (GeneralSettings) unmarshaller.unmarshal(reader);
		
		return settings;
	}
}
