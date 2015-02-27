#ifndef _CUSTOM_RTP_H
#define _CUSTOM_RTP_H
#include <jni.h>
#include "customrtpsession.h"
#include "customspeex.h"
class JavaRtp
{
public:
    JavaRtp(JNIEnv* env, jobject & thiz, jobject & weak_ref,jmethodID callbackid,jmethodID callbackid1);
    ~JavaRtp();
    void callJavaCallBack(int returncode, short * buffer, int size);
    void callJavaCallBack(int returncode, char * buffer, int size);
    inline CustomSpeex * getCustomSpeex(){return mSpeexRtp;};
    inline CustomRTPSession * getCustomRTPSession(){return mMyRTPSession;};
	inline bool isOpen(){return mIsOpen;};
	inline void setIsOpen(bool flag){mIsOpen = flag;};
	inline int getFrameSize(){return mFrameSize;};
	inline void setFrameSize(int size){mFrameSize = size;};
	inline uint16_t getPortBase(){return mPortBase;};
	inline void setPortBase(uint16_t in){mPortBase = in;};
private:
    JavaVM * mJavaVM;
	
    jclass      mClass;     // Reference to Rtp class
    jobject     mObject;    // Weak ref to Rtp Java object to call on
    CustomRTPSession * mMyRTPSession;//for rtp transport
    CustomSpeex * mSpeexRtp;//for speex encode.
    jmethodID   mCallback;
    jmethodID   mCallback1;

    bool mIsOpen;
    int mFrameSize;
    uint16_t mPortBase;
};

#endif