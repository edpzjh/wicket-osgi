package org.apache.wicket.osgi.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    static BundleContext context;
    private ServiceRegistration initializerBundleListenerContext;

    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;
        IInitializerBundleListener initializerBundleListener = new IInitializerBundleListener(context);
        initializerBundleListenerContext =
            context.registerService(IInitializerBundleListener.class.getName(), initializerBundleListener, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        initializerBundleListenerContext.unregister();
        initializerBundleListenerContext = null;
    }

}
