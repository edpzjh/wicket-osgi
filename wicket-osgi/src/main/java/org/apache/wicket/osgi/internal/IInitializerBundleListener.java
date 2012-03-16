package org.apache.wicket.osgi.internal;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.IInitializer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceRegistration;

public class IInitializerBundleListener implements BundleListener {

    private Map<String, ServiceRegistration> initializerServices = new HashMap<String, ServiceRegistration>();
    private final BundleContext ctx;

    public IInitializerBundleListener(BundleContext ctx) {
        this.ctx = ctx;
        analyseAlreadStartedBundles();
    }

    private void analyseAlreadStartedBundles() {
        Bundle[] bundles = ctx.getBundles();
        for (Bundle bundle : bundles) {
            analyseBundleToAddInitializerService(bundle);
        }
    }

    private void analyseBundleToAddInitializerService(Bundle bundle) {
        URL resource = bundle.getResource("wicket.properties");
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
            if (initializerServices.containsKey(event.getBundle().getSymbolicName())) {
                initializerServices.get(event.getBundle().getSymbolicName()).unregister();
                initializerServices.remove(event.getBundle().getSymbolicName());
            }
        }
    }

}
