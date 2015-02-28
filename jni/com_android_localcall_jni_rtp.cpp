#include <jni.h>
#include "rtpsession.h"
#include "rtpudpv4transmitter.h"
#include "rtpipv4address.h"
#include "rtpsessionparams.h"
#include "rtperrors.h"
#include "rtppacket.h"
#include "rtpsourcedata.h"


#ifndef WIN32
#include <netinet/in.h>
#include <arpa/inet.h>
#else
#include <winsock2.h>
#endif // WIN32
#include <stdlib.h>
#include <stdio.h>
#include <iostream>
#include <string>
#include <android/log.h>


#include "customrtpsession.h"
#include "com_android_localcall_jni_rtp.h"
#include "customspeex.h"
#include "jmutex.h"
#include "customrtp.h"
#include "autolock.h"
#include "define.h"

const char* const LOG_TAG = "RTP_JNI";
//for output android log
#define LOG_LOCAL(...)   __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
//throw java exception.
void jniThrowException(JNIEnv* env, const char* exc, const char* msg = NULL)
{
    jclass excClazz = env->FindClass(exc);
    env->ThrowNew(excClazz, msg);
}
//define fields for call java object fields and methods.
struct fields_t
{
    jfieldID    context;//restore JavaRtp to java Rtp object. context is Rtp object of mJavaRtp.
    jmethodID   callback;//restore Rtp object of callBackFromNative method.
    jmethodID   callback1;//restore Rtp object of callBackFromNative method.
};
//restore java rtp fields
static fields_t fields;
//synchronized JavaRtp use
static JMutex sLock;
//declear class
class JavaRtp;
//declear method
int MAX_PACKAGE_SIZE = 1000;

//restore JavaRtp to java object
void setJavaRtp(JNIEnv *env,jobject thiz,JavaRtp * jr)
{
    Autolock l(sLock);
    JavaRtp * old = (JavaRtp *)env->GetIntField(thiz, fields.context);
    if(old != NULL)
        delete(old);
    env->SetIntField(thiz, fields.context, (int)jr);
}
//get JavaRtp from java object
JavaRtp* getJavaRtp(JNIEnv *env,jobject thiz)
{
    Autolock l(sLock);
    JavaRtp * const p = (JavaRtp*)env->GetIntField(thiz, fields.context);
    return p;
}
//check error happen or not
bool checkerror(int rtperr)
{
    if (rtperr < 0)
    {
        LOG_LOCAL("checkerror %s\n",RTPGetErrorString(rtperr).c_str());
        return false;
    }
    return true;
}

