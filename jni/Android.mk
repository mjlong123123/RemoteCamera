LOCAL_PATH := $(call my-dir)


include $(CLEAR_VARS)

LOCAL_MODULE := libspeex
LOCAL_CFLAGS = -DFIXED_POINT -DUSE_KISS_FFT -DEXPORT="" -UHAVE_CONFIG_H
LOCAL_C_INCLUDES := $(LOCAL_PATH)/speex/include
LOCAL_SRC_FILES := \
./speex/libspeex/bits.c \
./speex/libspeex/buffer.c \
./speex/libspeex/cb_search.c \
./speex/libspeex/exc_10_16_table.c \
./speex/libspeex/exc_10_32_table.c \
./speex/libspeex/exc_20_32_table.c \
./speex/libspeex/exc_5_256_table.c \
./speex/libspeex/exc_5_64_table.c \
./speex/libspeex/exc_8_128_table.c \
./speex/libspeex/fftwrap.c \
./speex/libspeex/filterbank.c \
./speex/libspeex/filters.c \
./speex/libspeex/gain_table.c \
./speex/libspeex/gain_table_lbr.c \
./speex/libspeex/hexc_10_32_table.c \
./speex/libspeex/hexc_table.c \
./speex/libspeex/high_lsp_tables.c \
./speex/libspeex/jitter.c \
./speex/libspeex/kiss_fft.c \
./speex/libspeex/kiss_fftr.c \
./speex/libspeex/lpc.c \
./speex/libspeex/lsp.c \
./speex/libspeex/lsp_tables_nb.c \
./speex/libspeex/ltp.c \
./speex/libspeex/mdf.c \
./speex/libspeex/modes.c \
./speex/libspeex/modes_wb.c \
./speex/libspeex/nb_celp.c \
./speex/libspeex/preprocess.c \
./speex/libspeex/quant_lsp.c \
./speex/libspeex/resample.c \
./speex/libspeex/sb_celp.c \
./speex/libspeex/scal.c \
./speex/libspeex/smallft.c \
./speex/libspeex/speex.c \
./speex/libspeex/speex_callbacks.c \
./speex/libspeex/speex_header.c \
./speex/libspeex/stereo.c \
./speex/libspeex/vbr.c \
./speex/libspeex/vq.c \
./speex/libspeex/window.c

include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES:= \
                libjthread/src/jmutex.cpp \
                libjthread/src/jthread.cpp \
LOCAL_CFLAGS +=  -DHAVE_PTHREAD \
                 -DENABLE_TRACE \
LOCAL_C_INCLUDES:= $(LOCAL_PATH)/libjthread/include $(LOCAL_PATH)/libjthread/src 
LOCAL_EXPORT_C_INCLUDES:=$(LOCAL_C_INCLUDES)
LOCAL_PRELINK_MODULE := false
LOCAL_LDLIBS +=   -ldl -llog
LOCAL_MODULE:= libjthread
include $(BUILD_STATIC_LIBRARY)


include $(CLEAR_VARS)
LOCAL_SRC_FILES:=libjrtp/tools/gettypes.cpp \
                libjrtp/src/extratransmitters/rtpfaketransmitter.cpp \
                libjrtp/src/rtcpapppacket.cpp \
                libjrtp/src/rtcpbyepacket.cpp \
                libjrtp/src/rtcpcompoundpacket.cpp \
                libjrtp/src/rtcpcompoundpacketbuilder.cpp \
                libjrtp/src/rtcppacket.cpp \
                libjrtp/src/rtcppacketbuilder.cpp \
                libjrtp/src/rtcprrpacket.cpp \
                libjrtp/src/rtcpscheduler.cpp \
                libjrtp/src/rtcpsdesinfo.cpp \
                libjrtp/src/rtcpsdespacket.cpp \
                libjrtp/src/rtcpsrpacket.cpp \
                libjrtp/src/rtpcollisionlist.cpp \
                libjrtp/src/rtpdebug.cpp \
                libjrtp/src/rtperrors.cpp \
                libjrtp/src/rtpinternalsourcedata.cpp \
                libjrtp/src/rtpipv4address.cpp \
                libjrtp/src/rtpipv6address.cpp \
                libjrtp/src/rtplibraryversion.cpp \
                libjrtp/src/rtppacket.cpp \
                libjrtp/src/rtppacketbuilder.cpp \
                libjrtp/src/rtppollthread.cpp \
                libjrtp/src/rtprandom.cpp \
                libjrtp/src/rtpsession.cpp \
                libjrtp/src/rtpsessionparams.cpp \
                libjrtp/src/rtpsessionsources.cpp \
                libjrtp/src/rtpsourcedata.cpp \
                libjrtp/src/rtpsources.cpp \
                libjrtp/src/rtptimeutilities.cpp \
                libjrtp/src/rtpudpv4transmitter.cpp \
                libjrtp/src/rtpudpv6transmitter.cpp
