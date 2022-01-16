# KNX-Raspberry-Pi-Hat-Usage
A detailed walkthrough on how to use a KNX Raspberry Pi HAT to read the bus within a KNX network. 

*This is a condensed version of Tijl Schepens' guide to using this HAT with a Raspberry Pi. Original Link: https://hackaday.io/project/171850/instructions*


## Installing Raspberry Pi OS
Using Raspberry Pi Imager, put the 32-bit version of the Raspberry Pi OS onto the SD card of the pi.

*Original Home Screen*

<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Pi Imager Software Home.PNG">

*OS Selected*

<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Pi Imager OS Selected.PNG">

Wait for this installation to be done and then put the SD card back into the Pi.
Power on the Pi to begin the initial installation steps for the OS.

## Connecting to the Pi
If you'd like to do all these steps on the pi itself using a monitor, keyboard, and mouse, then skip this section to "Installing KNXD". 
If, however, you'd like to SSH into the pi, then continue reading.

To allow SSH connections, you must enable the SSH server in the pi through raspi-config.
- You also want to make sure that the pi has an IP address, so either connect to the internet through Ethernet or Wi-Fi.

### Activating the SSH server
In a new terminal, execute "sudo raspi-config" and you will be prompted with a configuration menu. 
- Choose "Interface Options".
<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Raspi-Config Home Interface Highlight.PNG">

- Choose "SSH"
<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Raspi-Config Interface SSH Highlight.PNG">

- Choose "Yes" to wanting the SSH server to be enabled.
- Finally, you can choose finish and the SSH server is enabled. To confirm this, you can run "sudo systemctl status ssh" on the command line to see if the service is running.

- Before disconnecting your peripherals, it would be a good idea to test if the server is actually running. Take note of the IP address of the Pi and then try to SSH into it from an external device on the same subnet.

### SSH into the Pi
In Windows, to SSH into the Pi, open a new instance of Command Prompt and then the format of the command is "ssh &lt;User&gt;@&lt;IP Address&gt;".

So, in this case, the command for the newly installed Raspberry Pi OS with default information is "pi@&lt;IP Address&gt;".

Using 127.0.0.1 as an example, a valid command would be "ssh pi@127.0.0.1".

The default password is "raspberry". 


## Installing KNXD
We need to install the Debian branch of the KNXD repository from Github and build it.
- *Note: This will take some time to finish. Roughly 20 minutes. Also, keep an eye on the progress as there will be input required during the last stage of building.*

Run these commands from the screenshot below.

<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Installing and Building KNXD.PNG">

**Once complete, restart the Pi.**


## Configuring KNXD
### Disabling the login shell over serial
We must disable the login shell to allow the UART peripheral to be used to communicate with the HAT.

Execute "sudo raspi-config"
- Select "Interface Options"
<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Raspi-Config Home Interface Highlight.PNG">
- Select "Serial Port"
<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Raspi-Config Interface Serial Port Highlight.PNG">
- Answer "No" to the question regarding a login shell being accessible over serial
<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Raspi-Config Serial Port Login Shell No Highlight.PNG">
- Answer "Yes" to the question regarding the serial port hardware being enabled.
<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Raspi-Config Serial Port Hardware Yes Highlight.PNG">
- Finally, choose Finish and then reboot the Pi to see the effects.
<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Raspi-Config Serial Port Summary.PNG">
<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Raspi-Config Serial Port Reboot Prompt.PNG">

Upon reboot, we need to add a rule for KNXD to be allowed to use UART to communicate with the Pi. So, we need to add a UDEV rule.

### Creating a new KNXD rule 
Follow the steps in the image below for commands.

<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Configure KNXD Rules Commands.PNG">
- Write your version of the finished line into the /etc/udev/rules.d/80-knxd.rules file.

To see this new rule in action, we must reboot the device once again.

Then, run this commmand.

<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Configure KNXD ttyKNX1 grep.PNG">

You should see the new serial interface pop up.

### Disabling Bluetooth and the hciuart service
Next, we need to disable Bluteooth because the UART interface is used for it.

Follow the steps in the image below.

<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Configure KNXD boot config and hciuart disable.PNG">

Now, we need to stop the KNXD service that starts after installation using default flags.
- Run "sudo systemctl stop knxd.service"
- Run "sudo systemctl stop knxd.socket"

Next, we need to update those flags in the configuration file /etc/knxd.conf

<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Configure KNXD config file.PNG">

Flag Explanation:
- -f9 means setting the error level to level 9
- -t1023 means maximum debugging output
- -u means to listen at Unix domain socket FILE
- --eibaddr means to set our EIB address to the value specified.
- -E means to assign clients these EIB addresses specified. The format X:Y means starting at EIB address X to EIB address Y
  - *Note, don't have addresses in range X to Y that are already being used by existing devices. This excludes the EIB address from -E.*
- -n means this is the name of the EIBnet/IP server that is listening.
- -D means allow the server to answer discovery and description requests
- -T means enable Tunneling in the EIBnet/IP server.
- -R means enable routing in the EIBnet/IP server.
- -S means starting an EIBnet/IP multicast server.
- --layer2 is specifying what interface is being used.


For a full list of flags and more detailed instructions: https://github.com/knxd/knxd/wiki/Command-line-parameters


Once this is done, KXND is ready to run.
Before we can start KNXD, ensure that the Pi is connected to the Local KNX network.


### Connecting the HAT to a local KNX network
If the HAT doesn't fit nicely onto the Pi, then manual connections using Dupont wires can be done easily. Shut down the KNX network and the pi to avoid any accidents or shocks.

Steps to connect are shown below.

Materials needed:
- 1 Raspberry Pi
- 1 KNX Raspberry Pi HAT
- 4 Dupont wires 
<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Local KNX Setup Materials.jpg">

Then connect the Dupont Wires to the pi

You will need to put connectors into pins 1,6,8, and 10. These are for power, ground and UART communication respectively.

<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Pi Pin Diagram.PNG">

*Note: Pins 39 and 40 will be closest to the USB connections on the Pi.*

<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Local KNX Setup Pi Pins.jpg">

Now, connect the pi wires to the Pi HAT.

<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Local KNX Setup Pi Hat Connected.jpg">
  
Next, connect the KNX connector to the local KNX network.

<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Local KNX Setup KNX Connector.jpg">

Finally, connect the KNX connector to the HAT and then power on the Pi and the local KNX network to power the HAT.

<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Local KNX Setup Power Connected.JPG">

## Running KNXD 
To start the KNXD service, run these commands.

<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Starting KNXD Service Commands.PNG">

Once this happens, you can check the status of the service by "sudo systemctl status knxd". This is sample output from the command.

<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Starting KNXD Status Check.PNG">

*Note: If service has an error status when first running the service, try restarting the Pi.*


To ensure full functionality, we can run the bus monitor command to see information coming across the bus.

Run this command

<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Starting KNXD Bus Monitor Command.PNG">

It may take a while depending on what you have in the local KNX Network. For instance, this setup had a presence detector and a temperature sensor. But, you should be able to see all traffic across the bus.

<img src="/KNX-Raspberry-Pi-Hat-Usage/Images/Starting KNXD Bus Monitor Output.PNG">
