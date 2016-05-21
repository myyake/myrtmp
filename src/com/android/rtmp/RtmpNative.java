package com.android.rtmp;

import android.util.Log;

public abstract class RtmpNative {
	public final static String TAG = "rtmp"; 
	
	static {
		try {
			System.loadLibrary("RtmpJni");
		}catch(Exception ex) {
			Log.d(TAG, ex.getMessage());
		}
		Log.d(TAG, "load success");
	}
	
	protected native int RtmpInit(int timeout);
	
	protected native int RtmpSetUrl(String url, int handler);
	
	protected native int RTMPSetLinkProperties(int type, int value, int handler);
	
	protected native int RTMPEnableWrite(int handler);
	
	protected native int RtmpSetBufferMs(int buffer_size, int handler);
	
	protected native int RtmpConnect(int handler);
	
	protected native int RtmpGetStreamId(int handler);
	
	protected native int RtmpConnectStream(int seek_time, int handler);
	
	protected native int RtmpRead(byte[] data, int buffer_size, int handler);
		
	protected native int RtmpIsConnected(int handler);
	
	protected native int RtmpSendPacket(int queue, int packet, int handler);
	
	protected native int RtmpWrite(byte[] data, int size, int handler);

	protected native int RtmpClose(int handler);
	
/***************************************************************/
	protected native int InitRtmpPacket();
	
	protected native int RtmpPacketAlloc(int alloc_size, int handler);

	protected native int RtmpPacketReset(int handler);

	protected native int RtmpPacketRelease(int handler);
	
	protected native int RtmpPacketProperties(int value, int type, int handler);

	protected native int RtmpPacketSetChunk(int chunk, int handler);
	
	protected native int RtmpPacketSetBody(byte[] body, int body_size, int handler);
/***************************************************************/	
}