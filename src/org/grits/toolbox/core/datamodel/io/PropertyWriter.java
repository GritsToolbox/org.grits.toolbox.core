package org.grits.toolbox.core.datamodel.io;

import java.io.IOException;

import org.grits.toolbox.core.datamodel.property.Property;
import org.jdom.Element;

public interface PropertyWriter 
{
    public void write(Property a_property, Element a_propertyElement) throws IOException;
}
