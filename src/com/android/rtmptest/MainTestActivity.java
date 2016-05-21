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


/* 具体请参考write和packet两个例子
 * 
 * RMTP 推流api使用 Rtmp只是个协议
 * 1. RtmpClient client = new RtmpClient();
 * 
 * 2. mRtmp.rtmpSetUrl(url); //设置url
 * 
 * 3. mRtmp.rtmpEnableWrite(); //设置当前可写属性
 * 
 * 4. mRtmp.rtmpConnect(); // 连接服务器
 * 
 * 5. mRtmp.rtmpConnectStream(0); 
 * 
 * 方式1 采用write方式
 * while(true) {
 * 		mRtmp.rtmpIsConnected(); // 判断是否已连接
 * 		mRtmp.rtmpWrite(buf);		 // 发送到服务端 , buf就是对应的数据源 具体需要依据编码格式进行传输 不同的编码格式可能不一样
 * }
 * 
 *  * 方式2 采用packet方式 
 * while(true) {
 * 		mRtmp.rtmpIsConnected(); // 判断是否已连接
 * 		创建packet 
 * 		mRtmp.rtmpSendPacket(buf);		 // 发送到服务端 , buf就是对应的数据源 具体需要依据编码格式进行传输 不同的编码格式可能不一样
 * }
 *  6. mRtmp.rtmpClose(); // jni对象必须释放资源
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
				
				// 初始化文件流 也就是数据源
				try {
					fileStream = new FileInputStream("/mnt/sdcard/test.flv");
					in = new DataInputStream(new BufferedInputStream(fileStream));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				/*
				 * flv 文件格式 https://wuyuans.com/2012/08/flv-format/
				 * 先跳过header 11个字节
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
						 *  preFrameTime 表示当前片段偏移最开始片段多久了，就好像你看视频一样，看到第几分钟了 这里做了一次缓冲
						 */
						long now = System.currentTimeMillis();
						if( (now - startTime < preFrameTime) && (nextIsKey == 1)) {
							/*
							 * 如果当前时间差还没有赶到这段时间就歇一会，什么意思就好比当前发送的这个片段是100ms，那需要等到第101ms再开始发送下一个片段
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
						 * 每个片段启动解析
						 */
	
//							名称		长度			介绍
//							Tag类型	1 bytes		8：音频 9：视频  18：脚本 其他：保留
//							数据区长度	3 bytes		在数据区的长度
//							时间戳		3 bytes		整数，单位是毫秒。对于脚本型的tag总是0
//							时间戳扩展	1 bytes		将时间戳扩展为4bytes，代表高8位。很少用到
//							StreamsID	3 bytes	总是0
//							数据区(data)	由数据区长度决定	数据实体
						
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

						// 将这个数据块发过去
						int sendLength = length + 11 + 4;
						byte[] sendBuffer = new byte[sendLength];
						in.read(sendBuffer);
						
						// 先判断是否connect
						if (mRtmp.rtmpIsConnected() < 0){
							Log.d(TAG, "rtmpIsConnected failed");
							break;
						}
						
						// 发送过去
						int sendBytes = mRtmp.rtmpWrite(sendBuffer, sendLength);
						if(sendBytes <= 0) {
							Log.d(TAG, "rtmpWrite failed");
							break;
						}
						
						// 判断下一个片段，主要用于终止
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
				
				// 资源必须手动释放！！！
				packet.rtmpPacketRelease();
				mRtmp.rtmpClose();
			}
		});
	}
	
	
}
