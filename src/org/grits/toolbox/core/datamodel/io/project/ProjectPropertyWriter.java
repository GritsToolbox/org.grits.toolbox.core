package org.grits.toolbox.core.datamodel.io.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.grits.toolbox.core.datamodel.io.PropertyWriter;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.datamodel.property.project.ProjectDetails;
import org.jdom.Attribute;
import org.jdom.Element;

public class ProjectPropertyWriter implements PropertyWriter
{
    @Override
    public void write(Property property, Element propertyElement) throws IOException
    {
		if(property instanceof ProjectProperty)
		{
			ProjectProperty projectProperty = (ProjectProperty) property;
			if(projectProperty.getDetailsFile() != null 
					&& projectProperty.getDetailsFile().getName() != null)
			{
				Element fileElement = new Element("file");
				List<Attribute> attributes = new ArrayList<Attribute>();
				attributes.add(new Attribute("name", projectProperty.getDetailsFile().getName()));
				attributes.add(new Attribute("version", ProjectDetails.CURRENT_VERSION));
				attributes.add(new Attribute("type", ProjectProperty.DETAILS_TYPE));
				fileElement.setAttributes(attributes);
				propertyElement.setContent(fileElement);
			}
			else
				throw new IOException("Property could not be added as its project_details file is missing.");
		}
		else
		{
			throw new IOException("This property is not a Project Property");
		}
    }
}
