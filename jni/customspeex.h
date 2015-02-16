#ifndef _CUSTOM_SPEEX_H
#define _CUSTOM_SPEEX_H

#include <speex/speex.h>
#include <speex/speex_preprocess.h>
#include <speex/speex_echo.h>

class CustomSpeex
{
public:

    CustomSpeex();
    virtual ~CustomSpeex();
    int getFrameSize();
    int encode(short * in,  char * out, int size);
    int decode(char * in, short * out, int size);
    int capture( short * out, int size);
    int playback(short * out, int size);
private:
    bool open();
    void close();
    int dec_frame_size;
    int enc_frame_size;

    SpeexBits ebits, dbits;

    void *enc_state;
    void *dec_state;
	
    SpeexPreprocessState * m_st;
    SpeexEchoState *echo_state;
};

#endif