LOCAL_CFLAGS += -DHAVE_TIME_H \-DHAVE_FCNTL_H \
                                -DHAVE_SYS_SELECT_H \
                                -DENABLE_TRACE \
                                -DHAVE_RTP_SUPPORT_IPV4MULTICAST\
                                -DOSIP_MT
LOCAL_C_INCLUDES:= $(LOCAL_PATH)/libjrtp/include $(LOCAL_PATH)/libjrtp/src $(LOCAL_PATH)/libjrtp/tools
LOCAL_EXPORT_C_INCLUDES:=$(LOCAL_C_INCLUDES)
LOCAL_STATIC_LIBRARIES :=\
           libjthread \
           
#LOCAL_LDLIBS += -lpthread -ldl
LOCAL_PRELINK_MODULE := false
LOCAL_MODULE:= libjrtp
include $(BUILD_STATIC_LIBRARY)
#$(call import-module,../../speex/jni)


include $(CLEAR_VARS)
LOCAL_MODULE    := rtp-jni
LOCAL_SRC_FILES := com_android_localcall_jni_rtp.cpp \
customrtpsession.cpp \
customspeex.cpp \
customrtp.cpp \
speex_jni.cpp
LOCAL_CFLAGS +=   -DENABLE_TRACE
LOCAL_C_INCLUDES:= $(LOCAL_PATH)/libjthread/include $(LOCAL_PATH)/libjthread/src $(LOCAL_PATH)/libjrtp/include $(LOCAL_PATH)/libjrtp/src $(LOCAL_PATH)/libjrtp/tools \
$(LOCAL_PATH)/speex/include
LOCAL_EXPORT_C_INCLUDES:=$(LOCAL_C_INCLUDES)
LOCAL_STATIC_LIBRARIES :=\
           libjthread libjrtp libspeex
LOCAL_LDLIBS += -llog
include $(BUILD_SHARED_LIBRARY)

#$(call import-module,libspeex)

#LOCAL_PATH := $(call my-dir)
#include $(CLEAR_VARS)
#LOCAL_MODULE    := rtp-jni
#LOCAL_SRC_FILES := rtp-jni.cpp
#LOCAL_CFLAGS +=   -DENABLE_TRACE
#LOCAL_C_INCLUDES:= $(LOCAL_PATH)/jthread $(LOCAL_PATH)/jrtp  
#LOCAL_EXPORT_C_INCLUDES:=$(LOCAL_C_INCLUDES)
#LOCAL_STATIC_LIBRARIES :=\
#           libjthread libjrtp 
#LOCAL_LDLIBS += -llog
#include $(BUILD_SHARED_LIBRARY)
#$(call import-module,libjthread)
#$(call import-module,libjrtp)
include $(CLEAR_VARS)

LOCAL_SRC_FILES+= com_android_ffmpeglib_H264Decoder.c
LOCAL_C_INCLUDES+=$(LOCAL_PATH)/libffmpeg
LOCAL_STATIC_LIBRARIES:= libavcodec libavformat libavutil
LOCAL_LDLIBS:=-llog
LOCAL_MODULE:=ffmpeglib
include $(BUILD_SHARED_LIBRARY)

#Sinclude $(CLEAR_VARS)


LOCAL_MODULE    := testvideo
MY_CPP_LIST := $(wildcard $(LOCAL_PATH)/*.cpp)
MY_CPP_LIST += $(wildcard $(LOCAL_PATH)/*.c)
#LOCAL_SRC_FILES := $(MY_CPP_LIST:$(LOCAL_PATH)/%=%)S
#LOCAL_SRC_FILES := com_example_testvideo_yuvconvert.cpp
#LOCAL_SRC_FILES := $(%.cpp)
#LOCAL_SRC_FILES += $(%.c)
LOCAL_LDLIBS += -llog
#LOCAL_STATIC_LIBRARIES := libavcodec libavformat libavutil libpostproc libswscale

#include $(BUILD_SHARED_LIBRARY)

include $(call all-makefiles-under,$(LOCAL_PATH))
