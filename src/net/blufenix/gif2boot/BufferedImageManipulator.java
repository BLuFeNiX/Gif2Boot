package net.blufenix.gif2boot;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class BufferedImageManipulator {

	private Status status = Status.NOT_DONE;
	private BufferedImage image = null;

	public BufferedImageManipulator(BufferedImage image) {
		setImage(image);
	}
	
	public BufferedImageManipulator() {
		// do nothing
	}
	
	public void setImage(BufferedImage image) {
		this.image = image;
	}	
	
	public BufferedImage getImage() {
		return image;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}
	
	public void resizeToScreen(int resizeWidth, int resizeHeight) {
		// Create new (blank) image of required (scaled) size
		BufferedImage scaledImage = new BufferedImage(resizeWidth, resizeHeight, BufferedImage.TYPE_INT_ARGB);
		// Paint scaled version of image to new image
		Graphics2D graphics2D = scaledImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(image, 0, 0, resizeWidth, resizeHeight, null);
		graphics2D.dispose();
		image = scaledImage;
	}

	public void cropSidesToMatchAspectRatio(int resizeWidth, int resizeHeight) {
		double ratio = (double)image.getHeight() / (double)resizeHeight;
		int newWidth =  (int) Math.round(((double)resizeWidth * ratio));
		int cropTotal = image.getWidth() - newWidth;
		int cropSide = cropTotal / 2;			
		// Create new (blank) image of required (cropped) size
		BufferedImage modifiedImage = new BufferedImage(newWidth, image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		// Paint cropped image at negative coords to cause cropping
		Graphics2D graphics2Dtest = modifiedImage.createGraphics();
		graphics2Dtest.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2Dtest.drawImage(image, (cropSide * -1), 0, image.getWidth(), image.getHeight(), null);
		graphics2Dtest.dispose();
		image = modifiedImage;
	}
	
	public void CropOrLetterboxToDimensions(int frameWidth, int frameHeight) {
		int offsetHeight = (image.getHeight() - frameHeight) / 2;
		int offsetWidth = (image.getWidth() - frameWidth) / 2;
		// Create new (blank) image of required size
		BufferedImage modifiedImage = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_ARGB);
		// Paint cropped image at negative coords to cause cropping
		Graphics2D graphics2Dtest = modifiedImage.createGraphics();
		graphics2Dtest.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2Dtest.drawImage(image, (offsetWidth * -1), (offsetHeight * -1), image.getWidth(), image.getHeight(), null);
		graphics2Dtest.dispose();
		image = modifiedImage;
	}
	
	public void invertColors() {
        Color c;
        for (int x = 0; x < image.getWidth(); x++) { //width
            for (int y = 0; y < image.getHeight(); y++) { //height
                int RGBA = image.getRGB(x, y); //gets RGBA data for the specific pixel
                c = new Color(RGBA, true); //get the color data of the specific pixel
                c = new Color(Math.abs(c.getRed() - 255), Math.abs(c.getGreen() - 255), Math.abs(c.getBlue() - 255)); //Swaps values
                //i.e. 255, 255, 255 (white)
                //becomes 0, 0, 0 (black)                
                image.setRGB(x, y, c.getRGB()); //set the pixel to the altered colors
            }
        }
    }

	public void rotate90() {
		int width = image.getWidth();
		int height = image.getHeight();
		BufferedImage newImage = new BufferedImage(height, width, image.getType());

		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				newImage.setRGB(height - 1 - j, i, image.getRGB(i, j));

		image = newImage;
	}
	
	public void rotate270() {
		int width = image.getWidth();
		int height = image.getHeight();
		BufferedImage newImage = new BufferedImage(height, width, image.getType());

		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				newImage.setRGB(j, width - 1 - i, image.getRGB(i, j));

		image = newImage;
	}

}
