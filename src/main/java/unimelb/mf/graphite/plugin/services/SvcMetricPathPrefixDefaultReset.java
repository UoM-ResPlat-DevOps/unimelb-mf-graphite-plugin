package unimelb.mf.graphite.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import unimelb.mf.graphite.plugin.GraphitePluginModule;

public class SvcMetricPathPrefixDefaultReset extends PluginService {

    public static final String SERVICE_NAME = "graphite.metric.path.prefix.default.reset";

    private Interface _defn;

    public SvcMetricPathPrefixDefaultReset() {
        _defn = new Interface();
    }

    @Override
    public Access access() {
        return ACCESS_ADMINISTER;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Resets the default metric path prefix.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        resetDefaultMetricPathPrefix(executor());
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    public static void resetDefaultMetricPathPrefix(ServiceExecutor executor) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("property", new String[] { "app", GraphitePluginModule.APPLICATION_NAME },
                GraphitePluginModule.APPLICATION_PROPERTY_DEFAULT_METRIC_PATH_PREFIX);
        boolean propertyExists = executor.execute("application.property.exists", dm.root()).booleanValue("exists");
        if (propertyExists) {
            dm = new XmlDocMaker("args");
            dm.add("property", new String[] { "app", GraphitePluginModule.APPLICATION_NAME },
                    GraphitePluginModule.APPLICATION_PROPERTY_DEFAULT_METRIC_PATH_PREFIX);
            executor.execute("application.property.destroy", dm.root());
        }
    }

}
