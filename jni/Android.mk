LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := rtmp
LOCAL_SRC_FILES := librtmp.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := ssl
LOCAL_SRC_FILES := libssl.so
include $(PREBUILT_SHARED_LIBRARY )

include $(CLEAR_VARS)
LOCAL_MODULE    := crypto
LOCAL_SRC_FILES := libcrypto.so
include $(PREBUILT_SHARED_LIBRARY )

include $(CLEAR_VARS) 
LOCAL_MODULE    := RtmpJni 
LOCAL_CFLAGS    += -Werror -frtti -fexceptions -g
LOCAL_C_INCLUDES = ./include
LOCAL_SRC_FILES := rtmp_jni.cpp
#LOCAL_SRC_FILES := libssl.so libcrypto.so
LOCAL_LDLIBS    := -llog -lz -L. -lrtmp -lssl -lcrypto
LOCAL_STATIC_LIBRARIES := rtmp
include $(BUILD_SHARED_LIBRARY)