//init fields
JNIEXPORT void JNICALL Java_com_android_localcall_jni_Rtp_native_1init(JNIEnv *env, jclass cla)
{
    jclass clazz;
    clazz = env->FindClass("com/android/localcall/jni/Rtp");
    if (clazz == NULL)
    {
        jniThrowException(env, "java/lang/RuntimeException", "Can't find  com/android/localcall/jni/Rtp");
        return;
    }

    fields.context = env->GetFieldID(clazz, "mJavaRtp", "I");
    if (fields.context == NULL)
    {
        jniThrowException(env, "java/lang/RuntimeException", "Can't find mJavaRtp");
        return;
    }

    fields.callback = env->GetStaticMethodID(clazz, "callBackFromNative","([SI)V");
    if (fields.callback == NULL)
    {
        jniThrowException(env, "java/lang/RuntimeException", "Can't find callBackFromNative");
        return;
    }
    fields.callback1 = env->GetStaticMethodID(clazz, "callBackFromNative1","([BI)V");
    if (fields.callback1 == NULL)
    {
        jniThrowException(env, "java/lang/RuntimeException", "Can't find callBackFromNative1");
        return;
    }
}
//set javaRtp
JNIEXPORT void JNICALL Java_com_android_localcall_jni_Rtp_native_1setup(JNIEnv *env,jobject thiz,jobject weak_ref)
{
    JavaRtp * mJavaRtp = new JavaRtp(env, thiz,weak_ref,fields.callback,fields.callback1);
    setJavaRtp(env,thiz,mJavaRtp);
}
// release javaRtp
JNIEXPORT void JNICALL Java_com_android_localcall_jni_Rtp_native_1rease(JNIEnv *env,jobject thiz)
{
    LOG_LOCAL("release");
    setJavaRtp( env, thiz,0);
}
JNIEXPORT jboolean JNICALL Java_com_android_localcall_jni_Rtp_openRtp
(JNIEnv * env, jobject thiz, jint port)
{
    RTPUDPv4TransmissionParams transparams;
    RTPSessionParams sessparams;
    int status;
    JavaRtp* jrtp;
    CustomSpeex * speex;
    CustomRTPSession * seesion;
    int framesize;
    jrtp = getJavaRtp(env,thiz);
    if(jrtp == NULL)
    {
        LOG_LOCAL("openRtp jrtp == NULL");
        jniThrowException(env, "java/lang/RuntimeException", "openRtp ERROR jrtp == NULL");
        return false;
    }
    if(jrtp->isOpen())
    {
        LOG_LOCAL("openRtp has opened");
        jniThrowException(env, "java/lang/RuntimeException", "openRtp ERROR has opened");
        return false;
    }
    seesion = jrtp->getCustomRTPSession();
    if(seesion == NULL)
    {
        LOG_LOCAL("openRtp seesion == NULL");
        jniThrowException(env, "java/lang/RuntimeException", "openRtp ERROR seesion == NULL");
        return false;
    }
    jrtp->setPortBase(port);
    speex = jrtp->getCustomSpeex();
    if(speex == NULL)
    {
        LOG_LOCAL("openRtp speex == NULL");
        jniThrowException(env, "java/lang/RuntimeException", "openRtp ERROR speex == NULL");
        return false;
    }
    framesize = speex->getFrameSize();
    if(framesize == 0)
    {
        LOG_LOCAL("openRtp framesize == 0");
        jniThrowException(env, "java/lang/RuntimeException", "openRtp ERROR framesize == 0");
        return false;
    }
    jrtp->setFrameSize(framesize);

    LOG_LOCAL("openRtp FrameSize:%d", framesize);
    LOG_LOCAL("openRtp portbase:%d", jrtp->getFrameSize());

    LOG_LOCAL("openRtp rtp max size:%d", sessparams.GetMaximumPacketSize());
    MAX_PACKAGE_SIZE = sessparams.GetMaximumPacketSize()-100;
    sessparams.SetJavaVM(jrtp->getJavaVM());
    sessparams.SetOwnTimestampUnit(1.0/20.0);
    sessparams.SetAcceptOwnPackets(false);
    sessparams.SetUsePollThread(true);
    transparams.SetPortbase(jrtp->getPortBase());

    status = seesion->Create(sessparams,&transparams);
    if(!checkerror(status))
    {
        return false;
    }
    status = seesion->SetDefaultPayloadType(101);
    if(!checkerror(status))
    {
        return false;
    }
    status = seesion->SetDefaultMark(false);
    if(!checkerror(status))
    {
        return false;
    }
    status = seesion->SetDefaultTimestampIncrement(160);
    if(!checkerror(status))
    {
        return false;
    }
    jrtp->setIsOpen(true);
    return true;
}

JNIEXPORT void JNICALL Java_com_android_localcall_jni_Rtp_closeRtp
(JNIEnv * env, jobject thiz)
{
    JavaRtp* jrtp;
    CustomSpeex * speex;
    CustomRTPSession * seesion;
    jrtp = getJavaRtp(env,thiz);
    if(jrtp == NULL)
    {
        LOG_LOCAL("openRtp jrtp == NULL");
        jniThrowException(env, "java/lang/RuntimeException", "closeRtp ERROR jrtp == NULL");
        return;
    }
    if(!jrtp->isOpen())
    {
        LOG_LOCAL("closeRtp has closed");
        jniThrowException(env, "java/lang/RuntimeException", "closeRtp ERROR has opened");
        return;
    }
    speex = jrtp->getCustomSpeex();
    if(speex == NULL)
    {
        LOG_LOCAL("closeRtp speex == NULL");
        jniThrowException(env, "java/lang/RuntimeException", "closeRtp ERROR speex == NULL");
        return;
    }
    seesion = jrtp->getCustomRTPSession();
    if(seesion == NULL)
    {
        LOG_LOCAL("closeRtp seesion == NULL");
        jniThrowException(env, "java/lang/RuntimeException", "closeRtp ERROR seesion == NULL");
        return;
    }
    seesion->BYEDestroy(RTPTime(1,0),0,0);

    jrtp->setIsOpen(false);
}

JNIEXPORT jint JNICALL Java_com_android_localcall_jni_Rtp_getFrameSize
(JNIEnv * env, jobject thiz)
{
    JavaRtp* jrtp;
    jrtp = getJavaRtp(env,thiz);
    if(jrtp == NULL)
    {
        LOG_LOCAL("getFrameSize jrtp == NULL");
        jniThrowException(env, "java/lang/RuntimeException", "getFrameSize ERROR jrtp == NULL");
        return 0;
    }
    return jrtp->getFrameSize();
}


JNIEXPORT void JNICALL Java_com_android_localcall_jni_Rtp_write___3BZ

