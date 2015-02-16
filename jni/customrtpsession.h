#ifndef _CUSTOM_RTP_SESSION
#define _CUSTOM_RTP_SESSION

#include "rtpsession.h"
#include "rtppacket.h"
#include "rtpsourcedata.h"

class JavaRtp;

class CustomRTPSession : public RTPSession
{
    protected:
     uint8_t playload_buffer[1000*10];
     uint32_t sequence_number;
     uint32_t timestamp;
     uint32_t position;
     bool isLostPacket;
public:
    CustomRTPSession(JavaRtp * jrtp):sequence_number(0),timestamp(0),position(0),isLostPacket(false)
    {
        mJavaRtp = jrtp;
    };
    CustomRTPSession() {};
    ~CustomRTPSession()
    {
        mJavaRtp = NULL;
    };

protected:
    void OnPollThreadStep();
    void ProcessRTPPacket(const RTPSourceData &srcdat,const RTPPacket &rtppack);
    JavaRtp * mJavaRtp;
    void resetPlayloadBuffer(){timestamp = 0;position = 0;};
    bool addDataToBuffer(uint8_t * in, uint32_t size, uint8_t ** pOut, uint32_t *outsize, uint32_t seq, uint32_t tim);
};
//memset(playload_buffer,sizeof(uint8_t),0);
#endif
