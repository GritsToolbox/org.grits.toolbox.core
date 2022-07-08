package org.grits.toolbox.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

/**
 * 
 * 
 * 
 */
public class UtilityZip
{
	private static final long MAX_FOLDER_SIZE = 5*1024*1024*1024;
	private static Logger logger = Logger.getLogger(UtilityZip.class);
	public static final int bufferSize = 10*1024*1024;

	/**
	 * adds directory to a zip
	 * @param childFile
	 * @param relativePath path relative to the zip file
	 * e.g. if the directly is to be directly inside zip, write the name of the directory
	 * @param zipOutputStream
	 * @param progressDialog
	 * @throws Exception
	 */
	public static void addDirectory(File childFile, String relativePath,
			ZipOutputStream zipOutputStream) throws Exception
	{
		if(childFile.isDirectory())
		{
			if (childFile.length() < MAX_FOLDER_SIZE)
			{
				try
				{
					zipOutputStream.putNextEntry(new ZipEntry(relativePath + "/"));
					zipOutputStream.closeEntry();
					addFiles(childFile, relativePath, zipOutputStream);
				} catch (Exception ex)
				{
					logger.error(ex.getMessage(), ex);
					throw ex;
				}
			}
			else throw new Exception("Cannot compress folders with large sizes (>" + MAX_FOLDER_SIZE + "");
		}
	}

	/**
	 * adds file to a zip
	 * @param childFile
	 * @param relativePath path relative to the zip file
	 * e.g. if the file is to be directly inside zip, write the name of the file
	 * @param zipOutputStream
	 * @param progressDialog
	 * @throws Exception
	 */
	public static void addFile(File childFile, String relativePath,
			ZipOutputStream zipOutputStream) throws Exception
	{
		if(childFile.isFile())
		{
			try
			{
				ZipEntry zipEntry = new ZipEntry(relativePath);
				zipOutputStream.putNextEntry(zipEntry);
				FileInputStream fis = new FileInputStream(childFile);
				byte[] buffer = new byte[bufferSize];
				int len;
				while ((len = fis.read(buffer)) > 0)
				{
					zipOutputStream.write(buffer, 0, len);
				}
				fis.close();
				zipOutputStream.closeEntry();
			} catch (Exception ex)
			{
				logger.error(ex.getMessage(), ex);
				throw ex;
			}
		}
	}

	private static void addFiles(File folderToCopy, String relativeZipPath,
			ZipOutputStream zipOutputStream) throws Exception
	{
		String relativePath = null;
		for (File childFile : folderToCopy.listFiles())
		{
			if (childFile.exists())
			{
				relativePath = relativeZipPath == null || relativeZipPath.isEmpty() ?
						childFile.getName() : relativeZipPath + "/" + childFile.getName();
						if (childFile.isDirectory())
						{
							addDirectory(childFile, relativePath, zipOutputStream);
						}
						else if (childFile.isFile() || (childFile.isHidden() && !childFile.isDirectory()))
						{
							addFile(childFile, relativePath, zipOutputStream);
						} 
						else
						{
							logger.error("Error zipping " + childFile.getName());
						}
			}
		}
	}
}