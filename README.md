BitcoinAuthenticator
====================

<<<<<<< HEAD
under development
=======
We use Apache Maven as the build system. If you don't have it already, you can [download it](http://maven.apache.org) and follow the installation instructions. 

Get yourself a copy of Bitcoinj 0.12 which can be found in the master branch.

```
git clone https://github.com/bitcoinj/bitcoinj.git
```

You need to build it with Maven. Cd into the bitcoinj directory and type

```
mvn clean package
```

Add the bitcoinj-0.12-SNAPSHOT.jar file found in /bitcoinj/core/target to your build path. 

That should be all you need to do to get the mobile app up and running. 

The test wallet included in this repo (also using Maven) has a few more dependencies:


[Bitcoinj](https://code.google.com/p/bitcoinj/) again.
[ZXing ("Zebra Crossing")](https://code.google.com/p/zxing/) for the QR code.
[weupnp](https://code.google.com/p/weupnp/) for universal plug and play.


This app is maybe 50% of the way to an alpha release at the moment. Here is a TODO list (in no particular order). 

* Add support for multi-in transactions and finialize the payload format for sending transactions to the authenticator.
* Connect to multiple wallets. Right now the Authenticator can pair with multiple wallets but it isn't set up to receive and sign transactions from any wallet other than the first one it paired with. 
* We will likely be using Google Cloud Messaging (GCM) to facilitate the P2P connection between the wallets and the Authenticator. Support for this still needs to be added. And a process for handling connections without GCM (when disabled in the settings) needs to be put in place. The Authenticator can still connect using the last known IP (assuming it's still valid). Or otherwise just repair the device. Repairing functionality needs to be added.
* The "How it works" activity is more or less just a placeholder. It needs to be made pretty. I'm thinking maybe some clipart explaining how it works where the user can swipe to view the next image.
* The "Pair wallet" activity is very ugly. It needs to be made pretty as well.
* The "Settings", "About", and "Donate" activities found in the menu need to be created. 
* Add some basic "auto-approve" functionality for transactions below a certain amount or below a daily/weekly total, etc. Also, I would like to add the ability to auto-approve BIP70 payment requests from trusted parties (like Bitpay), but this functionality isn't a high priorty and can be added to a future release. 

Finally, Bitcoinj has a fully functioning demo wallet in the wallettemplate directory. I would like to eventually migrate the test wallet over to that and use it for what would essentially be a reference implementation for plugin support. 

If you have any questions feel free to contact me: ctpacia@gmail.com
>>>>>>> d1e688f... updated readme

contact me with questions

ctpacia@gmail.com
