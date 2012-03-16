package org.apache.wicket.osgi.internal;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.wicket.application.IClassResolver;
import org.osgi.framework.Bundle;

public class ExtremlyBadUncachingTemporaryDelegatingBundleClassLoader implements IClassResolver {

    @Override
    public Class<?> resolveClass(String classname) throws ClassNotFoundException {
        if (Activator.context == null) {
            throw new IllegalStateException("Activator context null");
        }
        Bundle[] bundles = Activator.context.getBundles();
        for (Bundle bundle : bundles) {
            try {
                return bundle.loadClass(classname);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        throw new ClassNotFoundException();
    }

    @Override
    public Iterator<URL> getResources(String name) {
        if (Activator.context == null) {
            throw new IllegalStateException("Activator context null");
        }
        ArrayList<URL> collectedResources = new ArrayList<URL>();
        Bundle[] values = Activator.context.getBundles();
        for (Bundle bundle : values) {
            Enumeration<URL> enumeration;
            try {
                enumeration = bundle.getResources(name);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            if (enumeration == null) {
                continue;
            }
            while (enumeration.hasMoreElements()) {
                collectedResources.add(enumeration.nextElement());
            }
        }
        return collectedResources.iterator();
    }

}
