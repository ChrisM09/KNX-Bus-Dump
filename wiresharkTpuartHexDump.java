/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2015, 2021 B. Malinowsky

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

    Linking this library statically or dynamically with other modules is
    making a combined work based on this library. Thus, the terms and
    conditions of the GNU General Public License cover the whole
    combination.

    As a special exception, the copyright holders of this library give you
    permission to link this library with independent modules to produce an
    executable, regardless of the license terms of these independent
    modules, and to copy and distribute the resulting executable under terms
    of your choice, provided that you also meet, for each linked independent
    module, the terms and conditions of the license of that module. An
    independent module is a module which is not derived from or based on
    this library. If you modify this library, you may extend this exception
    to your version of the library, but you are not obligated to do so. If
    you do not wish to do so, delete this exception statement from your
    version.
*/

/*
 * Wireshark Tpuart Hex Dump Author: Chris Morales
 */

import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.DataUnitBuilder;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.device.BaseKnxDevice;
import tuwien.auto.calimero.device.KnxDeviceServiceLogic;
import tuwien.auto.calimero.dptxlator.DPTXlator;
import tuwien.auto.calimero.link.KNXNetworkLinkTpuart;
import tuwien.auto.calimero.link.medium.TPSettings;
import tuwien.auto.calimero.link.NetworkLinkListener;
import tuwien.auto.calimero.CloseEvent;
import tuwien.auto.calimero.FrameEvent;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.Thread;
import java.time.Instant;

/** 
 *
 * A class that can listen onto a TPUART connection and will create a hex dump file that can be imported into Wireshark.
 *
 * To stop this program, simply press Ctrl-C.
 */
public class wiresharkTpuartHexDump extends KnxDeviceServiceLogic implements Runnable {

	/*************************************** 
	 * 
	 * Change the portId variable for your
	 * serial device port. 
	 * 
	 ****************************************/
	private static final String portId = "/dev/ttyKNX1";
	private static final String deviceName = "Wireshark Tpuart Hex Dump Device";
	private static final IndividualAddress deviceAddress = new IndividualAddress(0, 2, 15);

	// Runs the hex dump device.
	public static void main(final String[] args) throws KNXException, IOException 
	{
		new wiresharkTpuartHexDump().run();
	}


	/*
	 * Method to start producing the hex dump file. 
	 */
	@Override
	public void run()
	{
		// Make a device connected to the bus via a serial port TPUART that doesn't contain any datapoints. 
		try(final var device = new BaseKnxDevice(deviceName, this);
			final var link = new KNXNetworkLinkTpuart(portId, new TPSettings(deviceAddress), List.of()))
		{
			// Filename for hex dump
			String hexDumpFilename = "wiresharkHexDumpedTelegrams.txt";

			// Start of a new packet header for wireshark's "import from hex dump" functionality.
			String newPacketIndicator = " 000000 ";

			// Add a listener to dump the data into a formatted hex dump to be used in Wireshark.
			link.addLinkListener(new NetworkLinkListener()
			{
				/* 
				 *	Overriden method that is called when a new telegram is received on the bus. 
				 *	This will also be triggered on top of the normal listener which prints to the console.
				 */
				
				@Override
				public void indication(final FrameEvent e)
				{
					try
					{
						// Basic checking to make sure the file exists.
						File hexDumpFileHandle = new File(hexDumpFilename);
						if(!hexDumpFileHandle.exists())
						{
							hexDumpFileHandle.createNewFile();
						}

						// The file must exist here so we can write to it and append a new packet onto it.
						FileWriter writer = new FileWriter(hexDumpFileHandle, true);
						
						// Write out the time in UTC time with microsecond precision.
						// Format of the entry is <Time> <Packet indicator> <Data>
						writer.write(Instant.now().toString() + newPacketIndicator + DataUnitBuilder.toHex(e.getFrame().toByteArray(), "") + "\n");
						writer.close();
					} catch (Exception ie)
					{
						ie.printStackTrace();
					}
				}

				@Override
				public void linkClosed(final CloseEvent e) {}
			});

			// Update the device with the new link containing the listener.
			device.setDeviceLink(link);

			// Run forever until interruption.
			while(true) {}

		} catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			System.out.println("[!] " + deviceName + " is shutting down!\n");
		}
	}

	// Mandatory functions to override from interface KnxDeviceServiceLogic. Shouldn't be called.
	@Override
	public void updateDatapointValue( final Datapoint ofDp, final DPTXlator update)
	{
		System.out.println("Inside update Datapoint value for: " + ofDp);
	}

	@Override
	public DPTXlator requestDatapointValue(final Datapoint ofDp) throws KNXException{
		System.out.println("Inside request datapoint value for: " + ofDp);
		return null;
	}

}

