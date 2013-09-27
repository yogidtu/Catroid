/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2013 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://developer.catrobat.org/license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.bluetoothdummyserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.bluetooth.UUID;
import javax.microedition.io.StreamConnection;

public class BTConnectionHandler implements Runnable {
	private StreamConnection connection = null;
	private InputStream inputStream = null;
	private OutputStream outputStream = null;
	private UUID uuid = null;
	private int readedBytes;
	private byte[] readBuffer = new byte[1024];

	public static final String SERVERDUMMYMULTIPLAYER = "multiplayer";
	public static final String SETASCLIENT = "setasclient";
	public static final String SETASSERVER = "setasserver";

	public BTConnectionHandler(StreamConnection connection) {
		this.connection = connection;
	}

	@Override
	public void run() {
		try {
			inputStream = connection.openInputStream();
			readedBytes = inputStream.read(readBuffer);
			String[] receivedMessage = (new String(readBuffer, 0, readedBytes, "ASCII")).split(";");

			if (receivedMessage[0].equals(SERVERDUMMYMULTIPLAYER)) {
				uuid = new UUID(receivedMessage[2], false);

				if (receivedMessage[1].equals(SETASCLIENT)) {

				} else if (receivedMessage[1].equals(SETASSERVER)) {
					multiplayerDummyServer();
				} else {
					System.err.println("Incorrect message for Multiplayer-Server");
					return;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void multiplayerDummyServer() {

	}

}
