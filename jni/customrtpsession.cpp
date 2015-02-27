
#include <jni.h>
#include <android/log.h>
#include "customrtpsession.h"
#include "rtpudpv4transmitter.h"
#include "rtpipv4address.h"
#include "rtperrors.h"
#include "customrtp.h"
#include "define.h"

const char* const LOG_TAG = "RTP_SESSION";

void CustomRTPSession::OnPollThreadStep()
{
    BeginDataAccess();

    // check incoming packets
    if (GotoFirstSourceWithData())
    {
        do
        {
            RTPPacket *pack;
            RTPSourceData *srcdat;

            srcdat = GetCurrentSourceInfo();

            while ((pack = GetNextPacket()) != NULL)
            {
                ProcessRTPPacket(*srcdat,*pack);
                DeletePacket(pack);
            }
        }
        while (GotoNextSourceWithData());
    }

    EndDataAccess();
}

void CustomRTPSession::ProcessRTPPacket(const RTPSourceData &srcdat,const RTPPacket &rtppack)
{
    // You can inspect the packet and the source's info here
    int index = 0;
    uint32_t ip;
    uint16_t port;
    uint8_t  * buffer = NULL;
    short buffer11[160];
    size_t size = 0;
    uint8_t type = 0;
    uint32_t Sequencenumber = 0;
    struct in_addr inaddr;
    bool mark = false;
    uint32_t timestamp;
	if(DEBUG)
	{
	    if (srcdat.GetRTPDataAddress() != 0)
	    {
	        const RTPIPv4Address *addr = (const RTPIPv4Address *)(srcdat.GetRTPDataAddress());
	        ip = addr->GetIP();
	        port = addr->GetPort();
	        inaddr.s_addr = htonl(ip);
	        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "ProcessRTPPacket ip %s", inet_ntoa(inaddr));
	        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "ProcessRTPPacket port %d", port);
	    }
	    else if (srcdat.GetRTCPDataAddress() != 0)
	    {
	        const RTPIPv4Address *addr = (const RTPIPv4Address *)(srcdat.GetRTCPDataAddress());
	        ip = addr->GetIP();
	        port = addr->GetPort()-1;
	        inaddr.s_addr = htonl(ip);
	        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "ProcessRTCPPacket ip %s", inet_ntoa(inaddr));
	        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "ProcessRTCPPacket port %d", port);
	    }
	}
    buffer = rtppack.GetPayloadData();
    size = rtppack.GetPayloadLength();
    type = rtppack.GetPayloadType();
    Sequencenumber = rtppack.GetSequenceNumber();
    mark = rtppack.HasMarker();
    timestamp =rtppack.GetTimestamp();
	if(DEBUG)
	{
	    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "receive data type %d",type);
	    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "receive data size : %d",size);
	    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "HasMarker  %d",mark);
	    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "ProcessRTPPacket GetTimestamp  %d",timestamp);
	    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "ProcessRTPPacket GetSequenceNumber  %d",Sequencenumber);
	    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "ProcessRTPPacket sequence_number  %d",sequence_number);

	    if(Sequencenumber != (sequence_number+1))
	        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "ProcessRTPPacket lost packet ---------------------------------------");
	}
	
    if(mJavaRtp != NULL)
    {
    	if(type == 101)
    	{
	        CustomSpeex * speex;
	        speex = mJavaRtp->getCustomSpeex();
	        if(speex != NULL)
	        {
	            speex->decode((char *)buffer, buffer11, speex->getFrameSize());
	            mJavaRtp->callJavaCallBack(0,buffer11,speex->getFrameSize());
	        }
			
	        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "speex : %d",(int)speex);
	    }
		else if(type == 96)
		{
				if(DEBUG)
				{
	            for(index = 0; ((index < size) && (index < 10)); index++)
	            {
	                __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "receive data[%d] 0x%x",index,buffer[index]);
	            }
	            if(mark)
	            {
	                if(((buffer[4]&0x1f) == 5))
	                __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "receive key frame ");
	            }
	            else
	            {
	                if(((buffer[5]&0x1f) == 5))
	                __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "receive key frame ");
	            }
				}
				
            if(size == 30)
            {
                if(buffer[29] == 1 &&
                buffer[28] == 0&&
                buffer[27] == 1&&
                buffer[26] == 0&&
                buffer[25] == 1&&
                buffer[24] == 0)
                {
                    sequence_number = Sequencenumber-1;
                    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "receive init sequence_number data  ");
                }
            }

            if((sequence_number+1) != Sequencenumber)
            {
                __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "lost packet-------");
            
                if(sequence_number != 0)
                {
                    if(mark)
                    {
                        if(((buffer[4]&0x1f) != 5))
                        {
                            resetPlayloadBuffer();
                            __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "lost packet----not key frame ---");
                            return;
                        }
                    }
                    else
                    {
                        if(((buffer[5]&0x1f) != 5))
                        {
                            resetPlayloadBuffer();
                            __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "lost packet----not key frame large---");
                            return;
                        }
                        else
                        {
                            if((buffer[5]&0xe0) != 0x80)
                            {
                                resetPlayloadBuffer();
                                __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "lost packet----not key frame large begin---");
                                return;
                            }
                        }
                    }

                    sequence_number = Sequencenumber;
                }
                else
                {
                    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "lost packet----init---");
                    sequence_number = Sequencenumber;
                }
            }
            sequence_number = Sequencenumber;
            if(mark)
            {
                mJavaRtp->callJavaCallBack(0,(char *)buffer,size);
            }
            else
            {
                uint8_t * outbuffer;
                uint32_t outsize;
                if(addDataToBuffer(buffer,size,&outbuffer,&outsize,Sequencenumber,timestamp))
                {
                    mJavaRtp->callJavaCallBack(0,(char *)outbuffer,outsize);
                }
            }

		}
    }
    else
    {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "mJavaRtp is null");
    }

    if(rtppack.HasExtension())
    {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "ExtendedSequenceNumber %d",rtppack.GetExtendedSequenceNumber());
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "ExtensionID %d",rtppack.GetExtensionID());
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "ExtensionLength %d",rtppack.GetExtensionLength());
        buffer = rtppack.GetExtensionData();

        for(index = 0; index < size && buffer != NULL && index < 10; index++)
        {
            __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "Extension buffer[%d] 0x%x",index, buffer[index]);
        }
    }
}

