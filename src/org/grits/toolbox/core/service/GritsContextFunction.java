/**
 * 
 */
package org.grits.toolbox.core.service;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.grits.toolbox.core.datamodel.GritsDataModelService;
import org.grits.toolbox.core.service.impl.GritsUIService;
import org.osgi.framework.FrameworkUtil;

/**
 * 
 *
 */
public class GritsContextFunction implements IContextFunction
{
	private static final Logger logger = Logger.getLogger(GritsContextFunction.class);

	public GritsContextFunction()
	{

	}

	@Override
	public Object compute(IEclipseContext context, String contextKey)
	{
		logger.info("Creating service : " + contextKey);

		if(IGritsDataModelService.class.getName().equals(contextKey))
		{
			// application not yet available
			// IEclipseContext applicationContext = context.get(MApplication.class).getContext();
			IGritsDataModelService dataModelService =
					ContextInjectionFactory.make(GritsDataModelService.class, context);
			context.set(IGritsDataModelService.class, dataModelService);

			logger.info("Registering service in osgi service layer : "
					+ IGritsDataModelService.class.getName());
			FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(
					IGritsDataModelService.class, dataModelService, null);

			return dataModelService;
		}
		else if(IGritsUIService.class.getName().equals(contextKey))
		{
			// application not yet available
			IGritsUIService gritsUIService =
					ContextInjectionFactory.make(GritsUIService.class, context);
			context.set(IGritsUIService.class, gritsUIService);

			logger.info("Registering service in osgi service layer : "
					+ IGritsUIService.class.getName());
			FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(
					IGritsUIService.class, gritsUIService, null);

			return gritsUIService;
		}

		return null;
	}
}
