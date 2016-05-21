package com.android.rtmp;

public class RTMPChunkJni {
	int headerSize;
    int chunkSize;
    String chunk;
    String header;
    int mHandler;
    
    RTMPChunkJni() {
    	
    }
    
	public int getHeaderSize() {
		return headerSize;
	}
	public void setHeaderSize(int headerSize) {
		this.headerSize = headerSize;
	}
	public int getChunkSize() {
		return chunkSize;
	}
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}
	public String getChunk() {
		return chunk;
	}
	public void setChunk(String chunk) {
		this.chunk = chunk;
	}
	public String getHeader() {
		return header;
	}
	public void setHeader(String header) {
		this.header = header;
	}
	public int getmHandler() {
		return mHandler;
	}
    
}
