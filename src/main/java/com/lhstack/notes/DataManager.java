package com.lhstack.notes;

import com.alibaba.fastjson2.JSON;
import com.lhstack.tools.plugins.Helper;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class DataManager {


    public static <T> T load(String filePath, Class<T> clazz) {
        try {
            File file = new File(filePath);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            byte[] bytes = Files.readAllBytes(file.toPath());
            if (bytes.length == 0) {
                return null;
            }
            return JSON.parseObject(new String(bytes, StandardCharsets.UTF_8), clazz);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> loadArray(String filePath, Class<T> clazz) {
        try {
            File file = new File(filePath);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            byte[] bytes = Files.readAllBytes(file.toPath());
            if (bytes.length == 0) {
                return null;
            }
            return JSON.parseArray(new String(bytes, StandardCharsets.UTF_8), clazz);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static File loadFile(String locationHash) {
        try {
            String path = Helper.getProjectBasePath(locationHash);
            File file = new File(path, ".idea/JTools/notes");
            if (!file.exists()) {
                file.mkdirs();
            }
            File dataFile = new File(file, "data.json");
            if (!dataFile.exists()) {
                dataFile.createNewFile();
            }
            return dataFile;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void storeData(List<Data> data, String locationHash) {
        try {
            File file = loadFile(locationHash);
            Files.write(file.toPath(), JSON.toJSONBytes(data));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    public static <T> List<T> loadData(String path, Class<T> clazz) {
        try {
            List<T> list = loadArray(path, clazz);
            if(list == null){
                list = new ArrayList<>();
            }
            return list;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Data> loadData(String locationHash) {
        try {
            File file = loadFile(locationHash);
            if (file.length() <= 0) {
                return new ArrayList<>();
            }
            byte[] bytes = Files.readAllBytes(file.toPath());
            return JSON.parseArray(new String(bytes, StandardCharsets.UTF_8), Data.class);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void store(T data, String path) {
        try {
            Files.write(new File(path).toPath(), JSON.toJSONBytes(data));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
