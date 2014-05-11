BitcoinAuthenticator
====================

This project is under developement. If you would like to contribute here's how you can get started:

We use Apache Maven as the build system. If you don't have it already, you can [download it](http://maven.apache.org) and follow the installation instructions. 

We are currently working with the HDW-Alpha version of Bitcoinj which can be found in the HDW-Alpha branch.

```
git clone -b hdw-alpha https://github.com/bitcoinj/bitcoinj.git
```

You need to build it with Maven. Cd into the bitcoinj directory and type

```
mvn clean package
```

Add the bitcoinj-0.12-SNAPSHOT.jar file found in /bitcoinj/core/target to your build path. 

That should be all you need to do to get the mobile app up and running. 

## Test Wallet

The test wallet included in this repo (also using Maven) has a few more dependencies:


[Bitcoinj](https://code.google.com/p/bitcoinj/) again.

[ZXing ("Zebra Crossing")](https://code.google.com/p/zxing/) for the QR code.

[weupnp](https://code.google.com/p/weupnp/) for universal plug and play.

## Importing BitcoinAuthenticator Into Eclipse 
1. Get a copy of the project and its submoodules 
   ```
   git clone --recursive https://github.com/cpacia/BitcoinAuthenticator.git
   ```
2. In Eclipse:
  ```
  File -> Import -> existing maven project
   ```
3. Install Google Play Services
  In Eclipse:
  ```
  Android SDK Manager -> 
  -Extras -> Install Google Play Services, Google Play Services for Froyo
  -Anroid 4.x -> Install Google API
  ```
4. Import the google play services for froyo project, in Eclipse: 
   ```
  File -> Import -> existing android code into workspace -> 
  search the project under <your sdk folder>extras/google_play_services_froyo/libproject/google-play-services_lib 
   ```
   
4. ``` Right click on the BitcoinAuthenticator project in Package Explorer -> Properties -> Android -> under Library -> Add      google_play_services_froyo ```

5. ``` Rick click on the BitcoinAuthenticator project in Package Explorer -> Properties -> Java Build Path -> Order and Export -> check Android Private Libraries and move it (by clicking up) to the top of the list


## TODO

This app is maybe 50% of the way to an alpha release at the moment. Here is a TODO list (in no particular order). 

* Test/debug GCM integration with when making transactions. 
* Connect to multiple wallets. Right now the Authenticator can pair with multiple wallets but it isn't set up to receive and sign transactions from any wallet other than the first one it paired with. 
* The "How it works" activity is more or less just a placeholder. It needs to be made pretty. I'm thinking maybe some clipart explaining how it works where the user can swipe to view the next image.
* The "Pair wallet" activity is very ugly. It needs to be made pretty as well.
* The "Settings", "About", and "Donate" activities found in the menu need to be created. 
* Add some basic "auto-approve" functionality for transactions below a certain amount or below a daily/weekly total, etc. Also, I would like to add the ability to auto-approve BIP70 payment requests from trusted parties (like Bitpay), but this functionality isn't a high priorty and can be added to a future release. 

Finally, Bitcoinj has a fully functioning demo wallet in the wallettemplate directory. I would like to eventually migrate the test wallet over to that and use it for what would essentially be a reference implementation for plugin support. 

Alon Muroch (alonmuroch@gmail.com) has started work on the wallettemplate:
https://github.com/negedzuregal/BitcoinAuthWallet

If you have any questions feel free to contact me: ctpacia@gmail.com
