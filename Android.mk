###############make package###################################  
LOCAL_PATH:= $(call my-dir)
MY_PATH := $(LOCAL_PATH)
include $(CLEAR_VARS)

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 \
                  
LOCAL_MODULE_TAGS := optional

		
LOCAL_PACKAGE_NAME := VideoTest
LOCAL_JNI_SHARED_LIBRARIES := libJniDataEncode
LOCAL_REQUIRED_MODULES := libJniDataEncode

ALL_DEFAULT_INSTALLED_MODULES += $(LOCAL_PACKAGE_NAME)
LOCAL_CERTIFICATE := platform
include $(BUILD_PACKAGE)

include $(MY_PATH)/jni/Android.mk
