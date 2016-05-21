package com.android.rtmp;

import android.util.Log;

public class RtmpPacket extends RtmpNative {
	private int mHandler = -1;
	private int headerType;
    private int packetType;
    private int hasAbsTimestamp;	/* timestamp absolute or relative? */
    private int nChannel;
    private int nTimeStamp;	/* timestamp */
    private int nInfoField2;	/* last 4 bytes in a long header */
    private int nBodySize;
    private int nBytesRead;
    RTMPChunkJni chunk;
    String body;
    
	public RtmpPacket() {
		mHandler = InitRtmpPacket();
	}
	
	public RtmpPacket(int packet_size) {
		mHandler = InitRtmpPacket();
		if(mHandler != 0) {
			rtmpPacketAlloc(packet_size);
			rtmpPacketReset();
		}
	}

	public int getmHandler() {
		return mHandler;
	}

	public int getHeaderType() {
		return headerType;
	}

	public void setHeaderType(int headerType) {
		this.headerType = headerType;
		if(mHandler == 0) {
			return;
		}
		RtmpPacketProperties(headerType, 0, mHandler);
	}

	public int getPacketType() {
		return packetType;
	}

	public void setPacketType(int type) {
		this.packetType = type;
		if(mHandler == 0) {
			return;
		}
		RtmpPacketProperties(type, 1, mHandler);
	}

	
	public int getHasAbsTimestamp() {
		return hasAbsTimestamp;
	}

	public void setHasAbsTimestamp(int hasAbsTimestamp) {
		this.hasAbsTimestamp = hasAbsTimestamp;
		if(mHandler == 0) {
			return;
		}
		RtmpPacketProperties(hasAbsTimestamp, 2, mHandler);
	}

	public int getnChannel() {
		return nChannel;
	}

	public void setnChannel(int nChannel) {
		this.nChannel = nChannel;
		if(mHandler == 0) {
			return;
		}
		RtmpPacketProperties(nChannel, 3, mHandler);
	}

	public int getnTimeStamp() {
		return nTimeStamp;
	}

	public void setnTimeStamp(int nTimeStamp) {
		this.nTimeStamp = nTimeStamp;
		if(mHandler == 0) {
			return;
		}
		RtmpPacketProperties(nTimeStamp, 4, mHandler);
	}

	public int getnInfoField2() {
		return nInfoField2;
	}

	public void setnInfoField2(int nInfoField2) {
		this.nInfoField2 = nInfoField2;
		if(mHandler == 0) {
			return;
		}
		RtmpPacketProperties(nInfoField2, 5, mHandler);
	}

	public int getnBodySize() {
		return nBodySize;
	}

	public void setnBodySize(int nBodySize) {
		this.nBodySize = nBodySize;
		if(mHandler == 0) {
			return;
		}
		RtmpPacketProperties(nBodySize, 6, mHandler);
	}

	public int getnBytesRead() {
		return nBytesRead;
	}

	public void setnBytesRead(int nBytesRead) {
		this.nBytesRead = nBytesRead;
		if(mHandler == 0) {
			return;
		}
		RtmpPacketProperties(nBytesRead, 7, mHandler);
	}
	
	
	public RTMPChunkJni getChunk() {
		return chunk;
	}

	public void setChunk(RTMPChunkJni chunk) {
		this.chunk = chunk;
		if(mHandler == 0) {
			return;
		}
		RtmpPacketSetChunk(chunk.getmHandler(), mHandler);
	}

	public String getBody() {
		return body;
	}

	public void setBody(byte[] body, int body_size) {
		Log.d("rtmp", "rtmp length: " + body.length);
		RtmpPacketSetBody(body, body_size, mHandler);
	}
	
	public int rtmpPacketAlloc(int alloc_size) {
		if(mHandler == 0) {
			return -1;
		}
		
		return RtmpPacketAlloc(alloc_size, mHandler);
	}

	public int rtmpPacketReset() {
		if(mHandler == 0) {
			return -1;
		}
		return RtmpPacketReset(mHandler);
	}
	
	public int rtmpPacketRelease() {
		if(mHandler == 0) {
			return -1;
		}
		
		return RtmpPacketRelease(mHandler);
	}
}
