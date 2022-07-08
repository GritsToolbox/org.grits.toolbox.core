package org.grits.toolbox.core.utilShare;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.DataModelHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.ProjectFileHandler;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;

@Deprecated
public class SaveUtils {
    //log4J Logger
    private static final Logger logger = Logger.getLogger(SaveUtils.class);

    public static void saveEntry(Entry parent, Entry entry) throws IOException
    {
        DataModelHandler dm = PropertyHandler.getDataModel();
        dm.addEntry(parent, entry, true);
        Entry projectEntry = DataModelSearch.findParentByType(entry, ProjectProperty.TYPE);

        //save it to the file
        try {
            ProjectFileHandler.saveProject(projectEntry);
        } catch (IOException e) {
            //then delete this entry from the datamodel
            dm.deleteEntry(entry,false);
            logger.error(e.getMessage(),e);
            throw e;
        }
    }

    public static void saveProjectWithEntry(Entry entry) throws IOException
    {
        Entry projectEntry = DataModelSearch.findParentByType(entry, ProjectProperty.TYPE);
        try 
        {
            ProjectFileHandler.saveProject(projectEntry);
        } 
        catch (IOException e) 
        {
            logger.error(e.getMessage(),e);
            throw e;
        }
    }
}
