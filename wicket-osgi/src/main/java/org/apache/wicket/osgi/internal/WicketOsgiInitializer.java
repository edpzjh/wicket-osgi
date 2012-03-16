package org.apache.wicket.osgi.internal;

import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;

public class WicketOsgiInitializer implements IInitializer {

    @Override
    public void init(Application application) {
        application.getApplicationSettings().setClassResolver(
            new ExtremlyBadUncachingTemporaryDelegatingBundleClassLoader());
    }

    @Override
    public void destroy(Application application) {
        // TODO Auto-generated method stub

    }

}
