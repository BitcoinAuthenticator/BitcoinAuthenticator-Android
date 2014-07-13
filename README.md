BitcoinAuthenticator
====================
###IMPORTANT - This project is under development.

####What Is This ?
![alt tag](https://raw.githubusercontent.com/cpacia/BitcoinAuthenticator/master/res/drawable-xhdpi/ic_icon_action_bar.png)

Bitcoin Authenticator is a P2P bitcoin wallet and android application for creating a 2 factor transaction authentication and authorization. Bitcoin Authenticator is composed of a desktop wallet and an  android application, by pairing the two together they create an P2SH bitcoin address. Any bitcoins that are transferred to that address will require the authentication and digital signature of both the desktop wallet and the android Authenticator app.

####How do i get the wallet application ? 
[Click here](https://github.com/negedzuregal/BitcoinAuthWallet/tree/basewallet) 

#### Building requirements
1. We use Apache Maven as the build system. If you don't have it already, you can [download it](http://maven.apache.org) and follow the installation instructions. 

####Building with Maven
1. Clone the project:
 ```
 $ git clone https://github.com/cpacia/BitcoinAuthenticator.git
 ```
2. 
 ```
 $ cd BitcoinAuthenticator
 ```
3. build: 
 ```
$mvn clean package
 ```

#### Importing BitcoinAuthenticator Into Eclipse 
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
   
4. Right click on the BitcoinAuthenticator project:

 ```
Package Explorer -> Properties -> Android -> under Library -> Add google_play_services_froyo 
```

5. Rick click on the BitcoinAuthenticator project:
	
 ```Package Explorer -> Properties -> Java Build Path -> Order and Export -> check Android Private Libraries and move it (by clicking up) to the top of the list```

## TODO

* The "How it works" activity is more or less just a placeholder. It needs to be made pretty. I'm thinking maybe some clipart explaining how it works where the user can swipe to view the next image.
* The "Pair wallet" activity is very ugly. It needs to be made pretty as well.
* The "About", and "Donate" activities found in the menu need to be created/ improved. 
* Add some basic "auto-approve" functionality for transactions below a certain amount or below a daily/weekly total, etc. Also, I would like to add the ability to auto-approve BIP70 payment requests from trusted parties (like Bitpay), but this functionality isn't a high priorty and can be added to a future release. 

If you have any questions feel free to contact us: ctpacia@gmail.com (Chris Pacia), alonmuroch@gmail.com (Alon Muroch).
