package unimelb.mf.graphite.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import unimelb.mf.graphite.plugin.GraphitePluginModule;

public class SvcMetricPathPrefixDefaultGet extends PluginService {

    public static final String SERVICE_NAME = "graphite.metric.path.prefix.default.get";

    private Interface _defn;

    public SvcMetricPathPrefixDefaultGet() {
        _defn = new Interface();
    }

    @Override
    public Access access() {
        return ACCESS_ACCESS;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Gets the default metric path prefix.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        String prefix = getDefaultMetricPathPrefix(executor());
        w.add("prefix", prefix);
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    public static String getDefaultMetricPathPrefix(ServiceExecutor executor) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("property", new String[] { "app", GraphitePluginModule.APPLICATION_NAME },
                GraphitePluginModule.APPLICATION_PROPERTY_DEFAULT_METRIC_PATH_PREFIX);
        boolean propertyExists = executor.execute("application.property.exists", dm.root()).booleanValue("exists");
        if (propertyExists) {
            return executor.execute("application.property.get", dm.root()).value("property");
        } else {
            return "mediaflux-" + executor.execute("server.uuid").value("uuid");
        }
    }

}
