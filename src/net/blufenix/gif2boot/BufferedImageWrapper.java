package net.blufenix.gif2boot;
import java.awt.image.BufferedImage;

/*
 * This class allows backend.java to thread the processing of BufferedImages.
 * The status variable indicates whether or not we have processed the image.
 *  
 */

public class BufferedImageWrapper {

	static public int NOT_DONE = 0;
	static public int PROCESSING = 1;
	static public int DONE = 2;
	private int status = 0;
	private BufferedImage bufferedImage = null;

	public BufferedImageWrapper(BufferedImage bufferedImage) {
		this.bufferedImage = bufferedImage;
	}
	
	public BufferedImageWrapper() {
		//do nothing
	}

	public void setImage(BufferedImage bufferedImage) {
		this.bufferedImage = bufferedImage;
	}
	
	public BufferedImage getImage() {
		return this.bufferedImage;
	}	
	
	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return this.status;
	}

}
