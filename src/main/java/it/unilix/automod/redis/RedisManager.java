package it.unilix.automod.redis;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nextdevv.auctions.Auctions;
import com.nextdevv.auctions.models.Auction;
import com.nextdevv.auctions.models.Bid;
import com.nextdevv.auctions.redis.redisdata.RedisAbstract;
import com.nextdevv.auctions.redis.redisdata.RedisPubSub;
import com.nextdevv.auctions.utils.ClassSerializer;
import io.lettuce.core.RedisClient;
import it.unilix.automod.AutoMod;
import it.unilix.automod.redis.redisdata.RedisAbstract;
import it.unilix.automod.redis.redisdata.RedisPubSub;

import java.io.File;


public class RedisImpl extends RedisAbstract {
    private final Gson gson;
    private final AutoMod plugin;

	public RedisImpl(RedisClient lettuceRedisClient, int size, AutoMod plugin) {
		super(lettuceRedisClient, size);
		gson = new Gson().newBuilder().setPrettyPrinting().create();
		this.plugin = plugin;
		subscribe();
	}

    public void publish(JsonObject jsonObject) {
        jsonObject.addProperty("server", getServerName());
        getConnectionAsync(c -> c.publish("auction", jsonObject.toString()));
    }

    public void subscribe() {
        getPubSubConnection(c -> {
            c.addListener(new RedisPubSub<String, String>() {
                @Override
                public void message(String channel, String message) {

                }
            });
			c.async().subscribe("auction");
        });
    }

	public String getServerName() {
        return new File(System.getProperty("user.dir")).getName();
    }
}