(JNIEnv * env, jobject thiz, jbyteArray jar, jboolean isAll)
{
    int index = 0;
    JavaRtp* jrtp;
    CustomRTPSession * seesion;
    int framesize=0;
    int status;
    int lenth = 0;
	jboolean iscopy = false;
	jbyte * buffer_in;
    jrtp = getJavaRtp(env,thiz);

    if(jrtp == NULL)
    {
        LOG_LOCAL("write jrtp == NULL");
        jniThrowException(env, "java/lang/RuntimeException", "write ERROR jrtp == NULL");
        return;
    }
   
    seesion = jrtp->getCustomRTPSession();
    if(seesion == NULL)
    {
        LOG_LOCAL("openRtp seesion == NULL");
        jniThrowException(env, "java/lang/RuntimeException", "closeRtp ERROR seesion == NULL");
        return;
    }

   lenth=env->GetArrayLength(jar);
	buffer_in = env->GetByteArrayElements(jar, &iscopy);
	if(DEBUG)
	{
	    LOG_LOCAL("send data lenth %d",lenth);
	    
	    for(index = 0; index < lenth && index < 10; index++)
	    {
	        LOG_LOCAL("send data buffer_in[%d] 0x%x",index,buffer_in[index]);
	    }
	}
    if (lenth <= MAX_PACKAGE_SIZE)
    {
        status = seesion->SendPacket((void *)buffer_in,lenth,96,true,3600);
    }
    else
    {
        uint8_t * const tempin = (uint8_t * )buffer_in;
        uint8_t indicator;
        uint8_t header_start;
        uint8_t header;
        uint8_t header_end;
        int sum = 0;
        int writelenth = 0;
        int left = lenth-5;

		 uint8_t bufferout[1500];

        // Set FU-A indicator
        indicator = (uint8_t) (((tempin[4] & 0x60) & 0xFF)|0x1c); // FU indicator NRI
        header_start = (uint8_t) ((tempin[4] & 0x1F)|0x80);  // FU header type Start
        header = (uint8_t) (tempin[4] & 0x1F);
        header_end = (uint8_t) ((tempin[4] & 0x1F)|0x40); 

		if(DEBUG)
		{
		        LOG_LOCAL("send data large start tempin[4] 0x%x",tempin[4]);
		        LOG_LOCAL("send data large indicator:0x%x",indicator);
		        LOG_LOCAL("send data large header_start:0x%x",header_start);
		        LOG_LOCAL("send data large header:0x%x",header);
		        LOG_LOCAL("send data large header_end:0x%x",header_end);
		        LOG_LOCAL("send data large type:0x%x",tempin[4]&0x1f);
		}
        while(left > 0)
        {
            uint32_t templenth;

            if(left < MAX_PACKAGE_SIZE)
            {
                templenth = left+6;
                memcpy(bufferout,tempin,4);
                memcpy(bufferout+6,tempin+5+sum, left);
                sum+=left;
                left-= left;
            }
            else
            {
                templenth = MAX_PACKAGE_SIZE+6;
                memcpy(bufferout,tempin,4);
                memcpy(bufferout+6,tempin+5+sum, MAX_PACKAGE_SIZE);
                sum+=MAX_PACKAGE_SIZE;
                left-=MAX_PACKAGE_SIZE;
            }

            if(sum == MAX_PACKAGE_SIZE)//start
            {
                bufferout[4] = indicator;
                bufferout[5] = header_start;
                status = seesion->SendPacket((void *)bufferout,templenth,96,false,1800);
            }
            else if(left == 0)//end
            {
                bufferout[4] = indicator;
                bufferout[5] = header_end;
                status = seesion->SendPacket((void *)bufferout,templenth,96,false,1800);
            }
            else
            {
                bufferout[4] = indicator;
                bufferout[5] = header;
                status = seesion->SendPacket((void *)bufferout,templenth,96,false,1800);
            }
        }
	}
	env->ReleaseByteArrayElements(jar, buffer_in, 0);
    if(!checkerror(status))
    {
        return;
    }
}


JNIEXPORT void JNICALL Java_com_android_localcall_jni_Rtp_write___3S

