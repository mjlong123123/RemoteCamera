/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_android_localcall_jni_Rtp */

#ifndef _Included_com_android_localcall_jni_Rtp
#define _Included_com_android_localcall_jni_Rtp
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_android_localcall_jni_Rtp
 * Method:    rtptransport
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_android_localcall_jni_Rtp_rtptransport
  (JNIEnv *, jobject);

/*
 * Class:     com_android_localcall_jni_Rtp
 * Method:    rtptransportsend
 * Signature: ([BI)V
 */
JNIEXPORT void JNICALL Java_com_android_localcall_jni_Rtp_rtptransportsend
  (JNIEnv *, jobject, jbyteArray, jint);

/*
 * Class:     com_android_localcall_jni_Rtp
 * Method:    rtptransportreceive
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_android_localcall_jni_Rtp_rtptransportreceive
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_android_localcall_jni_Rtp
 * Method:    native_init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_android_localcall_jni_Rtp_native_1init
  (JNIEnv *, jclass);

/*
 * Class:     com_android_localcall_jni_Rtp
 * Method:    native_setup
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_com_android_localcall_jni_Rtp_native_1setup
  (JNIEnv *, jobject, jobject);

/*
 * Class:     com_android_localcall_jni_Rtp
 * Method:    native_rease
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_android_localcall_jni_Rtp_native_1rease
  (JNIEnv *, jobject);

/*
 * Class:     com_android_localcall_jni_Rtp
 * Method:    openRtp
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_android_localcall_jni_Rtp_openRtp
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_android_localcall_jni_Rtp
 * Method:    closeRtp
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_android_localcall_jni_Rtp_closeRtp
  (JNIEnv *, jobject);

/*
 * Class:     com_android_localcall_jni_Rtp
 * Method:    addRtpDestinationIp
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_android_localcall_jni_Rtp_addRtpDestinationIp
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_android_localcall_jni_Rtp
 * Method:    delRtpDestinationIp
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_android_localcall_jni_Rtp_delRtpDestinationIp
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_android_localcall_jni_Rtp
 * Method:    getFrameSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_android_localcall_jni_Rtp_getFrameSize
  (JNIEnv *, jobject);

/*
 * Class:     com_android_localcall_jni_Rtp
 * Method:    write
 * Signature: ([S)V
 */
JNIEXPORT void JNICALL Java_com_android_localcall_jni_Rtp_write___3S
  (JNIEnv *, jobject, jshortArray);

/*
 * Class:     com_android_localcall_jni_Rtp
 * Method:    write
 * Signature: ([S)V
 */
JNIEXPORT void JNICALL Java_com_android_localcall_jni_Rtp_write___3BZ
  (JNIEnv *, jobject, jbyteArray, jboolean);

/*
 * Class:     com_android_localcall_jni_Rtp
 * Method:    write
 * Signature: ([SI)V
 */
JNIEXPORT void JNICALL Java_com_android_localcall_jni_Rtp_write___3SI
  (JNIEnv *, jobject, jshortArray, jint);

JNIEXPORT jint JNICALL Java_com_android_localcall_jni_Rtp_capture___3SI
  (JNIEnv *, jobject, jshortArray, jint);
JNIEXPORT jint JNICALL Java_com_android_localcall_jni_Rtp_playback___3SI
  (JNIEnv *, jobject, jshortArray, jint);
/*
 * Class:     com_android_localcall_jni_Rtp
 * Method:    read
 * Signature: ()[S
 */
JNIEXPORT jshortArray JNICALL Java_com_android_localcall_jni_Rtp_read
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
