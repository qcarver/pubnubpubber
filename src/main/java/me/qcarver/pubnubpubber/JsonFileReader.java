/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.qcarver.pubnubpubber;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiConsumer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author Quinn Carver
 */
public class JsonFileReader {

    String filename;
    Map objects = new HashMap<>();
    boolean haveParsed;
    static final private String ICSHT_DAS_SHAMPLE_FILE_NOMEN
            = "/Users/Quinn Carver/Downloads/MOCK_DATA.json";
    JSONObject jsonObject = null;
    Queue dataQ = null;

    JsonFileReader(String filename) {
        this.filename = filename;
    }

    public static void main(String[] args) {
        JsonFileReader _this = new JsonFileReader(ICSHT_DAS_SHAMPLE_FILE_NOMEN);
        _this.parse();
    }

    public boolean isTestRunFile() {
        if (!haveParsed) {
            parse();
        }
        return (dataQ != null);
    }
    
    public String getNextData(){
        String nextData = null;
        if (dataQ.size()>0){
            nextData = dataQ.remove().toString();
        }
        return nextData;
    }

    public String getFileAsString() {
        return jsonObject.toJSONString();
    }

    
    
    @SuppressWarnings("unchecked")
    private void parse() {
        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader(filename));
            jsonObject = (JSONObject) obj;

//            String name = (String) jsonObject.get("Name");
            BiConsumer<Object, Object> putInMap
                    = new BiConsumer<Object, Object>() {
                @Override
                public void accept(Object t, Object u) {
                    objects.put(t, u);
                }
            };
            jsonObject.forEach(putInMap);

            if (objects.containsKey("test_data")) {
                dataQ = new LinkedList<>();
                dataQ.addAll((JSONArray) objects.get("test_data"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.haveParsed = true;
    }
}
