package com.android.rtmp;

import android.util.Log;

public class RtmpClient extends RtmpNative {
	int mHandler = 0;
	
	public boolean rtmpInit(int timeout) {
		mHandler = RtmpInit(timeout);
		if(0 == mHandler) {
			return false;
		}
		Log.d(TAG, "rtmpInit ret: " + mHandler);
		return true;
	}
	
	public int rtmpSetUrl(String url) {
		if(0 == mHandler) {
			return -1;
		}
		Log.d(TAG, "rtmpSetUrl url: " + url);
		return RtmpSetUrl(url, mHandler);
	}

	public int rtmpSetLinkFlag(int flag) {
		if(0 == mHandler) {
			return -1;
		}
		
		Log.d(TAG, "rtmpSetLinkFlag flag: " + flag);
		return RTMPSetLinkProperties(19, flag, mHandler);
	}
	public int rtmpEnableWrite() {
		if(0 == mHandler) {
			return -1;
		}
		
		return RTMPEnableWrite(mHandler);
	}
	
	public int rtmpSetBufferMs(int buffer_size) {
		if(0 == mHandler) {
			return -1;
		}
		Log.d(TAG, "rtmpSetBufferMs buffer_size: " + buffer_size);
		return RtmpSetBufferMs(buffer_size, mHandler);
	}
	
	public int rtmpConnect() {
		if(0 == mHandler) {
			return -1;
		}
		return RtmpConnect(mHandler);
	}
	
	public int rtmpConnectStream(int seek_time) {
		if(0 == mHandler) {
			return -1;
		}
		Log.d(TAG, "rtmpConnectStream seek_time: " + seek_time);

		return RtmpConnectStream(seek_time, mHandler);
	}
	
	public int rtmpRead(byte[] data, int buffer_size) {
		if(0 == mHandler) {
			return -1;
		}
		return RtmpRead(data, buffer_size, mHandler);
	}
	
	public int rtmpGetStreamId() {
		if(0 == mHandler) {
			return -1;
		}
		return RtmpGetStreamId(mHandler);
	}
	public int rtmpIsConnected() {
		if(0 == mHandler) {
			return -1;
		}
		return RtmpIsConnected(mHandler);
	}
	
	
	public int rtmpSendPacket(int queue, RtmpPacket packet) {
		if(0 == mHandler) {
			return -1;
		}
		return RtmpSendPacket(queue, packet.getmHandler(), mHandler);
	}
	
	
	public int rtmpWrite(byte[] bytes, int size) {
		if(0 == mHandler) {
			return -1;
		}
		return RtmpWrite(bytes, size, mHandler);
	}
		
	
	public int rtmpClose()
	{
		if(0 == mHandler) {
			return -1;
		}
		return RtmpClose(mHandler);
	}
	
}
