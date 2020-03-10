package cn.craftyun.websafelogin;

import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.net.InetAddress;

public class WebSafeLoginListener implements Listener {
    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        String playerName = event.getName().toLowerCase();
        InetAddress address = event.getAddress();
        Jedis jedis = WebSafeLogin.getInstance().jedis;

        String md5PlayerName = DigestUtils.md5Hex(playerName);
        try {
            jedis.ping();
        } catch (JedisConnectionException e) {
            WebSafeLogin.getInstance().getLogger().info("连接Redis失败,自动重连：");
            WebSafeLogin.getInstance().connectRedis();
        }
        String playerIP = jedis.get("websafelogin." + md5PlayerName);
        if (playerIP == null || !playerIP.equals(address.getHostAddress())) {
            // 未通过验证
            event.setKickMessage("§c§l您的IP未通过验证，请进入网站验证后再登录!\n§9§l您的IP为：" + address.getHostAddress() + "\n§5§l有任何问题请联系服主");
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
            WebSafeLogin.getInstance().getLogger().info("玩家验证失败，用户名：" + playerName + "，用户MD5：" + md5PlayerName);
        }
        // 玩家验证成功，不进行拦截
    }
}
