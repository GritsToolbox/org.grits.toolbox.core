package org.grits.toolbox.core.utilShare;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.FrameworkUtil;

public class ResourceLocatorUtils {
    public static String getLegalPathOfResource(Object oClass, String sResource) throws IOException {
        URL url1 = FrameworkUtil.getBundle(oClass.getClass()).getEntry(
                sResource);
        String prePath = FileLocator.toFileURL(url1).toString();
        prePath = prePath.replace(" ", "%20");
        URI uri2 = URI.create(prePath);
        String path = uri2.toString();
        String postPath = path.substring(path.indexOf("/") + 1);
        postPath = postPath.replace("%20", " ");
        if ( postPath.indexOf(":") > postPath.indexOf("/") || postPath.indexOf(":") == -1 )
        {
            postPath = "/" + postPath;
        }
        return postPath;
    }
}
