
/*
 * rtmp.cpp
 *
 *  Created on: 2016骞�4鏈�30鏃�
 *      Author: zhangjl
 */

#include "jni.h"
#include "string.h"
#include <android/log.h>
#include "stdio.h"
#include "stdlib.h"
#include "include/rtmp.h"
#include "include/rtmp_sys.h"
#include "include/log.h"

#ifdef __cplusplus
extern "C"
{
#endif
#define TAG "rtmp"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__) // 定义LOGD类型

#ifndef NELEM
# define NELEM(x) ((jint) (sizeof(x) / sizeof((x)[0])))
#endif

static const char* const kClassPathName = "com/android/rtmp/RtmpNative";

static JavaVM* jvm = NULL;
static jobject clazz_obj = NULL;

/*********************************** RTMP Include ************************************/

static jint RtmpInit(JNIEnv* env, jobject clazz, jint timeout)
{
	if (clazz_obj == NULL) {
		clazz_obj = env->NewGlobalRef(clazz);
	}

//	FILE *fp = fopen("/mnt/sdcard/log.log", "w");
//	RTMP_LogSetOutput(fp);

	RTMP* rtmp = RTMP_Alloc();
	if(NULL == rtmp) {
		return -1;
	}

	RTMP_Init(rtmp);
	rtmp->Link.timeout = timeout;

	jint handler = (jint)rtmp;
	__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpInit] handler is %d", handler);
	return (jint)rtmp;

}

static jint RtmpSetUrl(JNIEnv* env, jobject clazz, jstring url, jint handler)
{
	if(0 == handler) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpSetUrl] handler is null");
		return -1;
	}

	RTMP* rtmp = (RTMP*)handler;
	const char* obj_url = env->GetStringUTFChars(url, 0);
	char *out_str  = (char*)malloc(strlen(obj_url) + 1);
	memset(out_str, 0, strlen(obj_url) + 1);
	strcpy(out_str, obj_url);

	jint ret = RTMP_SetupURL(rtmp, (char*)"rtmp://live.hkstv.hk.lxdns.com/live/hks");
	if(ret == 0) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpSetUrl] handler: %d, RTMP_SetupURL error:%s", handler, obj_url);
		env->ReleaseStringUTFChars(url, obj_url);
		return -1;
	}
	rtmp->Link.lFlags|=2;
	__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpSetUrl] handler: %d, out_str set url: %s, 1 strlen(rtmp_url): %d",
			handler, out_str, strlen(out_str));

	env->ReleaseStringUTFChars(url, obj_url);
	
	return 0;
}

static jint RTMPSetLinkProperties(JNIEnv* env, jobject clazz, jint type, jint value, jint handler)
{
	if(0 == handler) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RTMPSetLinkProperties] handler is null");
		return -1;
	}
	RTMP* rtmp = (RTMP*)handler;
	__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RTMPSetLinkProperties] handler: %d, type: %d, value: %d",
			handler, type, value);

	switch(type) {
	// 依据属性设置
	case 19:
		rtmp->Link.lFlags|=value;
		break;
	default:
		break;
	}

	return 0;
}
static jint RTMPEnableWrite(JNIEnv* env, jobject clazz, jint handler)
{
	if(0 == handler) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RTMPEnableWrite] handler is null");
		return -1;
	}
	__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RTMPEnableWrite] handler: %d", handler);

	RTMP* rtmp = (RTMP*)handler;
	RTMP_EnableWrite(rtmp);
	return 0;
}

static jint RtmpSetBufferMs(JNIEnv* env, jobject clazz, jint buffer_size, jint handler)
{
	if(0 == handler) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpSetBufferMs] handler is null");
		return -1;
	}
	
	RTMP* rtmp = (RTMP*)handler;
	RTMP_SetBufferMS(rtmp, buffer_size);
	__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpSetBufferMs] handler: %d, buffer_size: %d", handler, buffer_size);

	return 0;
}

static jint RtmpConnect(JNIEnv* env, jobject clazz, jint handler)
{
	if(0 == handler) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpConnect] handler is null");
		return -1;
	}
	
	RTMP* rtmp = (RTMP*)handler;
	jint ret = RTMP_Connect(rtmp, NULL);
	if(ret == 0) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpConnect] handler: %d RTMP_Connect error", handler);
		return -1;
	}
	__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpConnect] handler: %d, ret: %d", handler, ret);

	return 0;
}

static jint RtmpGetStreamId(JNIEnv* env, jobject clazz, jint handler)
{
	if(0 == handler) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpConnect] handler is null");
		return -1;
	}

	RTMP* rtmp = (RTMP*)handler;
	return rtmp->m_stream_id;
}

static jint RtmpConnectStream(JNIEnv* env, jobject clazz, jint seek_time, jint handler)
{
	if(0 == handler) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpConnectStream] handler is null");
		return -1;
	}
	
	RTMP* rtmp = (RTMP*)handler;
	jint ret = RTMP_ConnectStream(rtmp, seek_time);
	if(ret == 0) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpConnectStream] RTMP_ConnectStream error:%d", seek_time);
		return -1;		
	}

	__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpConnectStream] handler: %d, seek_time: %d", handler, seek_time);

	return 0;
}

