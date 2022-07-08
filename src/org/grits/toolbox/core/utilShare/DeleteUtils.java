package org.grits.toolbox.core.utilShare;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.log4j.Logger;

/**
 * Delete file
 * @author kitaemyoung
 *
 */
public class DeleteUtils {

	//log4J Logger
	private static final Logger logger = Logger.getLogger(DeleteUtils.class);

	public static void delete(File toDelete) throws IOException
	{
        logger.debug("START : Deleting file " + toDelete.getName());
		if(toDelete.exists())
		{
			if(toDelete.isDirectory())
			{
				//if it contains files or folders.
				//delete children of this folder
				for (File temp : toDelete.listFiles()) {
					//recursive delete
					delete(temp);
				}

				//check the directory again, if empty then delete it
				if(toDelete.list().length==0){
					toDelete.delete();
				}
			}
			else
			{
				Files.delete(toDelete.toPath());
			/*	if(!toDelete.delete())
				{
					//if cannot delete, then show an error
					logger.error("Cannot delete file: " + toDelete.getAbsolutePath());
					throw new IOException("Cannot delete file: " + toDelete.getAbsolutePath());
				}*/
			}
	        logger.debug("END   : Deleting file " + toDelete.getName());
		}
	}
}
