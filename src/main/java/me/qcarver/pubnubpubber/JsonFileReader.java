/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.qcarver.pubnubpubber;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
    JSONArray testData = null;
    boolean haveParsed;
    static final private String ICSHT_DAS_SHAMPLE_FILE_NOMEN
            = "/Users/Quinn Carver/Downloads/MOCK_DATA.json";

    JsonFileReader(String filename) {
        this.filename = filename;
    }

    public static void main(String[] args) {
        JsonFileReader _this = new JsonFileReader(ICSHT_DAS_SHAMPLE_FILE_NOMEN);
        _this.parse();
    }
    
    public boolean isTestRunFile(){
        if (!haveParsed){
            parse();
        }
        return (testData == null);
    }

    @SuppressWarnings("unchecked")
    private void parse() {
        JSONParser parser = new JSONParser();

        try {

            Object obj = parser.parse(new FileReader(filename));

            JSONObject jsonObject = (JSONObject) obj;

//            String name = (String) jsonObject.get("Name");
//            String author = (String) jsonObject.get("Author");
//            JSONArray companyList = (JSONArray) jsonObject.get("Company List");

            BiConsumer<Object, Object> putInMap
                    = new BiConsumer<Object, Object>() {
                @Override
                public void accept(Object t, Object u) {
                    objects.put(t, u);
                }
            };

            jsonObject.forEach(putInMap);

            if (objects.containsKey("test_data")) {
                testData = (JSONArray) objects.get("test_data");
            }

//            System.out.println("Name: " + name);
//            System.out.println("Author: " + author);
//            System.out.println("\nCompany List:");
            Iterator<JSONObject> iterator = testData.iterator();
            while (iterator.hasNext()) {
                System.out.println(iterator.next());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        this.haveParsed = true;
    }
}