(JNIEnv * env, jobject thiz, jshortArray jar)
{
    JavaRtp* jrtp;
    CustomSpeex * speex;
    CustomRTPSession * seesion;
    int framesize=0;
    int status;
    int lenth = 0;

    jrtp = getJavaRtp(env,thiz);

    if(jrtp == NULL)
    {
        LOG_LOCAL("write jrtp == NULL");
        jniThrowException(env, "java/lang/RuntimeException", "write ERROR jrtp == NULL");
        return;
    }
    speex = jrtp->getCustomSpeex();
    if(speex == NULL)
    {
        LOG_LOCAL("write speex == NULL");
        jniThrowException(env, "java/lang/RuntimeException", "write ERROR speex == NULL");
        return;
    }
    seesion = jrtp->getCustomRTPSession();
    if(seesion == NULL)
    {
        LOG_LOCAL("openRtp seesion == NULL");
        jniThrowException(env, "java/lang/RuntimeException", "closeRtp ERROR seesion == NULL");
        return;
    }

    framesize=speex->getFrameSize();
    lenth=env->GetArrayLength(jar);
    if(lenth != framesize)
    {
        LOG_LOCAL("write data lenth wrong lenth:%d framesize:%d",lenth,framesize);
        return;
    }

    {
        jshort * buf;
        uint8_t bufout[framesize];
        jboolean copyflag = false;
        buf = env->GetShortArrayElements(jar, &copyflag);
        lenth = speex->encode(buf, (char*)bufout,framesize);

        env->ReleaseShortArrayElements(jar, buf, framesize);
        if(lenth == -1)
        {
            LOG_LOCAL("write data  not speech");
            seesion->IncrementTimestampDefault();
        }
        else
        {
            LOG_LOCAL("write data  speech");
            status = seesion->SendPacket((void *)bufout,lenth,101,false,8000);
        }
    }

    if(!checkerror(status))
    {
        return;
    }
}

JNIEXPORT void JNICALL Java_com_android_localcall_jni_Rtp_addRtpDestinationIp
(JNIEnv * env, jobject thiz, jstring ip)
{
    JavaRtp* jrtp;
    CustomSpeex * speex;
    CustomRTPSession * seesion;

    jboolean flag = false;
    jchar const * ipchar = NULL;

    int lenth = 0;
    int i = 0;

    int status;
    char buf[50] ;
    std::string ipstr;
    uint32_t destip;

    jrtp = getJavaRtp(env,thiz);

    if(jrtp == NULL)
    {
        LOG_LOCAL("addRtpDestinationIp jrtp == NULL");
        jniThrowException(env, "java/lang/RuntimeException", "addRtpDestinationIp ERROR jrtp == NULL");
        return;
    }
    seesion = jrtp->getCustomRTPSession();
    if(seesion == NULL)
    {
        LOG_LOCAL("addRtpDestinationIp seesion == NULL");
        jniThrowException(env, "java/lang/RuntimeException", "addRtpDestinationIp ERROR seesion == NULL");
        return;
    }

    ipchar = env->GetStringChars(ip, &flag);
    lenth = env->GetStringLength(ip);
    memset(buf,0,sizeof(buf));
    for(i =0; i < lenth; i++)
    {
        buf[i] = (char)ipchar[i];
    }

    LOG_LOCAL("addRtpDestinationIp %s", (char *)buf);

    ipstr = std::string((char *)buf);
    destip = inet_addr(ipstr.c_str());
    destip = ntohl(destip);

    RTPIPv4Address addr(destip,jrtp->getPortBase());
    status = seesion->AddDestination(addr);
    if(!checkerror(status))
    {
        return;
    }
}



JNIEXPORT jint JNICALL Java_com_android_localcall_jni_Rtp_capture___3SI
(JNIEnv * env, jobject thiz, jshortArray in, jint size)
{
    JavaRtp* jrtp;
    jshort buf[size];

    CustomSpeex * speex;
    jrtp = getJavaRtp(env,thiz);
    if(jrtp == NULL)
    {
        LOG_LOCAL("capture jrtp == NULL");
        jniThrowException(env, "java/lang/RuntimeException", "capture ERROR jrtp == NULL");
        return 0;
    }
    speex = jrtp->getCustomSpeex();
    if(speex == NULL)
    {
        LOG_LOCAL("capture speex == NULL");
        jniThrowException(env, "java/lang/RuntimeException", "capture ERROR speex == NULL");
        return 0;
    }

    env->GetShortArrayRegion(in, 0, size, buf);
    speex->capture((short *) buf, size);
    env->SetShortArrayRegion(in, 0, size, buf);
    return 160;
}
JNIEXPORT jint JNICALL Java_com_android_localcall_jni_Rtp_playback___3SI
(JNIEnv * env, jobject thiz, jshortArray in, jint size)
{
    JavaRtp* jrtp;
    jshort buf[size];

    CustomSpeex * speex;
    jrtp = getJavaRtp(env,thiz);
    if(jrtp == NULL)
    {
        LOG_LOCAL("playback jrtp == NULL");
        jniThrowException(env, "java/lang/RuntimeException", "playback ERROR jrtp == NULL");
        return 0;
    }

    speex = jrtp->getCustomSpeex();
    if(speex == NULL)
    {
        LOG_LOCAL("playback speex == NULL");
        jniThrowException(env, "java/lang/RuntimeException", "playback ERROR speex == NULL");
        return 0;
    }
    env->GetShortArrayRegion(in, 0, size, buf);
    speex->playback(buf, size);
    env->SetShortArrayRegion(in, 0, size, buf);
    return 160;
}


