# ============================================================================
# Uninstall Plugins
# ============================================================================
set plugin_label           [string toupper PACKAGE_$package]
set plugin_namespace       /mflux/plugins/unimelb-mf-graphite-plugin
set plugin_zip             unimelb-mf-graphite-plugin.zip
set plugin_jar             unimelb-mf-graphite-plugin.jar
set module_class           unimelb.mf.graphite.plugin.GraphitePluginModule

if { [xvalue exists [plugin.module.exists :path ${plugin_namespace}/${plugin_jar} :class ${module_class}]] == "true" } {
    plugin.module.remove :path ${plugin_namespace}/${plugin_jar} :class ${module_class}
}

if { [xvalue exists [asset.namespace.exists :namespace ${plugin_namespace}]] == "true" } {
    asset.namespace.destroy :namespace "${plugin_namespace}"
}

system.service.reload

srefresh
