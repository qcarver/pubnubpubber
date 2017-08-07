/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.qcarver.pubnubpubber;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.qcarver.pubnubpubber.DropFrame.DropCallback;

/**
 *
 * @author Quinn Carver
 */
public class App {

    File tempDir = null;
    PNConfiguration pnConfiguration = null;
    PubNub pubnub = null;
    String channel;
    String pubKey;
    String subKey;

    DropFrame gui = null;

    public static void main(String[] args) {
        //get Configuration.get().uration
        //new ArgsParser(args);
        new App();
    }

    /**
     *
     * @param Configuration.get().
     */
    public App() {
        init();
    }

    private void init() {
        initPubNub();
        String labelContents
                = "<html><br><center><H1>Drag json file</H1><br>(to publish)<br>"
                + "<H1>[HERE]</H1><br>Channel " + channel + "<br><small>pubKey: "
                + pubKey + "</center></small></html>";
        String title = "PubNubPubber";
        gui = new DropFrame(title, labelContents,
                new DropCallback() {
            @Override
            public void onDrop(String nameOfFileDropped) {
                publishFile(nameOfFileDropped);
            }
        });
    }

    private void initPubNub() {
        String keyFilePath
                = "src\\main\\java\\me\\qcarver\\pubnubpubber\\html\\pubnubkeys.js";
        String keyFileContents = "If you don't have your own pubnub keys"
                + " it's easy and free to get them at pubnub.com";
        try {
            keyFileContents
                    = new String(Files.readAllBytes(Paths.get(keyFilePath)));
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE,
                    "Couldn't find private pubnub keys", ex);
        }

        int startOfPubKey = keyFileContents.indexOf("pub-c");
        int startOfSubKey = keyFileContents.indexOf("sub-c");
        int startOfChannel = keyFileContents.indexOf("channel");
        startOfChannel = keyFileContents.indexOf("'", startOfChannel) + 1;
        int endOfChannel = keyFileContents.indexOf("'", startOfChannel + 1);

        final int keyLength = 42;

        channel = keyFileContents.substring(startOfChannel, endOfChannel);
        pubKey = keyFileContents.substring(startOfPubKey,
                startOfPubKey + keyLength);
        subKey = keyFileContents.substring(startOfSubKey,
                startOfSubKey + keyLength);

        pnConfiguration = new PNConfiguration();
        pnConfiguration.setPublishKey(pubKey);
        pnConfiguration.setSubscribeKey(subKey);
        //pnConfiguration.setSecure(false);
        pubnub = new PubNub(pnConfiguration);
    }

    private void publishFile(String fileName) {
        JsonFileReader reader = new JsonFileReader(fileName);

        if (!reader.isTestRunFile()) {
            publishString(reader.getFileAsString());
        } else {
            System.out.println("test data was drag and dropped");
            String nextData = null; 
            
            //this will change.. JS will poll
            while ((nextData = reader.getNextData())!=null) {
                publishString(nextData);
                try {

                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    //do stuff
                }
                //allow for pause request
            }
        }
    }

    /*
    PublishFile is called
        if File is a single file: publish the file JSONOBject.toJSONString
        If File has a test_data array load the array
            start some timer which publishes each array element over time
    
    Order of events:
    JS:Loading page causes it to request an example (by ordinal #)
    J: An example is published
    JS:Page requests test_data
    J: Test Data sent on a timer
    JS:Page requests pause
    J: Timer pauses
    JS:Page requests continue
    J: Timer restarts
    J: Publishes end of test message
    
    Need to story board these web pages
    
    
    
     */
    private void publishString(String fileContents) {
        pubnub.publish()
                .channel("thesis")
                .message(fileContents)
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        // Check whether request successfully completed or not.
                        if (!status.isError()) {
                            System.out.println("message successfully published");
                        } else {
                            System.err.println("Message failed to publish");
                            gui.showError("<html><center><br><H1>Failed</H1>"
                                    + "to publish<br>"
                                    + status.getErrorData()
                                    + "</center></html>");
                            // Handle message publish error. Check 'category' property to find out possible issue
                            // because of which request did fail.
                            //
                            // Request can be resent using: [status retry];
                        }
                    }
                });
    }
}
