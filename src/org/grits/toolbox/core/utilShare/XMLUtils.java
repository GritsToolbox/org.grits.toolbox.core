package org.grits.toolbox.core.utilShare;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.dataShare.PropertyHandler;

public class XMLUtils {
	private static final Logger logger = Logger.getLogger(XMLUtils.class);	

	public static Object getObjectFromXML(String xmlString, Class<?> sourceClass ) {
		Object obj = null;
		try
		{
			JAXBContext context = JAXBContext.newInstance(sourceClass);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			obj  = unmarshaller.unmarshal(new StringReader(xmlString));
		} catch (JAXBException e)
		{
			logger.error("The object could not be read from xml." + e.getMessage(), e);
		}
		return obj;
	}

	public static String marshalObjectXML(Object object, List<Class<?>> destClassess) {
		String xmlString = null;
		try
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			JAXBContext context = JAXBContext.newInstance(destClassess.toArray(new Class[destClassess.size()]));
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
			marshaller.marshal(object, os);
			xmlString = os.toString();
		} catch (JAXBException e) {
			e.printStackTrace();
			logger.error("The object could not be serialized as xml." + e.getMessage(), e);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("The object could not be serialized as xml." + e.getMessage(), e);
		}
		return xmlString;
	}	
	
	public static String marshalObjectXML(Object object) {
		String xmlString = null;
		try
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream();

			JAXBContext context = JAXBContext.newInstance(object.getClass());
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);

			marshaller.marshal(object, os);
			xmlString = os.toString();
		} catch (JAXBException e) {
			e.printStackTrace();
			logger.error("The object could not be serialized as xml." + e.getMessage(), e);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("The object could not be serialized as xml." + e.getMessage(), e);
		}
		return xmlString ;
	}	

	public static Object unmarshalObjectXML(String xmlFile, Class<?> destClass) {
		Object obj = null;
		try {
			FileInputStream inputStream = new FileInputStream(xmlFile);
			InputStreamReader reader = new InputStreamReader(inputStream, 
					PropertyHandler.GRITS_CHARACTER_ENCODING);
			JAXBContext context = JAXBContext.newInstance(destClass);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			obj = unmarshaller.unmarshal(reader);
		} catch (JAXBException e) {
			logger.error("The file could not be unmarshalled." + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("The file could not be unmarshalled." + e.getMessage(), e);
		}
		return obj;
	}

	public static Object unmarshalObjectXML(String xmlFile, List<Class<?>> destClasses) {
		Object obj = null;
		try {
			FileInputStream inputStream = new FileInputStream(xmlFile);
			InputStreamReader reader = new InputStreamReader(inputStream, 
					PropertyHandler.GRITS_CHARACTER_ENCODING);
    		JAXBContext context = JAXBContext.newInstance(destClasses.toArray(new Class[destClasses.size()]));
    		Unmarshaller unmarshaller = context.createUnmarshaller();
			obj = unmarshaller.unmarshal(reader);
		} catch (JAXBException e) {
			logger.error("The file could not be unmarshalled." + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("The file could not be unmarshalled." + e.getMessage(), e);
		}
		return obj;
	}

}