static jint RtmpRead(JNIEnv* env, jobject clazz, jbyteArray buff, jint buf_size, jint handler)
{
	if(0 == handler) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpRead] handler is null");
		return -1;
	}

	RTMP* rtmp = (RTMP*)handler;
	jbyte* ptr =  env->GetByteArrayElements(buff, NULL);

	int read_bytes = RTMP_Read(rtmp, (char*)ptr, buf_size);

	env->ReleaseByteArrayElements(buff, ptr, 0);
	if(read_bytes <= 0) {
			__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpRead] handler: %d, sizeof(buf_char): %d, bufsize: %d, read_bytes: %d",
						handler, sizeof(ptr), buf_size, read_bytes);
	}
	return read_bytes;
}

static jint RtmpClose(JNIEnv* env, jobject clazz, jint handler)
{
	if(0 == handler) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpClose] handler is null");
		return -1;
	}
	__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpClose] handler: %d", handler);

	RTMP* rtmp = (RTMP*)handler;
	RTMP_Close(rtmp);
	RTMP_Free(rtmp);
	handler = 0;


	return 0;
}
//
//static jint RtmpGetTime(JNIEnv* env, jobject clazz)
//{
//	int time = RTMP_GetTime();
//	__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpGetTime] time: %d", time);
//	return time;
//
//}

static jint RtmpIsConnected(JNIEnv* env, jobject clazz, jint handler)
{
	if(0 == handler) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpIsConnected] handler is null");
		return -1;
	}

	RTMP* rtmp = (RTMP*)handler;
	int ret = RTMP_IsConnected(rtmp);
	__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RTMP_IsConnected] ret: %d", ret);
	return ret;


}

static jint RtmpSendPacket(JNIEnv* env, jobject clazz, jint queue, jint packet, jint handler)
{
	if(0 == handler || 0 == packet) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpSendPacket] handler is null");
		return -1;
	}

	RTMP* rtmp = (RTMP*)handler;
	RTMPPacket* packet_ptr = (RTMPPacket*)packet;

	int send_size = RTMP_SendPacket(rtmp, packet_ptr, queue);
	__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpSendPacket] handler:%d, queue: %d, packet: %d, send_size: %d, pakcet body: %p, m_nBodySize: %d",
			handler, queue, packet, send_size, packet_ptr->m_body, packet_ptr->m_nBodySize);

	return send_size;

}

static jint RtmpWrite(JNIEnv* env, jobject clazz, jbyteArray body, jint body_size, jint handler)
{
	if(0 == handler) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpWrite] handler is null");
		return -1;
	}

	RTMP* rtmp = (RTMP*)handler;
	jbyte* ptr =  (env->GetByteArrayElements(body, NULL));
	int send_size = RTMP_Write(rtmp, (char*)ptr, body_size);

	__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpWrite] handler:%d,  send_size: %d, body: %p, m_nBodySize: %d",
				handler, send_size, ptr, body_size);
	env->ReleaseByteArrayElements(body, ptr, 0);

	return send_size;
}


/*********************************** RTMP Include ************************************/


/*********************************** RTMP PACKET ************************************/

static jint InitRtmpPacket(JNIEnv* env, jobject clazz)
{
	RTMPPacket* packet=(RTMPPacket*)malloc(sizeof(RTMPPacket));
	if(NULL == packet) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[InitRtmpPacket] handler is null");

		return -1;
	}
	memset(packet, 0, sizeof(RTMPPacket));
	
	jint handler = (jint)packet;
	__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[InitRtmpPacket] handler: %d", handler);

	return handler;
}

static jint RtmpPacketAlloc(JNIEnv* env, jobject clazz, jint alloc_size, jint handler)
{
	RTMPPacket* packet=(RTMPPacket*)handler;
	if(0 == handler) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpPacketAlloc] handler is null");

		return -1;
	}
	__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpPacketAlloc] handler:%d, alloc_size: %d", handler, alloc_size);

	RTMPPacket_Alloc(packet, alloc_size);
	return 0;
}

static jint RtmpPacketReset(JNIEnv* env, jobject clazz, jint handler)
{
	RTMPPacket* packet=(RTMPPacket*)handler;
	if(0 == handler) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpPacketReset] handler is null");

		return -1;
	}
	__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpPacketReset] handler:%d", handler);

	RTMPPacket_Reset(packet);
	return 0;
}

static jint RtmpPacketRelease(JNIEnv* env, jobject clazz, jint handler)
{
	RTMPPacket* packet=(RTMPPacket*)handler;
	if(0 == handler) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpPacketRelease] handler is null");

		return -1;
	}
	__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpPacketRelease] handler:%d", handler);

	free(packet);
	handler = 0;
	return 0;
}


