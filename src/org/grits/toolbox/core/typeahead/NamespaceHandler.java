package org.grits.toolbox.core.typeahead;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.Activator;
import org.grits.toolbox.widgets.processDialog.GRITSProgressDialog;
import org.grits.toolbox.widgets.progress.CancelableThread;
import org.grits.toolbox.widgets.progress.IProgressThreadHandler;
import org.grits.toolbox.widgets.tools.GRITSProcessStatus;
import org.grits.toolbox.widgets.tools.GRITSWorker;

/**
 * @author Sena Arpinar
 *
 */
public class NamespaceHandler {

    static Logger logger = Logger.getLogger(NamespaceHandler.class);

    private String pluginId = Activator.PLUGIN_ID;
	private String resourceFolderName = "namespace";
	
	protected static Map <String, String> namespaceFileMapping = new HashMap<>();
	protected static Map <String, PatriciaTrie<String>> namespaces = new HashMap<>();
	
	String namespace;

    /**
     * create NamespaceHandler
     * @param namespace name of the namespace 
     * (generally a unique name assigned to address a particular namespace)
     * @param filename name of the file that 
     * contains key value mapping of typeahead and its value
     */
	public NamespaceHandler (String namespace, String filename) {
		if (!namespaceFileMapping.containsKey(namespace))
			namespaceFileMapping.put(namespace, filename);
		this.namespace = namespace;
	}

	/**
     * create NamespaceHandler
     * @param namespace name of the namespace 
     * (generally a unique name assigned to address a particular namespace)
     * @param resourceFolderName null or path to the file where the file can be found
     * (if resourceFolderName is null it uses default resourceFolderName as "namespace")
     * @param filename name of the file that 
     * contains key value mapping of typeahead and its value
	 */
    public NamespaceHandler (String namespace, 
            String resourceFolderName, String filename) {
        this(namespace, filename);
        if(resourceFolderName != null)
            this.resourceFolderName = resourceFolderName;
    }

    /**
     * create NamespaceHandler
     * @param namespace name of the namespace 
     * (generally a unique name assigned to address a particular namespace)
     * @param resourceFolderName null or path to the file where the file can be found
     * (if resourceFolderName is null it uses default resourceFolderName as "namespace")
     * @param filename name of the file that 
     * contains key value mapping of typeahead and its value
     * @param pluginId null or pluginId id of the plugin that contains the file resource
     * (if pluginId is null it uses default pluginId as "org.grits.toolbox.core")
     */
    public NamespaceHandler (String namespace,
            String resourceFolderName, String filename, String pluginId) {
        this(namespace, resourceFolderName, filename);
        if(pluginId != null)
            this.pluginId = pluginId;
    }
	
	public String getNamespace() {
		return this.namespace;
	}

	/**
	 * override this method if you are extending this class from another plugin and return your own resource
	 * 
	 * @return the location of the namespace files within the jar file
	 */
	public URL getNamespaceURL() {
		return Platform.getBundle(pluginId).getResource(resourceFolderName);
	};
	
	/**
	 * This is the default implementation which expects a file with two columns separated with a tab (\t) and 
	 * each line corresponds to a new entry. First column should contain all the synonyms to be matched and the second 
	 * column should have the actual value to be used
	 * 
	 * NOTE: Subclasses should override this method to parse their specific file formats
	 *
	 * @param filename containing the synonyms
	 * @return a PatriciaTrie for searching
	 */
	public PatriciaTrie<String> parseNamespaceFile (String filename) {
		PatriciaTrie<String> trie = new PatriciaTrie<String>();
		
		long startTime = System.currentTimeMillis();
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(filename);
			logger.info("Reading namespaces from inputstream");
			BufferedReader names = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line=names.readLine())!=null) {
				String[] parts = line.split("\\t");
				String synonym = parts[0].trim();
				String name = parts[1].trim();
				
				trie.put (synonym.toLowerCase(), name); 
			}
			logger.info("Closing inputstream for namespace file");
			inputStream.close();
			names.close();
		} catch (FileNotFoundException e) {
			logger.error("Cannot find the namespace: " + filename, e);
		} catch (IOException e) {
			logger.error("Cannot read the namespace: " + filename, e);
		} catch (Exception | Error e) {
			logger.fatal("Cannot load the namespace: " + filename + "\n" + e.getMessage(), e);
			throw e;
		}
		logger.info("NamespaceHandler Took: " +  (System.currentTimeMillis() - startTime)/1000.0 + " seconds for file: " + filename);
		return trie;
	}
	
	public PatriciaTrie<String> getTrieForNamespace () {
	    String namespace = getNamespace();
		logger.info("getting trie for namespace : " + namespace);
	    if (namespace == null) {
	        return null;
	    }
	    PatriciaTrie<String> trie = namespaces.get(namespace);
	    if (trie == null) {  // initialize it for the first time
	    	logger.info("trie not found in the map. initializing...");

	    	GRITSProgressDialog progressDialog = new GRITSProgressDialog(Display.getCurrent().getActiveShell(), 0, false, false);
			progressDialog.open();
			progressDialog.getMajorProgressBarListener().setMaxValue(2);
			progressDialog.setGritsWorker(new GRITSWorker() {
				
				@Override
				public int doWork() {
					CancelableThread t = new CancelableThread() {
                        @Override
                        public boolean threadStart(IProgressThreadHandler a_progressThreadHandler) throws Exception {
                            try {
                            	String filename = namespaceFileMapping.get(namespace);
                        		
            		        	logger.info("file name for this\"" + namespace + "\" namespace : " + filename);
            			        if (filename == null) {
            			            return false;
            			        }
            			        try {
            			            URL resourceFileUrl = FileLocator.toFileURL(getNamespaceURL());
            			            String namespaceFilePath = resourceFileUrl.getPath() + filename;
            			            File namespaceFile = new File(namespaceFilePath);
            			            if(namespaceFile.exists())
            			            {
            			    	    	logger.info("Creating trie from namespace file : " + namespaceFile.getName());
            			    	    	updateListeners("Parsing namespace file", 1);
            			    	    	PatriciaTrie<String> trie = parseNamespaceFile(namespaceFile.getAbsolutePath());
            			                namespaces.put (namespace, trie);
            			            }
            			        } catch (IOException e) {
            			            logger.error ("Error getting the namespace file : " + filename, e);
            			            return false;
            			        }
                                return true;
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                                return false;
                            }
                        }
                    };
                    t.setProgressThreadHandler(progressDialog);
                    progressDialog.setThread(t);
                    t.start();  
                    while ( ! t.isCanceled() && ! t.isFinished() && t.isAlive() ) 
                    {
                        if (!Display.getDefault().readAndDispatch()) 
                        {
                        //    Display.getDefault().sleep();
                        }
                    }
                    if( t.isCanceled() ) {
                        t.interrupt();
                        return GRITSProcessStatus.CANCEL;
                    }
			    	
			        updateListeners("Done!", 2);
			        return GRITSProcessStatus.OK;
				}
			});
			
			progressDialog.startWorker();
	    }
	    return namespaces.get(namespace);
	}
}
