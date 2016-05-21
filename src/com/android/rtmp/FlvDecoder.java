/*
 * flv�ļ���ʽ�ο� https://wuyuans.com/2012/08/flv-format/
 * �ϸ�˳�������ķ�ʽ�������òο�example˳��
 */

package com.android.rtmp;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.util.Log;

public class FlvDecoder {
	private DataInputStream in = null;
	private String fileName = null;
	int preFrameTime = 0;
	int lastTime = 0;
	
	public static int read3u(byte bytes[]) {
		int a = (bytes[0] & 0xff) << 16;
		int b = (bytes[1] & 0xff) << 8;
		int c = (bytes[2] & 0xff);
		return (a | b | c);
	}
	
	public FlvDecoder() {
	}
	
	public FlvDecoder(String fileName) {
		this.fileName = fileName;
		try {
			FileInputStream fileStream = new FileInputStream(fileName);
			in = new DataInputStream(new BufferedInputStream(fileStream));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * \note �Ƿ�Ҫ�����һ��
	 * \return true ���� false Ъһ��
	 */
	public boolean CheckFirst(long now, long start, int nextIsKey) {
		if( (now - start < preFrameTime) && (nextIsKey == 1)) {
			if(preFrameTime > lastTime){
				lastTime = preFrameTime;
			}
			return false;
		}
		return true;		
	}
	
	/*
	 * ����ͷ�ֶ�
	 */
	public void skipHeader() {
		try {
			in.skipBytes(9 + 4);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * ����ͷ�ֶ�
	 */
	public boolean decoderHeaders(byte flv[], int version, int streamInfo, int headerSize) {
		try {
			flv = null;
			flv = new byte[3];
			in.read(flv);

			version = (in.readByte() & 0xff);
			streamInfo = (in.readByte() & 0xff);
			headerSize = in.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/*
	 * ����ÿ��tag
	 * @param type: ��ǰtag ����
	 * @param length: ��ǰtag���峤��
	 * @param timeStamp: ��ǰʱ���
	 * @param body: ��ǰbody
	 * @retval �Ƿ���ȷ
	 */
	public boolean readMedia(RtmpPacket packet) throws Exception {
		int type = in.readByte() & 0xff;
		
		byte[] lArray = new byte[3];
		in.read(lArray);
		int length = read3u(lArray);
		
		byte[] tArray = new byte[3];
		in.read(tArray);
		preFrameTime = read3u(tArray);
		
		in.skip(4);
		
		Log.d("rtmp", "readMedia timeStamp: " + preFrameTime + ", type: " + type + ", length: " + length);
		
		if(type != 0x08 && type != 0x09) {
			in.skipBytes(length + 4);
			return false;
		}
		
		byte[] body = new byte[length];
		if(length != in.read(body)) {
			throw new Exception();
		}
		
		packet.setHeaderType(0);
		packet.setnTimeStamp(preFrameTime);
		packet.setPacketType(type);
		packet.setnBodySize(length);
		packet.setBody(body, length);
		
		return true;
	}
	
	public int readType() {
		int type = -1;;
		try {
			type = in.readByte() & 0xff;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return type;
	}
	
	public int readLength() {
		int length = -1;;
		try {
			byte[] lArray = new byte[3];
			in.read(lArray);
			length = read3u(lArray);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
		return length;
	}
	
	public int readTimeStamp() {
		int timeStamp = -1;;
		try {
			byte[] tArray = new byte[3];
			in.read(tArray);
			timeStamp = read3u(tArray);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return timeStamp;
	}
	
	
	/*
	 * �����һ��tag�Ϸ���
	 * @retval nextIsKey
	 */
	public int checkNext(int next) throws Exception{
		int nextIsKey = next;
		int preTagSize = in.readInt();
		in.mark(1);
		int nextType = in.readByte();
		in.reset();
		
		if(nextType == 0x09) {
			in.mark(11 + 1);
			if(in.skip(11) == 0) {
				Log.d("rtmp", "skip failed");
				nextIsKey = -1;
				throw new Exception();
			}
			
			nextType = in.readByte() & 0xff;
			if(nextType == 0x17) {
				nextIsKey = 1;
			} else {
				nextIsKey = 0;
			}
			in.reset();
		}
		
		return nextIsKey;
	}
}
