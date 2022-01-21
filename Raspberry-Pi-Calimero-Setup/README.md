# Raspberry Pi Calimero Setup
A short walkthrough to describe how to set up the Calimero-tools suite to be used with a Raspberry Pi 3 

*Note: This walkthrough will show how to use this suite with a Raspberry Pi HAT and TPUART. Setup for the HAT is [here](/KNX-Raspberry-Pi-Hat-Usage/README.md)*

## Download the calimero-project suite

Download the individual repositories that you will need. For our use case, we can use calimero-tools, calimero-rxtx, calimero-core, serial-native and finally the introduction repository.

Github page for overall suite of tools: https://github.com/calimero-project

Commands to download the suite:

```
  git clone https://github.com/calimero-project/calimero-tools.git
  git clone https://github.com/calimero-project/calimero-rxtx.git
  git clone https://github.com/calimero-project/calimero-core.git
  git clone https://github.com/calimero-project/calimero-device.git
  git clone https://github.com/calimero-project/serial-native.git
  git clone https://github.com/calimero-project/introduction.git
```

## Update Java to the latest version

Run these commands to update.
  
```
  sudo apt update
  sudo apt install default-jdk
```
  
## Install Maven to build the projects
We need to build some of these projects to run them and have some compiled libaries for use in other tools. The simplest way is to use the maven tool.
  
Steps are taken and modified from a guide which can be found directly [here](https://xianic.net/2015/02/21/installing-maven-on-the-raspberry-pi/).
  
1) Download the corresponding binary tar.gz file from the maven website. Replace the "3.2.5" with the latest version on the [maven website](http://maven.apache.org/download.cgi).
    - For example, the latest version (at the time of the guide) is *3.8.1*. So, the command would be 
    ```
      wget http://www.mirrorservice.org/sites/ftp.apache.org/maven/maven-3/3.8.1/binaries/apache-maven-3.8.1-bin.tar.gz
    ```
2) Extract the archive to the /opt directory. As always, replace the version number (3.8.1) with the latest.
    ```
      cd /opt && sudo tar -xzvf /path/to/apache-maven-3.8.1-bin.tar.gz
    ```
3) Now we need to let the shell know where to find maven. Modify the system profile settings to make it available for all users.
    ```
      sudoedit /etc/profile.d/maven.sh
    ```
    This will create a new file in which we will specify the location of the maven installation.
  
    Enter these lines into the *maven.sh* file.
    ```
      export M2_HOME=/opt/apache-maven-3.8.1
      export "PATH=$PATH:$M2_HOME/bin"
    ```
  
    Now save and quit the editor.
  
4) If everything is done correctly, you should be able to now restart the Pi to make the script take effect and see that maven is working now.
    - You can see if this is true by running
        ```
          mvn -version
        ```
  
  
  
## Build the serial-native repository
  We need to build this repository because it will create some library functions that will be used by the other tools in the suite.
  
  However, we need to update the file **pom.xml** to build according to our newly updated Java version and location as well as our Maven version.
  
  
  1) To find where Java is located, run
  
      ```
        java -XshowSettings:properties -version 2>&1 > /dev/null | grep 'java.home' 
      ```
  2) To find the maven plugin version
  
      ```
        mvn -Dplugin=com.github.maven-nar:nar-maven-plugin help:describe
      ```

  
### Update the pom.xml file
  This file contains settings for Linux and MacOS systems. But they need to be adjusted to the java version and location of where Java is located in. To be safe, I also updated the nar plugin version. 
  
  1) Update the *java.home* property located within the *properties* block before the *build* block with the information gathered from before.
  
  2) Update the *version* property located within the *plugin* block with the information gathered from before for the nar plugin version.
  
### Compile this library  
To build with Maven, all you need to do is change into the directory in which serial-native is located in. Then simply run
  
  ```
    mvn compile
  ```  

This should not give you any compilation errors. If it does, refer to the _Troubleshooting_ section. You won't be able to continue without getting past this step. It will take a few minutes as it will download other resources from the internet.
  
