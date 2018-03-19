package unimelb.mf.graphite.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import unimelb.mf.graphite.plugin.GraphitePluginModule;

public class SvcMetricPathPrefixDefaultSet extends PluginService {

    public static final String SERVICE_NAME = "graphite.metric.path.prefix.default.set";

    private Interface _defn;

    public SvcMetricPathPrefixDefaultSet() {
        _defn = new Interface();
        _defn.add(new Interface.Element("prefix", StringType.DEFAULT, "Metric path prefix.", 1, 1));
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
        return "Sets the default metric path prefix.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        String prefix = args.value("prefix");
        prefix = prefix.trim();
        while (prefix.endsWith(".")) {
            prefix = prefix.substring(0, prefix.length() - 1);
            prefix = prefix.trim();
        }
        if (prefix.isEmpty()) {
            throw new IllegalArgumentException("Invalid prefix: " + args.value("prefix"));
        }
        setDefaultMetricPathPrefix(executor(), prefix);
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    public static void setDefaultMetricPathPrefix(ServiceExecutor executor, String prefix) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("property", new String[] { "app", GraphitePluginModule.APPLICATION_NAME },
                GraphitePluginModule.APPLICATION_PROPERTY_DEFAULT_METRIC_PATH_PREFIX);
        boolean propertyExists = executor.execute("application.property.exists", dm.root()).booleanValue("exists");
        if (propertyExists) {
            dm = new XmlDocMaker("args");
            dm.add("property", new String[] { "app", GraphitePluginModule.APPLICATION_NAME, "name",
                    GraphitePluginModule.APPLICATION_PROPERTY_DEFAULT_METRIC_PATH_PREFIX }, prefix);
            executor.execute("application.property.set", dm.root());
        } else {
            dm = new XmlDocMaker("args");
            dm.push("property", new String[] { "app", GraphitePluginModule.APPLICATION_NAME, "name",
                    GraphitePluginModule.APPLICATION_PROPERTY_DEFAULT_METRIC_PATH_PREFIX });
            dm.add("value", prefix);
            dm.pop();
            executor.execute("application.property.create", dm.root());
        }
    }

}
