package com.lhstack.notes;

public class Config {

    private static Config config;

    public static Config getInstance(){
        if(config == null){
            config = DataManager.load(System.getProperty("user.home") + "/.jtools/notes/config.json", Config.class);
            if(config == null){
                config = new Config();
            }
        }
        return config;
    }

    public void store(){
        DataManager.store(this,System.getProperty("user.home") + "/.jtools/notes/config.json");
    }

    /**
     * 是否作用于全局
     */
    private boolean isGlobal = false;

    public boolean isGlobal() {
        return isGlobal;
    }

    public Config setGlobal(boolean global) {
        isGlobal = global;
        return this;
    }
}
