package com.hyunnyapp.brainylivingroom.ble;

import android.bluetooth.BluetoothDevice;

public interface BleConnectionManagerDelegate {
	void connected(BleConnectionManager manager, BluetoothDevice device);
	void disconnected(BleConnectionManager manager, BluetoothDevice device);
	void failToConnect(BleConnectionManager manager, BluetoothDevice device);
	void receivedData(BleConnectionManager manager, BluetoothDevice device, String data);
}
