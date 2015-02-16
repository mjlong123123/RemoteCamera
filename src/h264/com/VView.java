package h264.com;

import com.android.ffmpeglib.H264Decoder;


public class VView {
	private H264Decoder mH264Decoder = null;

	public int InitDecoder(int width, int height)
	{
		mH264Decoder = new H264Decoder();
		mH264Decoder.init(width, height);
		return 0;
	}

	public int UninitDecoder()
	{
		mH264Decoder.release();
		mH264Decoder = null;
		return 0;
	}

	public int DecoderNal(byte[] in, int insize, byte[] out){
		mH264Decoder.decode(in, out);
		return 0;
	}

/*	static {
		System.loadLibrary("H264Android");
	}*/

}
