BitcoinAuthenticator
====================
###IMPORTANT - This project is under development.

####What Is This ?
![alt tag](https://raw.githubusercontent.com/cpacia/BitcoinAuthenticator/master/res/drawable-xhdpi/ic_icon_action_bar.png)

Bitcoin Authenticator is a P2P bitcoin wallet and android application for creating a 2 factor transaction authentication and authorization. Bitcoin Authenticator is composed of a desktop wallet and an  android application, by pairing the two together they create an P2SH bitcoin address. Any bitcoins that are transferred to that address will require the authentication and digital signature of both the desktop wallet and the android Authenticator app.

####How do i get the wallet application ? 
[Click here](https://github.com/negedzuregal/BitcoinAuthWallet/tree/basewallet) 

#### General Building requirements
1. We use Apache Maven as the build system. If you don't have it already, you can [download it](http://maven.apache.org) and follow the installation instructions. 

## Eclipse requirements
1. Download Eclipse Kepler bundle
2. Install the Eclipse ADT plugin [From Here](http://developer.android.com/sdk/installing/installing-adt.html)
3. Install the [m2e](http://www.eclipse.org/m2e/download/) plugin : 
```
help -> install new software -> add -> http://download.eclipse.org/technology/m2e/releases
```
4. Install the Maven [android Configurator](http://rgladwell.github.io/m2e-android/)

5.  Install Google Play Services
In Eclipse:

```
Android SDK Manager -> 
-Extras -> Install Google Play Services, Google Play Services for Froyo
-Anroid 4.x -> Install Google API
```

## Dependencies
###  google_play_services_froyo

1. Import the google play services for froyo project, in Eclipse: 

```
File -> Import -> existing android code into workspace -> 
search the project under <your sdk folder>extras/google_play_services_froyo/libproject/google-play-services_lib 
```
### File Chooser
1. in your android workspace 
```
git clone https://github.com/iPaulPro/aFileChooser.git
```
2. import aFileChooser to eclipse as an android project
3. in case of jar mismatch, delete support library and add bitcoinauthenticator's support library as an external jar

### JakeWharton ViewPager
1. in your android workspace 
```
git clone https://github.com/JakeWharton/Android-ViewPagerIndicator.git
```
2. import the library into eclipse as an android project
3. in case of jar mismatch, delete support library and add bitcoinauthenticator's support library as an external jar

## Import to eclipse
1. open Eclipse and import the project as an Existing maven project
2. make sure the src folder is marked as source:<Br> 
```
right click on project -> build path -> use as source folder
```
3. Right click on the BitcoinAuthenticator project: 

3.1. ```
Package Explorer -> Properties -> Android -> under Library -> Add google_play_services_froyo 
```
3.2.  ```
Package Explorer -> Properties -> Android -> under Library -> Add aFIleChooser  
```
3.3.  ```
Package Explorer -> Properties -> Android -> under Library -> Add <view pager library>   
``` 

5. Right click on the BitcoinAuthenticator project: 

```Package Explorer -> Properties -> Java Build Path -> Order and Export -> check Android Private Libraries```

## Development 
Check our future development proposals [here](https://docs.google.com/spreadsheets/d/1o5ZS_L8OppZJit46SzpauJOthI0ncWuIgmo6ZtPevOU/edit?usp=sharing)

## TODO

* The "About", and "Donate" activities found in the menu need to be created/ improved. 
* Add some basic "auto-approve" functionality for transactions below a certain amount or below a daily/weekly total, etc. Also, I would like to add the ability to auto-approve BIP70 payment requests from trusted parties (like Bitpay), but this functionality isn't a high priorty and can be added to a future release. 

## Contacts
If you have any questions feel free to contact us: 
<br><b>ctpacia@gmail.com (Chris Pacia)
<br>alonmuroch@gmail.com (Alon Muroch)</b>
