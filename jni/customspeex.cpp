
#include "customspeex.h"

#include <android/log.h>
#include <unistd.h>


const char* const LOG_TAG = "SPEEX_LOG";

CustomSpeex::CustomSpeex()
{
    open();
}
CustomSpeex::~CustomSpeex()
{
    close();
}
bool CustomSpeex::open()
{
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "CustomSpeex open");

    int tmp = 4;
    float f;
    int i;
    speex_bits_init(&ebits);
    speex_bits_init(&dbits);

    enc_state = speex_encoder_init(&speex_nb_mode);
    dec_state = speex_decoder_init(&speex_nb_mode);

    speex_encoder_ctl(enc_state, SPEEX_SET_QUALITY, &tmp);
    speex_encoder_ctl(enc_state, SPEEX_GET_FRAME_SIZE, &enc_frame_size);

    speex_decoder_ctl(dec_state, SPEEX_GET_FRAME_SIZE, &dec_frame_size);

    echo_state = speex_echo_state_init(160, 160*15);

    m_st=speex_preprocess_state_init(160, 8000);
    int sampleRate = 8000;
    speex_echo_ctl(echo_state, SPEEX_ECHO_SET_SAMPLING_RATE, &sampleRate);
    speex_preprocess_ctl(m_st, SPEEX_PREPROCESS_SET_ECHO_STATE, echo_state);
	/*
    i=0;
    speex_preprocess_ctl(m_st, SPEEX_PREPROCESS_SET_AGC, &i);
    i=8000;
    speex_preprocess_ctl(m_st, SPEEX_PREPROCESS_SET_AGC_LEVEL, &i);
    i=0;
    speex_preprocess_ctl(m_st, SPEEX_PREPROCESS_SET_DEREVERB, &i);
    f=.0;
    speex_preprocess_ctl(m_st, SPEEX_PREPROCESS_SET_DEREVERB_DECAY, &f);
    f=.0;
    speex_preprocess_ctl(m_st, SPEEX_PREPROCESS_SET_DEREVERB_LEVEL, &f);
*/
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


    return true;
}
void CustomSpeex::close()
{
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "CustomSpeex close");
    speex_bits_destroy(&ebits);
    speex_bits_destroy(&dbits);
    speex_decoder_destroy(dec_state);
    speex_encoder_destroy(enc_state);
    speex_preprocess_state_destroy(m_st);
    speex_echo_state_destroy(echo_state);
}
int CustomSpeex::getFrameSize()
{
    return enc_frame_size;
}
int CustomSpeex::encode(short  * in,  char  *  out, int size)
{
    int ret = 0;
    int vad;

    if(in == NULL || out == NULL || size <= 0 || size != enc_frame_size)
    {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "CustomSpeex encode  data error");
        return ret;
    }
    vad = speex_preprocess_run(m_st, in);
    if(vad == 0)
    {
        return -1;
    }

    speex_bits_reset(&ebits);
    speex_encode_int(enc_state, in, &ebits);
    ret = speex_bits_write(&ebits, (char *)out,enc_frame_size);
    return ret;

}
int CustomSpeex::decode(char * in, short  *  out, int size)
{
    int ret = 0;

    if(in == NULL || out == NULL || size <= 0 || size != enc_frame_size)
    {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "CustomSpeex decode  data error");
        return ret;
    }

    speex_bits_reset(&dbits);
    speex_bits_read_from(&dbits, in, dec_frame_size);
    ret = speex_decode_int(dec_state, &dbits, out);
    return ret;
}

int CustomSpeex::capture( short * out, int size)
{
    short rec_buffer[160];
    memset(rec_buffer, 0, 160*2);
    //speex_preprocess_run(m_st, (spx_int16_t*)out);
    speex_echo_capture(echo_state, (spx_int16_t*)out, (spx_int16_t*)rec_buffer);
    //speex_preprocess_run(m_st, (spx_int16_t*)rec_buffer);
    memcpy(out, rec_buffer, 160*2);
    return 160;
}

int CustomSpeex::playback( short * out, int size)
{
    speex_echo_playback(echo_state, (spx_int16_t*)out);
    return 160;
}