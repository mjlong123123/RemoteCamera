/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
#include <stdlib.h>
#include <android/log.h>
#include "com_android_ffmpeglib_H264Decoder.h"

#include "libavcodec/avcodec.h"
#include "libavutil/mathematics.h"
#include "define.h"

#define TAG "H264Decoder"

static AVCodec *codec;
static AVCodec *codec_encode;
static AVCodecContext *context = NULL;
static AVFrame *picture;
static AVPacket avpkt;

int iWidth=0;
int iHeight=0;
	
int *colortab=NULL;
int *u_b_tab=NULL;
int *u_g_tab=NULL;
int *v_g_tab=NULL;
int *v_r_tab=NULL;

//short *tmp_pic=NULL;

unsigned int *rgb_2_pix=NULL;
unsigned int *r_2_pix=NULL;
unsigned int *g_2_pix=NULL;
unsigned int *b_2_pix=NULL;
		
void DeleteYUVTab()
{
//	av_free(tmp_pic);
	
	av_free(colortab);
	av_free(rgb_2_pix);
}

void CreateYUVTab_16()
{
	int i;
	int u, v;
	
//	tmp_pic = (short*)av_malloc(iWidth*iHeight*2); // ???? iWidth * iHeight * 16bits

	colortab = (int *)av_malloc(4*256*sizeof(int));
	u_b_tab = &colortab[0*256];
	u_g_tab = &colortab[1*256];
	v_g_tab = &colortab[2*256];
	v_r_tab = &colortab[3*256];

	for (i=0; i<256; i++)
	{
		u = v = (i-128);

		u_b_tab[i] = (int) ( 1.772 * u);
		u_g_tab[i] = (int) ( 0.34414 * u);
		v_g_tab[i] = (int) ( 0.71414 * v); 
		v_r_tab[i] = (int) ( 1.402 * v);
	}

	rgb_2_pix = (unsigned int *)av_malloc(3*768*sizeof(unsigned int));

	r_2_pix = &rgb_2_pix[0*768];
	g_2_pix = &rgb_2_pix[1*768];
	b_2_pix = &rgb_2_pix[2*768];

	for(i=0; i<256; i++)
	{
		r_2_pix[i] = 0;
		g_2_pix[i] = 0;
		b_2_pix[i] = 0;
	}

	for(i=0; i<256; i++)
	{
		r_2_pix[i+256] = (i & 0xF8) << 8;
		g_2_pix[i+256] = (i & 0xFC) << 3;
		b_2_pix[i+256] = (i ) >> 3;
	}

	for(i=0; i<256; i++)
	{
		r_2_pix[i+512] = 0xF8 << 8;
		g_2_pix[i+512] = 0xFC << 3;
		b_2_pix[i+512] = 0x1F;
	}

	r_2_pix += 256;
	g_2_pix += 256;
	b_2_pix += 256;
}

void DisplayYUV_16(unsigned int *pdst1, unsigned char *y, unsigned char *u, unsigned char *v, int width, int height, int src_ystride, int src_uvstride, int dst_ystride)
{
	int i, j;
	int r, g, b, rgb;

	int yy, ub, ug, vg, vr;

	unsigned char* yoff;
	unsigned char* uoff;
	unsigned char* voff;
	
	unsigned int* pdst=pdst1;

	int width2 = width/2;
	int height2 = height/2;
	
	if(width2>iWidth/2)
	{
		width2=iWidth/2;

		y+=(width-iWidth)/4*2;
		u+=(width-iWidth)/4;
		v+=(width-iWidth)/4;
	}

	if(height2>iHeight)
		height2=iHeight;

	for(j=0; j<height2; j++) // ???2x2?????????
	{
		yoff = y + j * 2 * src_ystride;
		uoff = u + j * src_uvstride;
		voff = v + j * src_uvstride;

		for(i=0; i<width2; i++)
		{
			yy  = *(yoff+(i<<1));
			ub = u_b_tab[*(uoff+i)];
			ug = u_g_tab[*(uoff+i)];
			vg = v_g_tab[*(voff+i)];
			vr = v_r_tab[*(voff+i)];

			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			rgb = r_2_pix[r] + g_2_pix[g] + b_2_pix[b];

			yy = *(yoff+(i<<1)+1);
			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			pdst[(j*dst_ystride+i)] = (rgb)+((r_2_pix[r] + g_2_pix[g] + b_2_pix[b])<<16);

			yy = *(yoff+(i<<1)+src_ystride);
			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			rgb = r_2_pix[r] + g_2_pix[g] + b_2_pix[b];

			yy = *(yoff+(i<<1)+src_ystride+1);
			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			pdst [((2*j+1)*dst_ystride+i*2)>>1] = (rgb)+((r_2_pix[r] + g_2_pix[g] + b_2_pix[b])<<16);
		}
	}
}

/*
 * Class:     com_android_ffmpeglib_H264Decoder
 * Method:    init
 * Signature: (II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_android_ffmpeglib_H264Decoder_init(
		JNIEnv * en, jobject jh264, jint jw, jint jh) {
		
	iWidth = jw;
	iHeight = jh;
  
		CreateYUVTab_16();
	/* init packet memory*/
	av_init_packet(&avpkt);
    /* must be called before using avcodec lib */
    avcodec_init();

    /* register all the codecs */
    avcodec_register_all();


