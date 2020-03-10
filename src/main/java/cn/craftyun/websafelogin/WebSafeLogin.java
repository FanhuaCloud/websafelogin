package cn.craftyun.websafelogin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public final class WebSafeLogin extends JavaPlugin {
    public static WebSafeLogin instance;
    public Jedis jedis;
    public WebSafeLoginListener webSafeLoginListener;
    public FileConfiguration configuration;

    public static WebSafeLogin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        // 加载配置
        loadConfig();
        getLogger().info("Plugin load success, wait to connect redis server.");
        connectRedis();
        // 初始化监听器
        webSafeLoginListener = new WebSafeLoginListener();
        getServer().getPluginManager().registerEvents(webSafeLoginListener, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (jedis.isConnected()) {
            jedis.close();
        }
        getLogger().info("Plugin disable success.");
    }

    public void connectRedis() {
        // 连接redis数据库
        try {
            jedis = new Jedis(configuration.getString("redis.ip"), configuration.getInt("redis.port"));
            jedis.auth(configuration.getString("redis.password"));
            jedis.select(configuration.getInt("redis.db"));
            if (jedis.ping().equals("PONG")) {
                getLogger().info("Connect redis success");
            }
        } catch (JedisConnectionException e) {
            getLogger().info("Connect redis failed,Please check connect info." + e);
        }
    }

    public void loadConfig() {
        configuration = getConfig();
        configuration.addDefault("redis", null);
        configuration.addDefault("redis.ip", "127.0.0.1");
        configuration.addDefault("redis.port", 6379);
        configuration.addDefault("redis.db", 0);
        configuration.addDefault("redis.password", "password");
        configuration.options().copyDefaults(true);
        saveConfig();
    }
}
