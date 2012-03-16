package org.apache.wicket.osgi.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.string.Strings;
import org.osgi.framework.Bundle;

public class InitializerService implements IInitializer {

    private HashMap<String, List<Class<?>>> initializer = new HashMap<String, List<Class<?>>>();

    public InitializerService(Bundle bundle, URL resource) {
        InputStream in = null;
        try
        {
            final Properties properties = new Properties();
            try {
                in = resource.openStream();
                properties.load(in);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            loadClasses(properties, bundle);
        } finally
        {
            try {
                IOUtils.close(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadClasses(Properties properties, Bundle bundle) {
        Set<Object> keySet = properties.keySet();
        addInitializer(Application.INITIALIZER, properties.getProperty(Application.INITIALIZER), bundle);
        for (Object key : keySet) {
            if (key.toString().endsWith("-" + Application.INITIALIZER)) {
                addInitializer(key.toString(), properties.getProperty(key.toString()), bundle);
            }
        }
    }

    private void addInitializer(String key, String property, Bundle bundle) {
        if (Strings.isEmpty(property)) {
            return;
        }
        if (!initializer.containsKey(key)) {
            initializer.put(key, new ArrayList<Class<?>>());
        }
        try {
            initializer.get(key).add(bundle.loadClass(property));
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void init(Application application) {
        List<IInitializer> initializerImplementations = loadInitializerImplementations(application);
        for (IInitializer initializerImpl : initializerImplementations) {
            initializerImpl.init(application);
        }
    }

    @Override
    public void destroy(Application application) {
        List<IInitializer> initializerImplementations = loadInitializerImplementations(application);
        for (IInitializer initializerImpl : initializerImplementations) {
            initializerImpl.destroy(application);
        }
    }

    private List<IInitializer> loadInitializerImplementations(Application application) {
        List<IInitializer> retVal = new ArrayList<IInitializer>();
        List<Class<?>> initializerClasses = initializer.get(Application.INITIALIZER);
        createInitializers(retVal, initializerClasses);
        List<Class<?>> applicationSpecificInitializerClasses = initializer.get(application.getName());
        createInitializers(retVal, applicationSpecificInitializerClasses);
        return retVal;
    }

    private void createInitializers(List<IInitializer> retVal, List<Class<?>> initializerClasses) {
        if (initializerClasses == null) {
            return;
        }
        for (Class<?> initializerClass : initializerClasses) {
            try {
                retVal.add((IInitializer) initializerClass.newInstance());
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
