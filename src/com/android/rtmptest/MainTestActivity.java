package com.android.rtmptest;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.rtmp.FlvDecoder;
import com.android.rtmp.R;
import com.android.rtmp.RtmpClient;
import com.android.rtmp.RtmpPacket;


/* ������ο�write��packet��������
 * 
 * RMTP ����apiʹ�� Rtmpֻ�Ǹ�Э��
 * 1. RtmpClient client = new RtmpClient();
 * 
 * 2. mRtmp.rtmpSetUrl(url); //����url
 * 
 * 3. mRtmp.rtmpEnableWrite(); //���õ�ǰ��д����
 * 
 * 4. mRtmp.rtmpConnect(); // ���ӷ�����
 * 
 * 5. mRtmp.rtmpConnectStream(0); 
 * 
 * ��ʽ1 ����write��ʽ
 * while(true) {
 * 		mRtmp.rtmpIsConnected(); // �ж��Ƿ�������
 * 		mRtmp.rtmpWrite(buf);		 // ���͵������ , buf���Ƕ�Ӧ������Դ ������Ҫ���ݱ����ʽ���д��� ��ͬ�ı����ʽ���ܲ�һ��
 * }
 * 
 *  * ��ʽ2 ����packet��ʽ 
 * while(true) {
 * 		mRtmp.rtmpIsConnected(); // �ж��Ƿ�������
 * 		����packet 
 * 		mRtmp.rtmpSendPacket(buf);		 // ���͵������ , buf���Ƕ�Ӧ������Դ ������Ҫ���ݱ����ʽ���д��� ��ͬ�ı����ʽ���ܲ�һ��
 * }
 *  6. mRtmp.rtmpClose(); // jni��������ͷ���Դ
 */
public class MainTestActivity extends Activity {
	public final static String TAG = "rtmprun";

	private RtmpClient mRtmp = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if(mRtmp != null) {
			mRtmp.rtmpClose();
			mRtmp = null;
		}
		mRtmp = new RtmpClient();
		// mRtmp = null;

		findViewById(R.id.init).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mRtmp.rtmpInit(30);
			}
		});

		findViewById(R.id.read).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String url = "rtmp://live.hkstv.hk.lxdns.com/live/hks";
				int ret = mRtmp.rtmpSetUrl(url);

				if (ret != 0) {
					Log.d(TAG, "set return: " + ret);
					mRtmp.rtmpClose();
					return;
				}

				mRtmp.rtmpSetLinkFlag(2);
				mRtmp.rtmpSetBufferMs(3600 * 1000);

				if (mRtmp.rtmpConnect() < 0) {
					Log.d(TAG, "rtmpConnect failed");
					mRtmp.rtmpClose();
					return;
				}

				if (mRtmp.rtmpConnectStream(0) < 0) {
					Log.d(TAG, "rtmpConnectStream failed");
					mRtmp.rtmpClose();
					return;
				}

				int nRead = -1;
				int buffer_size = 1024 * 1024 * 10;
				byte[] data = new byte[buffer_size];
				int countbufsize = 0;
				while (true) {
					nRead = mRtmp.rtmpRead(data, buffer_size);
					if (nRead <= 0) {
						Log.d(TAG, "read over");
						break;
					}
					countbufsize += nRead;
					String out = "Receive: " + nRead + "Byte, Total: "
							+ (countbufsize * 1.0 / 1024) + "fkB";
					Log.d(TAG, out);
				}

				mRtmp.rtmpClose();
			}
		});

		findViewById(R.id.write).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String url = "rtmp://192.168.1.110:1935/publishlive/livestream";
				int ret = mRtmp.rtmpSetUrl(url);

				if (ret != 0) {
					Log.d(TAG, "set return: " + ret);
					mRtmp.rtmpClose();
					return;
				}

				mRtmp.rtmpEnableWrite();
				if (mRtmp.rtmpConnect() < 0) {
					Log.d(TAG, "rtmpConnect failed");
					mRtmp.rtmpClose();
					return;
				}

				if (mRtmp.rtmpConnectStream(0) < 0) {
					Log.d(TAG, "rtmpConnectStream failed");
					mRtmp.rtmpClose();
					return;
				}
				
