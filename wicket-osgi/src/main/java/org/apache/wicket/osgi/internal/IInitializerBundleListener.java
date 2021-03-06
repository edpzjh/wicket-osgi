package org.apache.wicket.osgi.internal;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceRegistration;

public class IInitializerBundleListener implements BundleListener {

    private final Map<String, ServiceRegistration> initializerServices;
    private final BundleContext ctx;

    public IInitializerBundleListener(BundleContext ctx) {
        this.ctx = ctx;
        this.initializerServices = new HashMap<String, ServiceRegistration>();
        analyseAlreadyStartedBundles();
    }

    private void analyseAlreadyStartedBundles() {
        Bundle[] bundles = ctx.getBundles();
        for (Bundle bundle : bundles) {
            analyseBundleToAddInitializerService(bundle);
        }
    }

    private void analyseBundleToAddInitializerService(Bundle bundle) {
        URL resource = bundle.getResource(Application.WICKET_PROPERTIES);
        if (resource != null) {
            addIInitializerServiceToBundle(bundle, resource);
        }
    }

    private void addIInitializerServiceToBundle(Bundle bundle, URL resource) {
        InitializerService initializerService = new InitializerService(bundle, resource);
        ServiceRegistration service =
            bundle.getBundleContext().registerService(IInitializer.class.getName(), initializerService, null);
        initializerServices.put(bundle.getSymbolicName(), service);
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        if (event.getType() == BundleEvent.STARTED) {
            analyseBundleToAddInitializerService(event.getBundle());
        }
        if (event.getType() == BundleEvent.STOPPING) {
            String symbolicName = event.getBundle().getSymbolicName();
            if (initializerServices.containsKey(symbolicName)) {
                initializerServices.get(symbolicName).unregister();
                initializerServices.remove(symbolicName);
            }
        }
    }

}
