package unimelb.mf.graphite.plugin.services;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.DateType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.utils.DateTime;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import unimelb.graphite.Metric;
import unimelb.graphite.Metrics;

public class SvcMetricsList extends PluginService {

    public static final String SERVICE_NAME = "graphite.metrics.list";

    public static final String[] SERVER_METRICS = new String[] { "uptime", "thread", "licence", "connection", "memory",
            "os", "store", "stream", "task", "all" };

    private Interface _defn;

    public SvcMetricsList() {
        _defn = new Interface();
        addToDefn(_defn);
    }

    static void addToDefn(Interface defn) {
        Interface.Element metric = new Interface.Element("metric", XmlDocType.DEFAULT, "Metric.", 0, Integer.MAX_VALUE);
        metric.add(new Interface.Element("path", StringType.DEFAULT, "Metric path.", 1, 1));
        metric.add(new Interface.Element("time", DateType.DEFAULT, "Metric timestamp. Defaults to current server time.",
                0, 1));
        metric.add(new Interface.Element("value", StringType.DEFAULT, "Metric value.", 0, 1));
        Interface.Element service = new Interface.Element("service", XmlDocType.DEFAULT,
                "The service to retrieve the metric value.", 0, 1);
        service.add(new Interface.Attribute("name", StringType.DEFAULT, "Service name.", 1));
        service.add(new Interface.Attribute("xpath", StringType.DEFAULT,
                "XPATH to retrieve the value from service result.", 1));
        service.setIgnoreDescendants(true);
        metric.add(service);
        defn.add(metric);

        defn.add(new Interface.Element("server-metrics", new EnumType(SERVER_METRICS),
                "Predefined Mediaflux server metrics.", 0, SERVER_METRICS.length));
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
        return "List the specified metrics (without sending to Graphite server).";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        Metrics metrics = new Metrics();
        addMetrics(executor(), args, metrics);
        for (Metric metric : metrics) {
            w.add("metric", new String[] { "time", DateTime.string(metric.timestampDate()), "path", metric.path(),
                    "value", metric.value() });
        }
    }

    static void addMetrics(ServiceExecutor executor, Element args, Metrics metrics) throws Throwable {

        String metricPathPrefix = SvcMetricPathPrefixDefaultGet.getDefaultMetricPathPrefix(executor);
        if (args.elementExists("metric")) {
            List<XmlDoc.Element> mes = args.elements("metric");
            for (XmlDoc.Element me : mes) {
                addMetric(executor, metricPathPrefix, me, metrics);
            }
        }
        Set<String> sms = new LinkedHashSet<String>();
        if (args.elementExists("server-metrics")) {
            sms.addAll(args.values("server-metrics"));
        } else {
            if (!args.elementExists("metric")) {
                sms.add("all");
            }
        }
        if (!sms.isEmpty()) {
            addServerMetrics(executor, metricPathPrefix, sms, metrics);
        }

    }

