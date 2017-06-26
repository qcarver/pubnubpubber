# pubnubpubber

This is my thesis project for FIT, there are two parts to it, 
1. A java app (App.java) which publishes (JSON) files to my pubnub account via a swanky drag and drop interface.
2. A set of html files including files which subscribe to view the data published by the app.
  * ClustForcePubNub.html is the most current work it uses data from pubNub and displays it as per ClusterForceIV (Bostock)
  * ClusterForceIV.html this is a rip-off of M.Bostocks ClusterForceIV, I just inlined all the html and CSS to make it easy for me
  * CirclePackPubNub, Earlier work, like ClusterForcePubNub but based on Bostocks' CirclePack demo
  * ThesisChat, sanity check to make sure that pubnub stuff was working. A silly chat channel using pubnub.s pub sub.
  

Notes:
You will need to get your own keys from pubnub and paste them over publish and subscribe key strings in my pubnub.js file. You will need to serve the html pages yourself. I recommend you use Pythons SimpleServer or see if you can get your Developement IDE to serve it in debug mode or whatever. The javascript json parsing still seems a little ornery, so I included a demo drag and drop file in the resources folder which I know works (for now).
