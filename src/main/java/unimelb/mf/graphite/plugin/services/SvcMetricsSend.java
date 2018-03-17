package unimelb.mf.graphite.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;
import unimelb.graphite.Metrics;

public class SvcMetricsSend extends PluginService {

    public static final String SERVICE_NAME = "graphite.metrics.send";

    private Interface _defn;

    public SvcMetricsSend() {
        _defn = new Interface();
        _defn.add(new Interface.Element("host", StringType.DEFAULT, "Graphite carbon server host.", 1, 1));
        _defn.add(new Interface.Element("port", new IntegerType(0, 65535),
                "Graphite carbon server port. Defaults to 2003 if protocol is plaintext, 2004 if protocol is pickle.",
                0, 1));
        _defn.add(new Interface.Element("protocol", new EnumType(new String[] { "plaintext", "pickle" }),
                "Graphite server protocol. Defaults to pickle.", 0, 1));

        SvcMetricsList.addToDefn(_defn);
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

        return "Sends the metrics to Graphite server.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        String protocol = args.stringValue("protocol", "pickle");
        String host = args.value("host");
        int port = args.elementExists("port") ? args.intValue("port")
                : ("pickle".equalsIgnoreCase(protocol) ? 2004 : 2003);
        Metrics metrics = new Metrics();
        SvcMetricsList.addMetrics(executor(), args, metrics);
        if (!metrics.isEmpty()) {
            if (protocol.equalsIgnoreCase("pickle")) {
                metrics.sendPickle(host, port);
            } else {
                metrics.sendPlainText(host, port);
            }
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
