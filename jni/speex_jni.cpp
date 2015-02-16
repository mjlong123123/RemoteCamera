#include <jni.h>

#include <string.h>
#include <unistd.h>

#include <speex/speex.h>
#include <speex/speex_preprocess.h>
#include <speex/speex_echo.h>

static int codec_open = 0;

static int dec_frame_size;
static int enc_frame_size;

static SpeexBits ebits, dbits;

void *enc_state;
void *dec_state;

SpeexPreprocessState * m_st;
SpeexEchoState *echo_state;

extern "C"
JNIEXPORT jint JNICALL Java_com_android_dlvp_jni_Speex_open
(JNIEnv *env, jobject obj, jint compression)
{
    int tmp;

    if (codec_open++ != 0)
        return (jint)0;

    speex_bits_init(&ebits);
    speex_bits_init(&dbits);

    enc_state = speex_encoder_init(&speex_nb_mode);
    dec_state = speex_decoder_init(&speex_nb_mode);

    tmp = compression;

    speex_encoder_ctl(enc_state, SPEEX_SET_QUALITY, &tmp);
    speex_encoder_ctl(enc_state, SPEEX_GET_FRAME_SIZE, &enc_frame_size);

    speex_decoder_ctl(dec_state, SPEEX_GET_FRAME_SIZE, &dec_frame_size);

    m_st=speex_preprocess_state_init(160, 8000);

    int denoise = 1;
    int noiseSuppress = -25;
    speex_preprocess_ctl(m_st, SPEEX_PREPROCESS_SET_DENOISE, &denoise);
    speex_preprocess_ctl(m_st, SPEEX_PREPROCESS_SET_NOISE_SUPPRESS, &noiseSuppress);
    /*
        //louder hight
        int agc = 1;
        float qq=24000;
        speex_preprocess_ctl(m_st, SPEEX_PREPROCESS_SET_AGC, &agc);
        speex_preprocess_ctl(m_st, SPEEX_PREPROCESS_SET_AGC_LEVEL,&qq);
    */
    int vad = 1;
    int vadProbStart = 80;
    int vadProbContinue = 65;
    speex_preprocess_ctl(m_st, SPEEX_PREPROCESS_SET_VAD, &vad);
    speex_preprocess_ctl(m_st, SPEEX_PREPROCESS_SET_PROB_START , &vadProbStart);
    speex_preprocess_ctl(m_st, SPEEX_PREPROCESS_SET_PROB_CONTINUE, &vadProbContinue);

    return (jint)0;
}

extern "C"
JNIEXPORT jint Java_com_android_dlvp_jni_Speex_encode
(JNIEnv *env, jobject obj, jshortArray lin, jint offset, jbyteArray encoded, jint size)
{

    jshort buffer[enc_frame_size];
    jbyte output_buffer[enc_frame_size];
    int nsamples = (size-1)/enc_frame_size + 1;
    int i, tot_bytes = 0;

    short input[enc_frame_size];

    if (!codec_open)
        return 0;

    if(size != enc_frame_size)
        return 0;
    /*
        for (i = 0; i < nsamples; i++)
        {
            env->GetShortArrayRegion(lin, offset + i*enc_frame_size, enc_frame_size, buffer);
            speex_bits_reset(&ebits);
            speex_encode_int(enc_state, buffer, &ebits);
        }
        */

    env->GetShortArrayRegion(lin, offset, enc_frame_size, buffer);


    speex_bits_reset(&ebits);
    speex_encode_int(enc_state, buffer, &ebits);

    tot_bytes = speex_bits_write(&ebits, (char *)output_buffer,
                                 enc_frame_size);
    env->SetByteArrayRegion(encoded, 0, tot_bytes,
                            output_buffer);

    return (jint)tot_bytes;
}

extern "C"
JNIEXPORT jint Java_com_android_dlvp_jni_Speex_encodeExt
(JNIEnv *env, jobject obj, jshortArray lin, jint offset, jbyteArray encoded, jint size)
{

    jshort buffer[enc_frame_size];
    jbyte output_buffer[enc_frame_size];
    int nsamples = (size-1)/enc_frame_size + 1;
    int i, tot_bytes = 0;

    float input[enc_frame_size];

    if (!codec_open)
        return 0;

    for (i = 0; i < nsamples; i++)
    {
        env->GetShortArrayRegion(lin, offset + i*enc_frame_size, enc_frame_size, buffer);
        spx_int16_t * ptr=(spx_int16_t *)buffer;
        if(speex_preprocess_run(m_st, ptr))
        {
            for (i=0; i<enc_frame_size; i++)

                input[i]=buffer[i];
        }
        else
        {
            return 0;
        }


        speex_bits_reset(&ebits);
        speex_encode(enc_state, input, &ebits);
    }
    //env->GetShortArrayRegion(lin, offset, enc_frame_size, buffer);
    //speex_encode_int(enc_state, buffer, &ebits);

    tot_bytes = speex_bits_write(&ebits, (char *)output_buffer,
                                 enc_frame_size);
    env->SetByteArrayRegion(encoded, 0, tot_bytes,
                            output_buffer);

    return (jint)tot_bytes;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_android_dlvp_jni_Speex_decode
(JNIEnv *env, jobject obj, jbyteArray encoded, jshortArray lin, jint size)
{

    int i = 0;
    jbyte buffer[dec_frame_size];
    jshort output_buffer[dec_frame_size];
    jsize encoded_length = size;

    short output[dec_frame_size];
    if (!codec_open)
        return 0;
    env->GetByteArrayRegion(encoded, 0, encoded_length, buffer);
    speex_bits_read_from(&dbits, (char *)buffer, encoded_length);
    speex_decode_int(dec_state, &dbits, output);
    for (i=0; i<dec_frame_size; i++)
        output_buffer[i]=output[i];
    env->SetShortArrayRegion(lin, 0, dec_frame_size,
                             output_buffer);
    return (jint)dec_frame_size;
}


extern "C"
JNIEXPORT jint JNICALL Java_com_android_dlvp_jni_Speex_decodeExt
(JNIEnv *env, jobject obj, jbyteArray encoded, jshortArray lin, jint size)
{

    int i = 0;
    jbyte buffer[dec_frame_size];
    jshort output_buffer[dec_frame_size];
    jsize encoded_length = size;

    float output[dec_frame_size];
    if (!codec_open)
        return 0;
    env->GetByteArrayRegion(encoded, 0, encoded_length, buffer);
    speex_bits_read_from(&dbits, (char *)buffer, encoded_length);
    speex_decode(dec_state, &dbits, output);
    for (i=0; i<dec_frame_size; i++)
        output_buffer[i]=output[i];
    env->SetShortArrayRegion(lin, 0, dec_frame_size,
                             output_buffer);
    return (jint)dec_frame_size;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_android_dlvp_jni_Speex_getFrameSize
(JNIEnv *env, jobject obj)
{

    if (!codec_open)
        return 0;
    return (jint)enc_frame_size;

}

extern "C"
JNIEXPORT void JNICALL Java_com_android_dlvp_jni_Speex_close
(JNIEnv *env, jobject obj)
{

    if (--codec_open != 0)
        return;

    speex_bits_destroy(&ebits);
    speex_bits_destroy(&dbits);
    speex_decoder_destroy(dec_state);
    speex_encoder_destroy(enc_state);
    speex_preprocess_state_destroy(m_st);
}
