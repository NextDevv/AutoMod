package it.unilix.automod.redis.redisdata;


public enum RedisKeys {


    ALERT("bw-alert");


    private final String keyName;

    /**
     * @param keyName the name of the key
     */
    RedisKeys(final String keyName) {
        this.keyName = keyName;
    }

    /**
     * Use {@link #toString} instead of this method
     *
     * @return the name of the key
     */
    public String getKey() {
        return keyName;
    }

}