/*
	codec_encode = avcodec_find_encoder(CODEC_ID_H264);
	if (!codec_encode) {
		__android_log_print(ANDROID_LOG_ERROR, TAG,
				"H264Decoder init  find encoder error %d\n", (int) codec_encode);
		exit(1);
	}
*/
	/* find the mpeg1 video decoder */
	codec = avcodec_find_decoder(CODEC_ID_H264);
	if (!codec) {
		__android_log_print(ANDROID_LOG_ERROR, TAG,
				"H264Decoder init  find error %d\n", (int) codec);
		exit(1);
	}

	/*init decoder context*/
	context = avcodec_alloc_context();
	/*malloc frame memory*/
	picture = avcodec_alloc_frame();

	if (codec->capabilities & CODEC_CAP_TRUNCATED)
		context->flags |= CODEC_FLAG_TRUNCATED; /* we do not send complete frames */

	/* For some codecs, such as msmpeg4 and mpeg4, width and height
	 MUST be initialized there because this information is not
	 available in the bitstream. */

	/* open it */
	if (avcodec_open(context, codec) < 0) {
		__android_log_print(ANDROID_LOG_ERROR, TAG,
				"H264Decoder init open error %d\n", (int) codec);
		exit(1);
	}

	/* put sample parameters */
    context->bit_rate = 400000;
    /* resolution must be a multiple of two */
    context->width = iWidth;
    context->height = iHeight;
    /* frames per second */
    context->time_base= (AVRational){1,25};
    context->gop_size = 10; /* emit one intra frame every ten frames */
    context->max_b_frames=1;
    context->pix_fmt = PIX_FMT_YUV420P;
    /* open it 
    if (avcodec_open(context, codec_encode) < 0) {
        fprintf(stderr, "could not open codec_encode\n");
        exit(1);
    }
*/
  	context->flags2|=CODEC_FLAG2_CHUNKS;
	return 0;
}

/*
 * Class:     com_android_ffmpeglib_H264Decoder
 * Method:    release
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_android_ffmpeglib_H264Decoder_release
(JNIEnv * en, jobject jh264)
{
    avcodec_close(context);
    av_free(context);
    av_free(picture);
    DeleteYUVTab();
}

/*
 * Class:     com_android_ffmpeglib_H264Decoder
 * Method:    decode
 * Signature: ([B[B)Z
 */
JNIEXPORT jboolean JNICALL Java_com_android_ffmpeglib_H264Decoder_decode(
		JNIEnv * en, jobject jh264, jbyteArray jin, jbyteArray jout) {
    int len = 0;
    int got_picture = 0;
    int index = 0;

	jbyte * Pixel= (jbyte*)(*en)->GetByteArrayElements(en, jout, 0);
    jbyte * inbuf = (* en)->GetByteArrayElements(en,jin,0);
   int arrsize = (*en)->GetArrayLength(en,jin);
    if(DEBUG)
    {
		__android_log_print(ANDROID_LOG_ERROR, TAG,
				"H264Decoder decode arrsize %d\n",arrsize);
    }
//    for(index = 0; index < arrsize; index++)
 //   {
	//	__android_log_print(ANDROID_LOG_ERROR, TAG,
	//			"H264Decoder decode inbuf[%d] %d\n",index,inbuf[index]);
  //  }
	avpkt.size = arrsize;
	     

	        /* NOTE1: some codecs are stream based (mpegvideo, mpegaudio)
	           and this is the only method to use them because you cannot
	           know the compressed data size before analysing it.

	           BUT some other codecs (msmpeg4, mpeg4) are inherently frame
	           based, so you must call them with all the data for one
	           frame exactly. You must also initialize 'width' and
	           'height' before initializing them. */

	        /* NOTE2: some codecs allow the raw parameters (frame size,
	           sample rate) to be changed at any frame. We handle this, so
	           you should also take care of it */

	        /* here, we use a stream based decoder (mpeg1video), so we
	           feed decoder and see if it could decode a frame */
	        avpkt.data = inbuf;
	        while (avpkt.size > 0) {
	            len = avcodec_decode_video2(context, picture, &got_picture, &avpkt);
              if(DEBUG)
              {
              		__android_log_print(ANDROID_LOG_ERROR, TAG,
              				"H264Decoder decode len %d\n",len);
              }
	            if (len < 0) {
		__android_log_print(ANDROID_LOG_ERROR, TAG,
				"H264Decoder decode error\n");
	                exit(1);
	            }
	            if (got_picture) {
               if(DEBUG)
               {
            		__android_log_print(ANDROID_LOG_ERROR, TAG,
            				"H264Decoder get frame data 0 %d data 1 %d data 2 %d\n",picture->data[0],picture->data[1],picture->data[2]);
            		__android_log_print(ANDROID_LOG_ERROR, TAG,
            				"H264Decoder get frame context->width %d context->height %d data 2 %d\n",context->width,context->height);
            		__android_log_print(ANDROID_LOG_ERROR, TAG,"H264Decoder get framepicture->linesize[0] %d picture->linesize[1] %d\n",picture->linesize[0],picture->linesize[1]);
               }
    		DisplayYUV_16((int*)Pixel, picture->data[0], picture->data[1], picture->data[2], context->width, context->height, picture->linesize[0], picture->linesize[1], iWidth);	

	            }
	            avpkt.size -= len;
	            avpkt.data += len;
	        }

(*en)->ReleaseByteArrayElements(en,jin, inbuf, 0);
(*en)->ReleaseByteArrayElements(en,jout, Pixel, 0);

	return len;
}

