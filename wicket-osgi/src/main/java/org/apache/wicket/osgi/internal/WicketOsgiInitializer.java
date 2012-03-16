package org.apache.wicket.osgi.internal;

import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WicketOsgiInitializer implements IInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(WicketOsgiInitializer.class);

    @Override
    public void init(final Application application) {
        LOG.debug("Initializing application '{}' for OSGi", application.getName());

        application.getApplicationSettings().setClassResolver(
            new ExtremlyBadUncachingTemporaryDelegatingBundleClassLoader());
    }

    @Override
    public void destroy(final Application application) {
        LOG.debug("Destroying OSGi initializer for application: {}", application.getName());
    }

}