static jint RtmpPacketProperties(JNIEnv* env, jobject clazz, jint value, jint type, jint handler)
{
	if(0 == handler) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpPacketProperties] handler is null");
		return -1;
	}

	__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpPacketProperties] handler:%d, value:%d, type:%d",
			handler, value, type);

	RTMPPacket* packet = (RTMPPacket*)handler;
	switch(type) {
	case 0:
		packet->m_headerType = value;
		break;
	case 1:
		packet->m_packetType = value;
		break;
	case 2:
		packet->m_hasAbsTimestamp = value;
		break;
	case 3:
		packet->m_nChannel = value;
		break;
	case 4:
		packet->m_nTimeStamp = value;
		break;
	case 5:
		packet->m_nInfoField2 = value;
		break;
	case 6:
		packet->m_nBodySize = value;
		break;
	case 7:
		packet->m_nBytesRead = value;
		break;
	default:
		break;
	}

	return 0;
}

static jint RtmpPacketSetChunk(JNIEnv* env, jobject clazz, jint chunk, jint handler)
{
	if(0 == handler || 0 == chunk) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpPacketSetChunk] handler is null");
		return -1;
	}
	__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpPacketSetChunk] handler:%d, chunk:%d",
				handler, chunk);

	RTMPPacket* packet = (RTMPPacket*)handler;
	RTMPChunk* chunk_ptr = (RTMPChunk*)chunk;
	packet->m_chunk = chunk_ptr;
	return 0;
}

static jint RtmpPacketSetBody(JNIEnv* env, jobject clazz, jbyteArray body, jint body_size, jint handler)
{
	if(0 == handler) {
		__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "handler is null");
		return -1;
	}

	RTMPPacket* packet = (RTMPPacket*)handler;

	jbyte* ptr =  env->GetByteArrayElements(body, NULL);
	memcpy(packet->m_body, ptr, body_size);

	env->ReleaseByteArrayElements(body, ptr, 0);

	__android_log_print(ANDROID_LOG_DEBUG, "rtmp", "[RtmpPacketSetBody] handler:%d, size: %d, body: %p",
				handler, body_size, packet->m_body);

	return 0;
}

/*********************************** RTMP PACKET ************************************/


static JNINativeMethod gMethods[] = {
    {"RtmpInit",       "(I)I",    (void *)RtmpInit},
    {"RtmpSetUrl",     "(Ljava/lang/String;I)I",    	 (void *)RtmpSetUrl},
    {"RTMPSetLinkProperties","(III)I",         (void *)RTMPSetLinkProperties},
    {"RTMPEnableWrite",       "(I)I",         (void *)RTMPEnableWrite},
    {"RtmpSetBufferMs",       "(II)I",         (void *)RtmpSetBufferMs},
    {"RtmpConnect",       "(I)I",         (void *)RtmpConnect},
    {"RtmpGetStreamId",       "(I)I",         (void *)RtmpGetStreamId},
    {"RtmpConnectStream",       "(II)I",         (void *)RtmpConnectStream},
    {"RtmpRead",       "([BII)I",         (void *)RtmpRead},
    {"RtmpClose",       "(I)I",         (void *)RtmpClose},
//    {"RtmpGetTime",       "()I",         (void *)RtmpGetTime},
    {"RtmpIsConnected",       "(I)I",         (void *)RtmpIsConnected},
    {"RtmpSendPacket",       "(III)I",         (void *)RtmpSendPacket},
    {"InitRtmpPacket",       "()I",         (void *)InitRtmpPacket},
    {"RtmpPacketAlloc",       "(II)I",         (void *)RtmpPacketAlloc},
    {"RtmpPacketReset",       "(I)I",         (void *)RtmpPacketReset},
    {"RtmpPacketRelease",     "(I)I",         (void *)RtmpPacketRelease},
    {"RtmpPacketProperties",  "(III)I",         (void *)RtmpPacketProperties},
    {"RtmpPacketSetChunk",       "(II)I",         (void *)RtmpPacketSetChunk},
    {"RtmpPacketSetBody",       "([BII)I",         (void *)RtmpPacketSetBody},
    {"RtmpWrite","([BII)I",         (void *)RtmpWrite}
};


static jint register_nativeFunData(JNIEnv* env, const char* className,
							  JNINativeMethod* m, jint num)
{
	jclass clazz = (env)->FindClass(className);
	if(clazz == NULL)
		return JNI_FALSE;

	if((env)->RegisterNatives(clazz, m, num) < 0)
		return JNI_FALSE;
	return JNI_TRUE;
}

// This function only registers the native methods
jint register_com_android_dataencode(JavaVM* vm,JNIEnv *env)
{
	jvm = vm;
	return register_nativeFunData(env,kClassPathName, gMethods, NELEM(gMethods));
}

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	__android_log_print(ANDROID_LOG_INFO, TAG, " JNI_OnLoad");

    JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
    	__android_log_print(ANDROID_LOG_ERROR, TAG, " GetEnv failed");
        goto bail;
    }

    if (register_com_android_dataencode(vm,env) < 0) {
    	__android_log_print(ANDROID_LOG_ERROR, TAG, " register jnitest data failed");
		goto bail;
    }

    result = JNI_VERSION_1_4;

	bail:
		return result;
}

#ifdef __cplusplus
}
#endif
