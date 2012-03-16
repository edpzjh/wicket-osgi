package org.apache.wicket.osgi;

import javax.servlet.ServletContext;

import org.apache.wicket.IInitializer;
import org.apache.wicket.IModuleInitializer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.IWebApplicationFactory;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class OsgiWebApplicationFactory implements IWebApplicationFactory {

    /** One of the two params need to be filled; either a class or a bean in a guice, blueprint, ... context. */
    public static final String APP_CLASS_PARAM = "applicationClassName";
    public static final String BEAN_NAME_PARAM = "beanName";
    private ServiceTracker iinitializerProviderTracker;

    @Override
    public WebApplication createApplication(WicketFilter filter) {
        ServletContext sc = filter.getFilterConfig().getServletContext();
        final BundleContext bndCtx = (BundleContext) sc.getAttribute("osgi-bundlecontext");
        String appClassName = filter.getFilterConfig().getInitParameter(APP_CLASS_PARAM);
        String beanName = filter.getFilterConfig().getInitParameter(BEAN_NAME_PARAM);
        try
        {
            final Class<?> applicationClass = bndCtx.getBundle().loadClass(appClassName);
            if (WebApplication.class.isAssignableFrom(applicationClass))
            {
                // Construct WebApplication subclass
                final WebApplication webApplication = (WebApplication) applicationClass.newInstance();
                iinitializerProviderTracker = new ServiceTracker(bndCtx, IInitializer.class.getName(), null) {
                    @Override
                    public Object addingService(ServiceReference reference) {
                        IInitializer service = (IInitializer) super.addingService(reference);
                        service.init(webApplication);
                        return service;
                    }

                    @Override
                    public void modifiedService(ServiceReference reference, Object service) {
                        ((IInitializer) service).destroy(webApplication);
                        ((IInitializer) service).init(webApplication);
                        super.modifiedService(reference, service);
                    }

                    @Override
                    public void removedService(ServiceReference reference, Object service) {
                        ((IInitializer) service).destroy(webApplication);
                        super.removedService(reference, service);
                    }
                };
                webApplication.setModuleInitializer(new IModuleInitializer()
                {
	                @Override
	                public void initialize()
	                {
		                iinitializerProviderTracker.open(true);
	                }
                });
                return webApplication;
            }
            else
            {
                throw new WicketRuntimeException("Application class " + appClassName
                        + " must be a subclass of WebApplication");
            }
        } catch (ClassNotFoundException e) {
            throw new WicketRuntimeException("Unable to create application of class " + appClassName, e);
        } catch (InstantiationException e) {
            throw new WicketRuntimeException("Unable to create application of class " + appClassName, e);
        } catch (IllegalAccessException e) {
            throw new WicketRuntimeException("Unable to create application of class " + appClassName, e);
        } catch (SecurityException e) {
            throw new WicketRuntimeException("Unable to create application of class " + appClassName, e);
        }
    }

    @Override
    public void destroy(WicketFilter filter) {
        iinitializerProviderTracker.close();
    }

}
