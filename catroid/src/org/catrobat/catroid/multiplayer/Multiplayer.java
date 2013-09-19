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
package org.catrobat.catroid.multiplayer;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.formulaeditor.UserVariableShared;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

public class Multiplayer {
	public static final String SHARED_VARIABLE_NAME = "shared_variable_name";
	public static final String SHARED_VARIABLE_VALUE = "shared_variable_value";
	public static final String MAGIC_PACKET = "AEIOU";
	public static final int STATE_CONNECTED = 1001;

	private static Multiplayer instance = null;
	private MultiplayerBtManager multiplayerBtManager = null;
	private Object lock = new Object();
	private static Handler btHandler;
	private static boolean initialized = false;
	private Integer randomNumber = 0;

	private Multiplayer() {
	}

	public static Multiplayer getInstance() {
		if (instance == null) {
			instance = new Multiplayer();
		}
		return instance;
	}

	public void resetMultiplayer() {
		randomNumber = 0;
		initialized = false;
		multiplayerBtManager = null;
	}

	public boolean createBtManager(String mac_address) {
		if (!mac_address.equals("connected")) {
			synchronized (lock) {
				if (!initialized) {
					if (multiplayerBtManager == null) {
						multiplayerBtManager = new MultiplayerBtManager();
						btHandler = multiplayerBtManager.getHandler();
						initialized = true;
					}
				}
			}

			if (!handleDoubleConnectionClient(multiplayerBtManager.connectToMACAddress(mac_address))) {
				return false;
			}
			multiplayerBtManager.startReceiverThread();
			return true;
		}

		return false;
	}

	public boolean createBtManager(BluetoothSocket btSocket) {
		if (handleDoubleConnectionServer(btSocket)) {
			synchronized (lock) {
				if (multiplayerBtManager == null) {
					multiplayerBtManager = new MultiplayerBtManager();
					btHandler = multiplayerBtManager.getHandler();
					initialized = true;
				}
			}

			multiplayerBtManager.createInputOutputStreams(btSocket);
			return true;
		}

		return false;
	}

	public boolean handleDoubleConnectionClient(BluetoothSocket btSocket) {
		try {
			if (btSocket == null) {
				return false;
			}

			OutputStream btOutStream;
			synchronized (randomNumber) {
				if (randomNumber < 0) {
					Log.d("Multiplayer", "------- CLIENT: Random Number: -1 ------");
					return false;
				}
				btOutStream = btSocket.getOutputStream();
				Random generator = new Random();
				randomNumber = generator.nextInt(Integer.MAX_VALUE - 1) + 1;
				randomNumber = 4899284;
				Log.d("Multiplayer", "------- CLIENT: Random Number: " + randomNumber + " ------");
			}
			byte[] buffer = new byte[64];
			ByteBuffer.wrap(buffer).put(MAGIC_PACKET.getBytes());
			ByteBuffer.wrap(buffer).putInt(MAGIC_PACKET.length(), randomNumber);
			btOutStream.write(buffer, 0, MAGIC_PACKET.length() + Integer.SIZE);
			btOutStream.flush();

			int receivedbytes = 0;
			InputStream btInStream = btSocket.getInputStream();
			receivedbytes = btInStream.read(buffer);
			if (receivedbytes < 0) {
				Log.d("Multiplayer", "------- CLIENT: Received Bytes: -1 ------");
				return false;
			}

			if (MAGIC_PACKET.equals(new String(buffer, 0, MAGIC_PACKET.length(), "ASCII"))) {
				Log.d("Multiplayer", "------- CLIENT: NOW CLIENT!! ------");
				return true;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d("Multiplayer", "------- CLIENT: end of function reached!! ------");
		return false;
	}

	public boolean handleDoubleConnectionServer(BluetoothSocket btSocket) {
		byte[] buffer = new byte[64];

		try {
			InputStream btInStream = btSocket.getInputStream();
			btInStream.read(buffer);
			if (!MAGIC_PACKET.equals(new String(buffer, 0, MAGIC_PACKET.length(), "ASCII"))) {
				// error wrong magic packet / RETURN FROM FUNCTION
			}
			Integer receivedRandomNumber = ByteBuffer.wrap(buffer).getInt(MAGIC_PACKET.length());
			Log.d("Multiplayer", "------- SERVER: Received Random Number: " + receivedRandomNumber + " ------");

			synchronized (randomNumber) {
				if (randomNumber == 0) {
					randomNumber = -1;
					Log.d("Multiplayer", "------- SERVER: Random Number: -1 ------");
					// now btSocket is the correct Server Socket!!
				} else if (randomNumber < receivedRandomNumber) { // what's if randomNumber == recivedRandomNumber !!
					btSocket.close();
					Log.d("Multiplayer", "------- SERVER: randomNumber < recivedRandomNumber --> NOW CLIENT!! ------");
					return false;
				}
			}

			OutputStream btOutStream = btSocket.getOutputStream();
			btOutStream.write(buffer, 0, MAGIC_PACKET.length());
			btOutStream.flush();

			Log.d("Multiplayer", "------- SERVER: NOW SERVER!! ------");
			return true;

		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d("Multiplayer", "------- SERVER: end of function reached!! ------");
		return false;
	}

	public void destroyMultiplayerManager() {
		if (multiplayerBtManager != null) {
			multiplayerBtManager.destroyMultiplayerBTManager();
			multiplayerBtManager = null;
			initialized = false;
		}
	}

	public static synchronized void sendBtMessage(String name, double value) {
		if (initialized == false) {
			Log.e("Multiplayer", "not initialized yet");
			return;
		}

		Bundle myBundle = new Bundle();
		myBundle.putString(SHARED_VARIABLE_NAME, name);
		myBundle.putDouble(SHARED_VARIABLE_VALUE, value);
		Message myMessage = btHandler.obtainMessage();
		myMessage.setData(myBundle);
		btHandler.sendMessage(myMessage);
	}

	public static void updateSharedVariable(String name, Double value) {
		UserVariableShared sharedVariable = ProjectManager.getInstance().getCurrentProject().getUserVariables()
				.getSharedVariabel(name);
		if (sharedVariable != null) {
			sharedVariable.setValueWithoutSend(value);
		}
	}
}