bool CustomRTPSession::addDataToBuffer(uint8_t * in, uint32_t size, uint8_t ** pOut, uint32_t *outsize, uint32_t seq, uint32_t tim)
{
    bool ret = false;
    int index = 0;

    if(in == NULL || size <= 6)
    {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "addDataToBuffer in %x size %d",in,size);
        return ret;
    }

    if(in[0] != 0 || in[1] != 0 || in[2] != 0 || in[3] != 1)
    {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "addDataToBuffer data error1");
        return ret;
    }
       
    if((in[4] & 0x1f) == 0x1c)
    {
			if(DEBUG)
			{
		       __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "addDataToBuffer seq:%d",seq);
		       __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "addDataToBuffer timestamp:%d",timestamp);
		       __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "addDataToBuffer tim:%d",tim);
			}
        if(((in[5] & 0xe0) == 0x80))//start
        {
				if(DEBUG)
				{
       			__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "addDataToBuffer data fu-a slice start");
       			__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "addDataToBuffer data before position:%d",position);
       			__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "addDataToBuffer data before size:%d",size);
				}
				//clear old data.
            resetPlayloadBuffer();
            timestamp = tim;
            memcpy(playload_buffer,in,4);
            playload_buffer[4] = (in[4] & 0xe0) | (in[5] & 0x1f);
            memcpy(playload_buffer+5,in+6,size-6);
            if(DEBUG)
            {
	            for(index = 0; ((index < size-6) && (index < 10)); index++)
	            {
	                __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "check data[%d] 0x%x",index,playload_buffer[index]);
	            }
            }
            position = (size-1);
        }
        else if(((in[5] & 0xe0) == 0x40))//end
        {
				if(DEBUG)
				{
       			__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "addDataToBuffer data fu-a slice end");
       			__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "addDataToBuffer data before position:%d",position);
      				 __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "addDataToBuffer data before size:%d",size);
				}
            {
                memcpy(playload_buffer+position,in+6,size - 6);
					if(DEBUG)
					{
			            for(index = 0; ((index < size-6) && (index < 10)); index++)
			            {
			                __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "check data[%d] 0x%x",index+position,playload_buffer[index+position]);
			            }
					}
                position += (size-6);
                *outsize = position;
                *pOut = playload_buffer;
                if(DEBUG)
       					__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "addDataToBuffer data size:%d",position);
                ret = true;
            }
        }
        else
        {
					if(DEBUG)
					{
	       			__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "addDataToBuffer data fu-a slice middle");
	       			__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "addDataToBuffer data before position:%d",position);
	       			__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "addDataToBuffer data before size:%d",size);
					}
					{
                memcpy(playload_buffer+position,in+6,size - 6);
					if(DEBUG)
					{
	                for(index = 0; ((index < size-6) && (index < 10)); index++)
	            		{
	               		__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "check data[%d] 0x%x",index+position,playload_buffer[index+position]);
	            		}
					}
                position += (size-6);

            }
        }
    }
	else
	{
       __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "addDataToBuffer data error2");
	}
	
	if(DEBUG)
       __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "addDataToBuffer data after position:%d",position);

    return ret;
}