    private static void addServerMetrics(ServiceExecutor executor, String metricPathPrefix, Set<String> sms,
            Metrics metrics) throws Throwable {
        XmlDoc.Element status = executor.execute("server.status");
        Date time = new Date();
        if (sms.contains("all") || sms.contains("uptime")) {
            double uptime = status.doubleValue("uptime", 0.0);
            String units = status.stringValue("uptime/@units");
            if ("seconds".equalsIgnoreCase(units)) {

            } else if ("minutes".equalsIgnoreCase(units)) {
                uptime *= 60.0;
            } else if ("hours".equalsIgnoreCase(units)) {
                uptime *= 3600.0;
            } else if ("days".equalsIgnoreCase(units)) {
                uptime *= 86400.0;
            } else if ("weeks".equalsIgnoreCase(units)) {
                uptime *= 604800.0;
            } else {
                throw new Exception("Unexpected uptime/@units: " + units);
            }
            metrics.addMetric(metricPathPrefix + ".uptime.seconds", time, uptime);
        }
        if (sms.contains("all") || sms.contains("thread")) {
            metrics.addMetric(metricPathPrefix + ".threads.total", time, status.value("threads/total"));
        }
        if (sms.contains("all") || sms.contains("memory")) {
            double memoryUsedMB = status.doubleValue("memory/used");
            if ("GB".equalsIgnoreCase(status.value("memory/used/@units"))) {
                memoryUsedMB = memoryUsedMB * 1000.0;
            } else if ("TB".equalsIgnoreCase(status.value("memory/used/@units"))) {
                memoryUsedMB = memoryUsedMB * 1000000.0;
            } else if ("PB".equalsIgnoreCase(status.value("memory/used/@units"))) {
                memoryUsedMB = memoryUsedMB * 1000000000.0;
            }
            metrics.addMetric(metricPathPrefix + ".memory.used.mb", time, String.format("%.3f", memoryUsedMB));
            double memoryFreeMB = status.doubleValue("memory/free");
            if ("GB".equalsIgnoreCase(status.value("memory/free/@units"))) {
                memoryFreeMB = memoryFreeMB * 1000.0;
            } else if ("TB".equalsIgnoreCase(status.value("memory/free/@units"))) {
                memoryFreeMB = memoryFreeMB * 1000000.0;
            } else if ("PB".equalsIgnoreCase(status.value("memory/free/@units"))) {
                memoryFreeMB = memoryFreeMB * 1000000000.0;
            }
            metrics.addMetric(metricPathPrefix + ".memory.free.mb", time, String.format("%.3f", memoryFreeMB));
        }
        if (sms.contains("all") || sms.contains("stream")) {
            metrics.addMetric(metricPathPrefix + ".streams.open", time, status.value("streams/nbopen"));
        }
        if (sms.contains("all") || sms.contains("os")) {
            metrics.addMetric(metricPathPrefix + ".system.cpu.load.percent", time,
                    status.value("operating-system/system-cpu-load/@pc"));
            metrics.addMetric(metricPathPrefix + ".process.cpu.load.percent", time,
                    status.value("operating-system/process-cpu-load/@pc"));
            metrics.addMetric(metricPathPrefix + ".process.cpu.time.seconds", time,
                    status.longValue("operating-system/process-cpu-time/@millisecs", 0) / 1000L);
            metrics.addMetric(metricPathPrefix + ".system.load.average", time,
                    status.value("operating-system/system-load-average"));

        }
        if (sms.contains("all") || sms.contains("task")) {
            metrics.addMetric(metricPathPrefix + ".tasks", time, status.value("number-of-tasks"));
        }
        if (sms.contains("all") || sms.contains("licence")) {
            XmlDoc.Element licence = executor.execute("licence.describe").element("licence");
            Date time1 = new Date();
            int total = licence.intValue("total");
            int remaining = licence.intValue("remaining");
            metrics.addMetric(metricPathPrefix + ".licence.used", time1, total - remaining);
            metrics.addMetric(metricPathPrefix + ".licence.remaining", time1, remaining);
        }
        if (sms.contains("all") || sms.contains("connection")) {
            XmlDoc.Element re = executor.execute("network.describe");
            Date time2 = new Date();
            Set<String> types = new LinkedHashSet<String>(re.values("service/@type"));
            for (String type : types) {
                List<XmlDoc.Element> ses = re.elements("service[@type='" + type + "']");
                for (XmlDoc.Element se : ses) {
                    String port = se.value("@port");
                    metrics.addMetric(metricPathPrefix + "." + type + "." + port + ".connections.active", time2,
                            se.value("connections/active"));
                    metrics.addMetric(metricPathPrefix + "." + type + "." + port + ".connections.total", time2,
                            se.value("connections/total"));
                }
            }
        }
        if (sms.contains("all") || sms.contains("store")) {
            List<XmlDoc.Element> ses = executor.execute("asset.store.describe").elements("store");
            Date time3 = new Date();
            if (ses != null) {
                for (XmlDoc.Element se : ses) {
                    String storeType = se.value("type");
                    String storeId = se.value("@id");
                    long free = se.longValue("mount/free", 0);
                    double freeGB = (double) ((double) free) / 1000000000.0;
                    long size = se.longValue("mount/size", 0);
                    double sizeGB = (double) ((double) size) / 1000000000.0;

                    String prefix = metricPathPrefix + "." + storeType + ".store." + storeId;
                    metrics.addMetric(prefix + ".free.gb", time3, String.format("%.3f", freeGB));
                    String freePC = se.value("mount/free/@partition-percent");
                    if (freePC != null) {
                        metrics.addMetric(prefix + ".free.partition.percent", time3, freePC);
                    }
                    metrics.addMetric(prefix + ".size.gb", time3, String.format("%.3f", sizeGB));
                    String sizePC = se.value("mount/size/@partition-percent");
                    if (sizePC != null) {
                        metrics.addMetric(prefix + ".size.partition.percent", time3, sizePC);
                    }
                }
            }
        }
    }

    private static void addMetric(ServiceExecutor executor, String metricPathPrefix, Element me, Metrics metrics)
            throws Throwable {
        while (metricPathPrefix.endsWith(".") || metricPathPrefix.endsWith(" ")) {
            metricPathPrefix = metricPathPrefix.substring(0, metricPathPrefix.length() - 1);
        }
        String path = me.value("path");
        if (!path.startsWith(metricPathPrefix + ".")) {
            while (path.startsWith(".") || path.startsWith(" ")) {
                path = path.substring(1);
            }
            path = metricPathPrefix + "." + path;
        }
        Date time = me.dateValue("time", new Date());
        String value = me.value("value");
        XmlDoc.Element service = me.element("service");
        if (value != null) {
            metrics.addMetric(path, time, value);
        } else if (service != null) {
            String serviceName = service.value("@name");
            String resultXPath = service.value("@xpath");
            XmlDocMaker dm = new XmlDocMaker("args");
            dm.add(service, false);
            value = executor.execute(serviceName, dm.root()).value(resultXPath);
            if (value != null) {
                time = new Date();
                metrics.addMetric(path, time, value);
            }
        } else {
            throw new IllegalArgumentException("No metric/value or metric/service.");
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
