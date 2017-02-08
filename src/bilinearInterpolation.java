import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class bilinearInterpolation {
	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage img;

	public void playFrame(byte[] outputBytes, int desiredFrame, int width, int height) {
		BufferedImage desiredImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int ind = width*height*desiredFrame*3;
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){			
				byte a = 0;
				byte r = outputBytes[ind];
				byte g = outputBytes[ind+height*width];
				byte b = outputBytes[ind+height*width*2]; 
				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				//int pix = ((a << 24) + (r << 16) + (g << 8) + b); bit shifting
				desiredImg.setRGB(x,y,pix);
				ind++;
			}
		}
		displayImg(desiredImg, width, height);
		
	}
	public BufferedImage[] bytes2IMG(int width, int height, long totalFrames, byte[] bytes) {
		BufferedImage[] allFramesAsImages = new BufferedImage[(int)totalFrames];
		for(int frameIndex = 0; frameIndex < totalFrames; frameIndex++){
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			//ind contains where frameNumber is located in bytes array	
			int ind = width*height*frameIndex*3;
			for(int y = 0; y < height; y++){
				for(int x = 0; x < width; x++){			
					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 
					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b); bit shifting
					img.setRGB(x,y,pix);
					ind++;
					}
				}
			allFramesAsImages[frameIndex] = img;
			img.flush();
		}
		return allFramesAsImages;

	}
	public byte[] RGBFile2Bytes(File file, int width, int height) {
		byte[] bytes = null;
		try {
			//file only contains RGB no alpha
			InputStream is = new FileInputStream(file);
			long len = file.length();
			bytes = new byte[(int)len];

			//read the whole file into  to temp buffer called bytes
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				//is(byte[], off, len) reads up to len bytes from is, attempt to read len bytes but smaller amount may be read
				//return number of bytes read as int, offset tells b[off] through b[off+k-1] where k is amount read
				offset += numRead;
			}
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytes;
	}
	
	public void displayImg(BufferedImage inputImg, int width, int height) {
		frame = new JFrame();
		//when click x button frame closes
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//GridBagLayout places components in a grid of rows and columns
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);
		String result = String.format("Video height: %d, width: %d", height, width);
		JLabel lbText1 = new JLabel(result);
		lbText1.setHorizontalAlignment(SwingConstants.CENTER);
		lbIm1 = new JLabel();
		
		GridBagConstraints c = new GridBagConstraints();
		//Stretches frame horizontally
		c.fill = GridBagConstraints.HORIZONTAL; //Resize the component horizontally but not vertically
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5; //Specifies how to distribute extra horizontal space
		c.gridx = 0;
		c.gridy = 0;
		frame.getContentPane().add(lbText1, c);
		lbIm1.setIcon(new ImageIcon(inputImg));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);
			
		frame.pack();

		frame.setVisible(true);
		inputImg.flush();		
	}
	
	public byte[] byteFrame(int height, int width, byte[] bytes, int frame) {
		byte[] desiredByteFrame = new byte[height*width*3];
		int frameOffset = height*width*3*frame;
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){	
				int outputIndex = y * width + x;
				byte cPxlRByte = bytes[frameOffset];
				byte cPxlGByte = bytes[frameOffset+height*width];						
				byte cPxlBByte = bytes[frameOffset+height*width*2];
				desiredByteFrame[outputIndex] = cPxlRByte;
				desiredByteFrame[outputIndex+height*width] = cPxlGByte;
				desiredByteFrame[outputIndex+height*width*2] = cPxlBByte;
				frameOffset++;
			}
		}
		return desiredByteFrame;
	}
	public int[] resizeBilinear(int[] pixels, int w, int h, int w2, int h2) {
	    int[] temp = new int[w2*h2] ;
	    int a, b, c, d, x, y, index ;
	    float x_ratio = ((float)(w-1))/w2 ;
	    float y_ratio = ((float)(h-1))/h2 ;
	    float x_diff, y_diff, blue, red, green ;
	    int offset = 0 ;
	    for (int i=0;i<h2;i++) {
	        for (int j=0;j<w2;j++) {
	            x = (int)(x_ratio * j) ;
	            y = (int)(y_ratio * i) ;
	            x_diff = (x_ratio * j) - x ;
	            y_diff = (y_ratio * i) - y ;
	            index = (y*w+x) ;                
	            a = pixels[index] ;
	            b = pixels[index+1] ;
	            c = pixels[index+w] ;
	            d = pixels[index+w+1] ;

	            // blue element
	            // Yb = Ab(1-w)(1-h) + Bb(w)(1-h) + Cb(h)(1-w) + Db(wh)
	            blue = (a&0xff)*(1-x_diff)*(1-y_diff) + (b&0xff)*(x_diff)*(1-y_diff) +
	                   (c&0xff)*(y_diff)*(1-x_diff)   + (d&0xff)*(x_diff*y_diff);

	            // green element
	            // Yg = Ag(1-w)(1-h) + Bg(w)(1-h) + Cg(h)(1-w) + Dg(wh)
	            green = ((a>>8)&0xff)*(1-x_diff)*(1-y_diff) + ((b>>8)&0xff)*(x_diff)*(1-y_diff) +
	                    ((c>>8)&0xff)*(y_diff)*(1-x_diff)   + ((d>>8)&0xff)*(x_diff*y_diff);

	            // red element
	            // Yr = Ar(1-w)(1-h) + Br(w)(1-h) + Cr(h)(1-w) + Dr(wh)
	            red = ((a>>16)&0xff)*(1-x_diff)*(1-y_diff) + ((b>>16)&0xff)*(x_diff)*(1-y_diff) +
	                  ((c>>16)&0xff)*(y_diff)*(1-x_diff)   + ((d>>16)&0xff)*(x_diff*y_diff);

	            temp[offset++] = 
	                    0xff000000 | // hardcode alpha
	                    ((((int)red)<<16)&0xff0000) |
	                    ((((int)green)<<8)&0xff00) |
	                    ((int)blue) ;
	        }
	    }
	    return temp ;
	}
	public int[] bytes2Pxls(int width, int height, byte[] bytes) {
		int[] pxls = new int[bytes.length/3];
		int ind = 0;
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				byte a = 0;
				byte r = bytes[ind];
				byte g = bytes[ind+height*width];
				byte b = bytes[ind+height*width*2]; 
				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				//int pix = ((a << 24) + (r << 16) + (g << 8) + b); bit shifting
				pxls[ind] = pix;
				ind++;
			}	
		}
		return pxls;
	}
	
	public BufferedImage pxls2Image(int width, int height, int[] pixels) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int ind = 0;
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				int pixel = pixels[ind];
				image.setRGB(x,y,pixel);
				ind++;
			}
		}
		return image;
	}
	
	public BufferedImage sd2hd(File inputFile) {
		int inputWidth = 176;
		int inputHeight = 144;
		int outputWidth = 960;
		int outputHeight = 540;
		int desiredFrame = 50;
		byte[] bytes = RGBFile2Bytes(inputFile, inputWidth, inputHeight);
		byte[] desiredFrameBytes = byteFrame(inputHeight, inputWidth, bytes, desiredFrame);
		int[] inputPixels = bytes2Pxls(inputWidth, inputHeight, desiredFrameBytes);
		BufferedImage test = pxls2Image(inputWidth, inputHeight, inputPixels);
		displayImg(test,inputWidth, inputHeight);
		int[] outputPixels = resizeBilinear(inputPixels, inputWidth, inputHeight, outputWidth, outputHeight);
		BufferedImage outputImage = pxls2Image(outputWidth, outputHeight, outputPixels);
		return outputImage;
	}
	
	public BufferedImage resize(String[] args){
		File inputFile = new File(args[0]);
		String operation = args[1];
		BufferedImage outputImage = null;

		if (operation.equals("SD2HD")) {
			int outputWidth = 960, outputHeight = 540;
			outputImage = sd2hd(inputFile);
			displayImg(outputImage,outputWidth, outputHeight);
		}
		return outputImage;
		}
	
		

	public static void main(String[] args) {
		bilinearInterpolation ren = new bilinearInterpolation();
		String[] test = {"/Users/shane/Documents/workspace/imageReader/test/prison_176_144.rgb","SD2HD"};
		ren.resize(test);
	}
	
}
