package com.hao.redistest.redistemplatetest.service.impl;

import com.alibaba.fastjson.JSON;
import com.hao.redistest.redistemplatetest.service.TestService;
import org.apache.commons.io.FileUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hao
 * @date 2020/4/27
 */
@Service
public class TestServiceImpl implements TestService {

    @Resource(name = "macResourceRedisTemplate")
    private RedisTemplate<String, String> macResourceRedisTemplate;

    /**
     * 读取文件并写入redis
     * @param path 要写入的文件
     */
    @Override
    public void writeToRedis(String path) {
        try {
            //通过参数path读取要写入的文件
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            String[] resourceArr = null;
            AtomicInteger inc = new AtomicInteger();
            //分批执行
            int batchSize = 20000;
            List<Map<String,String>> batch = new ArrayList<>(batchSize);
            while ((line = br.readLine()) != null) {
                //这里是我读取写入的规则，大家可以按自己的规则来
                resourceArr = line.split(",");
                String key = resourceArr[0] + "_" + resourceArr[4];
                Map<String,Object> element = new HashMap<>(1);
                element.put("param1", resourceArr[1]);
                element.put("param2", resourceArr[2]);
                element.put("param3", resourceArr[7]);
                Map<String,String> kv = new HashMap<>();
                kv.put(key, JSON.toJSONString(element));
                batch.add(kv)   ;
                if(batch.size() % batchSize == 0 ){
                    List<Map<String,String>> toSaveBatch = new ArrayList<>(batch);
                    try{
                        //到达设定的batchSize进行pipeline写入
                        batchSave(toSaveBatch,inc);
                        batch = new ArrayList<>(batchSize);
                    }catch (Exception ex ){
                        for(Map<String,String> m :toSaveBatch){
                            for(Map.Entry<String,String> entry : m.entrySet()){
                                FileUtils.writeStringToFile(new File("tmp/mac_error.txt"),entry.getKey() + "@" + entry.getValue() + "\n", StandardCharsets.UTF_8,true);
                            }
                        }
                        throw new RuntimeException(ex);
                    }
                }
            }
            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 分批用pipeline写入redis
     * @param batch
     * @param inc
     */
    private void batchSave(List<Map<String,String>> batch,AtomicInteger inc ){
        //调用redisTemplate的executePipelined  重新内部的doInRedis方法，这里用lambda语法写的 隐藏掉了
        macResourceRedisTemplate.executePipelined((RedisCallback<Object>) redisConnection -> {
            //打开pipeline管道
            redisConnection.openPipeline();
            for(Map<String,String> e : batch){
                for(Map.Entry<String,String> entry : e.entrySet() ){
                    try {
                        //遍历集合数据，通过pipeline推入redis
                        redisConnection.lPush(entry.getKey().getBytes(),entry.getValue().getBytes());
                    }catch (Exception ex){
                        System.out.println("key:" + entry.getKey() + ",value: " + entry.getValue());
                        throw new RuntimeException(ex);
                    }
                    System.out.println(inc.incrementAndGet());
                }
            }
            return null;
        });
    }
}