The location of the compiled library will be under target/nar/*gpp-jni
  
  - Continue moving down through this directory until you find a *.so file
  
Keep note of the .so file location.
  
## Place compiled library for future use
  
  Now with this library compiled, we need to place this in our java installation directory.
  
  - To find a suitable location, we need to find where the java library path is located. We can do this by running the *jlp_discovery.java* file with
  
    ```
      java jlp_discovery.java
    ```
  
  This will print out a list of locations where various java libraries can be placed and then used. In my instance, I could simply use the */usr/lib* directory. Each qualifying path is separated by a colon, you can use whichever you'd like. 
  
  Copy the resulting .so file into the directory of your choice.

  
## Avoiding timeout issues with Properties scan
  The communication medium can be slow when using Calimero and it will lead to partial frames being discarded and then possible crashes. To resolve this issue, we need to increase the delay between bytes to avoid this. 
  
  This can be done by modifying the *build.gradle* file within the *calimero-tools* repository. 
  
  All we need to do is add this line in the *run* section of *build.gradle* at line 33.
  
  ```
    systemProperty "calimero.serial.tpuart.maxInterByteDelay", findProperty("calimero.serial.tpuart.maxInterByteDelay")
  ```
  
## Running a property scan
  This tool will help scan for all the properties within a device on the KNX bus. 
  
  For example, suppose we have a device at EIB address 1.1.9. We can read all the properties with some basic information about what those properties are.
  
  From the Pi HAT setup guide, we know that it will use /dev/ttyKNX1 as the communication TPUART interface.
  
  The format of the command is as follows:
    - ./gradlew &lt;Additional properties&gt; run --args "&lt;command&gt; &lt;arguments for the previous command&gt;"
 
  We can use the "-r &lt;EIB address&gt;" flag to establish a remote connection to the device at the specified EIB address. 
  Then we can assign this client an EIB address for temporary use with the "-k &lt;EIB address&gt;" flag. 
  Finally, we need to specify the communication medium using the "--tpuart &lt;Interface&gt;"
  
  So, we can run this with
  
  ```
    ./gradlew -Pcalimero.serial.tpuart.maxInterByteDelay=60000 run --args "properties -r 1.1.9 -k 1.1.49 --tpuart /dev/ttyKNX1"
  ```
  
  We will be given a shell from the tool itself. Now, to run the scan, we can simply run
  ```
    scan all
  ```
  
  And that should be it for the properties scan. 


# Troubleshooting

## Calimero-Suite

### MVN compilation errors 
A version of the calimero-project that does in fact work with a Raspberry Pi 3 is 2.5. 

In order to revert back, you'll need to utilize Git's _checkout_ capability. You will need the correct commit hash to change the state of the repository. You can find the tags under the _Releases_ section.

_Note: The hashes are the same whether or not you're looking at Releases or Tags_

For example (highlighted in yellow):

_Introduction repository tag location:_

<img src="/Raspberry-Pi-Calimero-Setup/Images/Introduction-Releases-Location.png">

_Calimero Tools repository tag location:_

<img src="/Raspberry-Pi-Calimero-Setup/Images/Tools-Releases-Location.png">

You will need the hashes of ALL the repos that you downloaded:
1. Calimero-core 
2. Calimero-tools
3. Calimero-rxtx
4. Calimero-device
5. Introduction
6. Serial-native


#### Reverting back to a previous version

Now that you have the correct hashes. You can begin reverting. Just
be aware that if you had changed the _pom.xml_ file in the _serial-native_ directory, then you will not be able to simply revert back. You will need to either undo the changes or you can delete the directory as a whole, then clone again, and then apply the checkout command.

#### Format of the checkout command

```
git checkout <Hash of Commit>
```
Simply just change into the corresponding directories of the different projects and run this command with the corresponding hashes.

Once this is done, then you can rerun ``` mvn compile ``` 
