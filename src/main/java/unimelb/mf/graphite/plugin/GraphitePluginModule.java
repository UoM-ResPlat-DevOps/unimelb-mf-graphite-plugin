package unimelb.mf.graphite.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import arc.mf.plugin.ConfigurationResolver;
import arc.mf.plugin.PluginModule;
import arc.mf.plugin.PluginService;
import unimelb.mf.graphite.plugin.services.SvcMetricPathPrefixDefaultGet;
import unimelb.mf.graphite.plugin.services.SvcMetricPathPrefixDefaultReset;
import unimelb.mf.graphite.plugin.services.SvcMetricPathPrefixDefaultSet;
import unimelb.mf.graphite.plugin.services.SvcMetricsList;
import unimelb.mf.graphite.plugin.services.SvcMetricsSend;

public class GraphitePluginModule implements PluginModule {

    public static final String APPLICATION_NAME = "unimelb-mf-grahpite-plugin";

    public static final String APPLICATION_PROPERTY_DEFAULT_METRIC_PATH_PREFIX = "metric.path.prefix.default";

    private List<PluginService> _services;

    public GraphitePluginModule() {
        _services = new ArrayList<PluginService>();
        _services.add(new SvcMetricsList());
        _services.add(new SvcMetricsSend());
        _services.add(new SvcMetricPathPrefixDefaultGet());
        _services.add(new SvcMetricPathPrefixDefaultReset());
        _services.add(new SvcMetricPathPrefixDefaultSet());
    }

    public String description() {
        return "Plugin services to send server metrics to Graphite carbon server.";
    }

    public void initialize(ConfigurationResolver conf) throws Throwable {

    }

    public Collection<PluginService> services() {
        return _services;
    }

    public void shutdown(ConfigurationResolver conf) throws Throwable {

    }

    public String vendor() {
        return "Research Platform Services, Infrastructure Services, University Services, The University of Melbourne";
    }

    public String version() {
        return "0.0.1";
    }

}
