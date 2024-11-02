package io.jwyoon1220.survival.plugin

import org.bukkit.plugin.java.JavaPlugin

class ConfigManager(plugins: JavaPlugin) {
    private val plugin: JavaPlugin = plugins

    fun setValue(key: String, value: Any) {
        plugin.config.set(key, value)
        plugin.saveConfig()
    }
    fun getValue(key: String): Any? {
        return plugin.config.get(key)
    }
}