//				FlvDecoder flvDecoder = new FlvDecoder("/mnt/sdcard/test.flv");
				long startTime = System.currentTimeMillis();;
				int preFrameTime = 0;
				int lastTime = 0;
				int nextIsKey = 1;
				boolean running = true;
				
				FileInputStream fileStream = null;
				DataInputStream in = null;
				
				// ��ʼ���ļ��� Ҳ��������Դ
				try {
					fileStream = new FileInputStream("/mnt/sdcard/test.flv");
					in = new DataInputStream(new BufferedInputStream(fileStream));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				/*
				 * flv �ļ���ʽ https://wuyuans.com/2012/08/flv-format/
				 * ������header 11���ֽ�
				 */
				try {
					in.skipBytes(9 + 4);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				while(running) {
					try {
						/*
						 *  preFrameTime ��ʾ��ǰƬ��ƫ���ʼƬ�ζ���ˣ��ͺ����㿴��Ƶһ���������ڼ������� ��������һ�λ���
						 */
						long now = System.currentTimeMillis();
						if( (now - startTime < preFrameTime) && (nextIsKey == 1)) {
							/*
							 * �����ǰʱ��û�иϵ����ʱ���Ъһ�ᣬʲô��˼�ͺñȵ�ǰ���͵����Ƭ����100ms������Ҫ�ȵ���101ms�ٿ�ʼ������һ��Ƭ��
							 */
							if(preFrameTime > lastTime){
								Log.d(TAG, "TimeStamp: " + preFrameTime + " ms");
								lastTime = preFrameTime;
							}
							Thread.sleep(10);
							continue;
						}
						
						Log.d(TAG, "continue!!!");
						
						/*
						 * ÿ��Ƭ����������
						 */
	
//							����		����			����
//							Tag����	1 bytes		8����Ƶ 9����Ƶ  18���ű� ����������
//							����������	3 bytes		���������ĳ���
//							ʱ���		3 bytes		��������λ�Ǻ��롣���ڽű��͵�tag����0
//							ʱ�����չ	1 bytes		��ʱ�����չΪ4bytes�������8λ�������õ�
//							StreamsID	3 bytes	����0
//							������(data)	�����������Ⱦ���	����ʵ��
						
						in.mark(7);
						int type = in.readByte() & 0xff;
						
						byte[] lArray = new byte[3];
						in.read(lArray);
						int length = FlvDecoder.read3u(lArray);
						
						byte[] tArray = new byte[3];
						in.read(tArray);
						int timeStamp = FlvDecoder.read3u(tArray);
						preFrameTime = timeStamp;
						
						in.reset();
						Log.d(TAG, "type: " + type + ", length: " + length + ", time: " + timeStamp);

						// ��������ݿ鷢��ȥ
						int sendLength = length + 11 + 4;
						byte[] sendBuffer = new byte[sendLength];
						in.read(sendBuffer);
						
						// ���ж��Ƿ�connect
						if (mRtmp.rtmpIsConnected() < 0){
							Log.d(TAG, "rtmpIsConnected failed");
							break;
						}
						
						// ���͹�ȥ
						int sendBytes = mRtmp.rtmpWrite(sendBuffer, sendLength);
						if(sendBytes <= 0) {
							Log.d(TAG, "rtmpWrite failed");
							break;
						}
						
						// �ж���һ��Ƭ�Σ���Ҫ������ֹ
						in.mark(1);
						int nextType = in.readByte();
						in.reset();
						
						if(nextType == 0x09) {
							in.mark(11 + 1);
							if(in.skip(11) == 0) {
								Log.d("rtmp", "skip failed");
								nextIsKey = -1;
								break;
							}
							
							nextType = in.readByte() & 0xff;
							if(nextType == 0x17) {
								nextIsKey = 1;
							} else {
								nextIsKey = 0;
							}
							in.reset();
						}	
						Log.d(TAG, "nextType: " + nextType + ", nextIsKey: " + nextIsKey);
					} catch (Exception e) {
						Log.d(TAG, "send data over!!!");
						e.printStackTrace();
						running = false;
						
					}			
				}	
				
				mRtmp.rtmpClose();
			}
		});
		
		findViewById(R.id.packet).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String url = "rtmp://192.168.1.110:1935/publishlive/livestream";
				int ret = mRtmp.rtmpSetUrl(url);

				if (ret != 0) {
					Log.d(TAG, "set return: " + ret);
					mRtmp.rtmpClose();
					return;
				}

				mRtmp.rtmpEnableWrite();
				if (mRtmp.rtmpConnect() < 0) {
					Log.d(TAG, "rtmpConnect failed");
					mRtmp.rtmpClose();
					return;
				}

				if (mRtmp.rtmpConnectStream(0) < 0) {
					Log.d(TAG, "rtmpConnectStream failed");
					mRtmp.rtmpClose();
					return;
				}

				
				RtmpPacket packet = new RtmpPacket(1024 * 512);
				packet.setHasAbsTimestamp(0);
				packet.setnChannel(4);
				packet.setnInfoField2(mRtmp.rtmpGetStreamId());	
				
				FlvDecoder flvDecoder = new FlvDecoder("/mnt/sdcard/test.flv");
				long startTime = System.currentTimeMillis();
				int nextIsKey = 1;
				flvDecoder.skipHeader();
				boolean running = true;
				
				while(running) {
					long now = System.currentTimeMillis();
					if(!flvDecoder.CheckFirst(now, startTime, nextIsKey)) {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return ;
						}
						continue;
					}
					
					try {
						if(!flvDecoder.readMedia(packet)) {
							continue;
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						
						running = false;
						Log.d("rtmp", "readMedia is failed, running: " + running);
					}
					
					Log.d(TAG, "running: " + running);
					if(mRtmp.rtmpIsConnected() <= 0) {
						Log.d("rtmp", "unconnected");
						running = false;
						break;
					}
					
					if(mRtmp.rtmpSendPacket(0, packet) <= 0){
						Log.d(TAG, "rtmpSendPacket failed");
						running = false;
						break;
					}
					
					int key = 0;
					try {
						key = flvDecoder.checkNext(nextIsKey);
						if(key != -1) {
							nextIsKey = key;
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.d(TAG, "nextIsKey is failed");
						e.printStackTrace();
						running = false;
						break;
					}
					Log.d(TAG, "nextIsKey: " + nextIsKey);
				}
				
				Log.d(TAG, "send over!!!");
				
				// ��Դ�����ֶ��ͷţ�����
				packet.rtmpPacketRelease();
				mRtmp.rtmpClose();
			}
		});
	}
	
	
}
