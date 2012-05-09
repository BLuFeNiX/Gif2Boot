import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Scanner;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.apache.xmlgraphics.image.codec.png.PNGImageEncoder;


public class backend {

	public static int createBootZip(File file, Dimension deviceDimensions, String options, final JProgressBar progressBar, final JLabel progressLabel) {
		
		int framerate = 10; //default to initialize with, will change later
		
		if (!file.exists()) {
           System.err.println("\"" + file.getName() + "\" does not exist, please check the path and try again.\n");
           return 1;
        }
		
		
		ImageInputStream stream = null;
		try {
			stream = ImageIO.createImageInputStream(file);
		} catch (IOException e1) { return 3; }
		
		
		Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
		if (!readers.hasNext()) {
			return 2;
		}
		ImageReader reader = (ImageReader) readers.next();
		reader.setInput(stream);
		
		System.out.println("Processing animated gif...");
		
		// get number of frames
		int numImages;
		try {
			numImages = reader.getNumImages(true);
		} catch (IOException e1) { return 3; }
		System.out.println("Number of frames: " + numImages);
		
		String[] filenames = new String[numImages+1]; // These are the files to include in the bootanimation.zip file at the end

		BufferedImage background = null;	// optimized GIFs store only the differences in each frame, so we keep a background frame to layer them, creating a complete new frame
		
		for (int i = 0; i < numImages; i++) {
			// get metadata (in order to detect framerate and top/left positioning for each frame)
			IIOImage frame = null;
			try {
				frame = reader.readAll(i,null);
			} catch (IOException e1) { return 3; }
			IIOMetadata meta = frame.getMetadata();
			IIOMetadataNode imgRootNode = (IIOMetadataNode) meta.getAsTree("javax_imageio_gif_image_1.0");
			
			// get framerate for animation (only from the first frame, since it will usually all be the same)
			if (i == 0) {
				IIOMetadataNode gce = (IIOMetadataNode) imgRootNode.getElementsByTagName("GraphicControlExtension").item(0);
				int delay = Integer.parseInt(gce.getAttribute("delayTime"));
				System.out.println("gif frame delay: " + delay);
				framerate = (100 / delay);  // get framerate from delay
				System.out.println("framerate: " + framerate);
			}
			
			System.out.print("Processing frame " + (i+1) + "... ");
			final int progress = (int)( (double)(i+1) / (double)numImages * 100);
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
			    	progressLabel.setText(progress + "%");
			    	progressBar.setValue(progress);
			    }
			  });

			// get top/left position for current frame
			IIOMetadataNode imgDescr = (IIOMetadataNode) imgRootNode.getElementsByTagName("ImageDescriptor").item(0);
			int offsetX = Integer.parseInt(imgDescr.getAttribute("imageLeftPosition"));  // find offsets for each
			int offsetY = Integer.parseInt(imgDescr.getAttribute("imageTopPosition"));   // frame in the animated GIF

			// set current frame as overlay and previous frame as background, in order to create a complete frame
			BufferedImage overlay;
			try {
				overlay = reader.read(i);
			} catch (IOException e1) { return 3; }
			if (i == 0) { background = overlay; }			

			// create new object to store the complete frame
			BufferedImage combined = new BufferedImage(background.getWidth(), background.getHeight(), BufferedImage.TYPE_INT_ARGB);

			// paint both images to the new object, preserving the alpha channels
			Graphics g = combined.getGraphics();
			g.drawImage(background, 0, 0, null);
			g.drawImage(overlay, offsetX, offsetY, null);			
			background = combined; // save our most recently created frame as the background for the next frame (in case of optimized GIFs)

			
			// check for options
			if (options.contains("centerFrame")) {
				combined = centerFrame(combined, (int)deviceDimensions.getHeight());
			}
			else if (options.contains("zoomFrame")) {
				combined = zoomFrame(combined, (int)deviceDimensions.getWidth(), (int)deviceDimensions.getHeight());
			}// OTHERWISE, rotate frame if image is wider than it is high (this way we don't squash it on resize)	
			else if (combined.getWidth() > combined.getHeight()) {
				combined = rotate270(combined);
			}

			// resize frame to fit screen
			combined = resizeToScreen(combined, (int)deviceDimensions.getWidth(), (int)deviceDimensions.getHeight());

			// clean up old files and create new directory for images
			if (i == 0) {
				try {
					delete(new File("part0"));
					delete(new File("desc.txt"));
				} catch (IOException e) { return 3; }
				new File("part0/").mkdirs();
			}
			
			PNGImageEncoder encoder;
			
		    try {
		    	encoder = new PNGImageEncoder(new FileOutputStream("part0/img" + String.format("%04d", i) + ".png"), null);
				encoder.encode((RenderedImage) combined);
			} catch (IOException e) { return 3; }
		    
		    //add filename to list of files to zip
		    filenames[i] = "part0/img" + String.format("%04d", i) + ".png";		    
		    System.out.println("done.");
		    
		}
		
		try {
			stream.close();
		} catch (IOException e) { return 3; }
		
		System.out.println("creating desc.txt");
		createDescFile((int)deviceDimensions.getWidth(), (int)deviceDimensions.getHeight(), framerate);
		System.out.println("packing files into bootanimation.zip");
		filenames[numImages] = "desc.txt"; //don't need to add one because of 0-based array
		try {
			zipIt(filenames);
			System.out.println("cleaning up...");
			delete(new File("part0"));
			delete(new File("desc.txt"));
		} catch (IOException e) { return 3; }
		
		
		System.out.println("Complete.");
		System.out.println("\nTo use the animation, place bootanimation.zip in the /data/local/ directory on your phone.");
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	progressLabel.setText("");
		    	progressBar.setValue(0);
		    }
		  });
		return 0;
		
	}
	
	
	
	
	private static BufferedImage resizeToScreen(BufferedImage bufferedImage, int resizeWidth, int resizeHeight) {
		// Create new (blank) image of required (scaled) size
		BufferedImage scaledImage = new BufferedImage(resizeWidth, resizeHeight, BufferedImage.TYPE_INT_ARGB);
		// Paint scaled version of image to new image
		Graphics2D graphics2D = scaledImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(bufferedImage, 0, 0, resizeWidth, resizeHeight, null);
		graphics2D.dispose();
		return scaledImage;
	}

	private static BufferedImage centerFrame(BufferedImage bufferedImage, int resizeHeight) {
		double ratio = (double)bufferedImage.getHeight() / (double)resizeHeight;
		int newWidth = (int) ((double)bufferedImage.getWidth() * ratio);
		int cropTotal = bufferedImage.getWidth() - newWidth;
		int cropSide = cropTotal / 2;			
		// Create new (blank) image of required (cropped) size
		BufferedImage modifiedImage = new BufferedImage(newWidth, bufferedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		// Paint cropped image at negative coords to cause cropping
		Graphics2D graphics2Dtest = modifiedImage.createGraphics();
		graphics2Dtest.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2Dtest.drawImage(bufferedImage, (cropSide * -1), 0, bufferedImage.getWidth(), bufferedImage.getHeight(), null);
		graphics2Dtest.dispose();
		return modifiedImage;
	}
	
	private static BufferedImage zoomFrame(BufferedImage bufferedImage, int frameWidth, int frameHeight) {
		int offsetHeight = (bufferedImage.getHeight() - frameHeight) / 2;
		int offsetWidth = (bufferedImage.getWidth() - frameWidth) / 2;
		// Create new (blank) image of required size
		BufferedImage modifiedImage = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_ARGB);
		// Paint cropped image at negative coords to cause cropping
		Graphics2D graphics2Dtest = modifiedImage.createGraphics();
		graphics2Dtest.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2Dtest.drawImage(bufferedImage, (offsetWidth * -1), (offsetHeight * -1), bufferedImage.getWidth(), bufferedImage.getHeight(), null);
		graphics2Dtest.dispose();
		return modifiedImage;
	}

	public static void createDescFile(int width, int height, int framerate){
		try {
			PrintWriter out = new PrintWriter(new FileWriter("desc.txt"));
			out.println(width + " " + height + " " + framerate);
			out.println("p 20 0 part0");
			out.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
		
	public static void zipIt(String[] filenames) throws IOException {
        File zipFile = new File("bootanimation.zip");
        //if (zipFile.exists()) {
        //   System.err.println("Zip file already exists, please try another");
        //    System.exit(-2);
        //}
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = new ZipOutputStream(fos);
        int bytesRead;
        byte[] buffer = new byte[1024];
        CRC32 crc = new CRC32();
        for (int i = 0; i < filenames.length; i++) {
            String name = filenames[i];
            File file = new File(name);
            //if (!file.exists()) {
            //    System.err.println("Skipping: " + name);
            //    continue;
            //}
            BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(file));
            crc.reset();
            while ((bytesRead = bis.read(buffer)) != -1) {
                crc.update(buffer, 0, bytesRead);
            }
            bis.close();
            // Reset to beginning of input stream
            bis = new BufferedInputStream(
                new FileInputStream(file));
            ZipEntry entry = new ZipEntry(name);
            entry.setMethod(ZipEntry.STORED);
            entry.setCompressedSize(file.length());
            entry.setSize(file.length());
            entry.setCrc(crc.getValue());
            zos.putNextEntry(entry);
            while ((bytesRead = bis.read(buffer)) != -1) {
                zos.write(buffer, 0, bytesRead);
            }
            bis.close();
        }
        zos.close();
    }
	
	
	
	

	public static BufferedImage rotate90(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		BufferedImage newImage = new BufferedImage(height, width, img.getType());

		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				newImage.setRGB(height - 1 - j, i, img.getRGB(i, j));

		return newImage;
	}
	
	public static BufferedImage rotate270(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		BufferedImage newImage = new BufferedImage(height, width, img.getType());

		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				newImage.setRGB(j, width - 1 - i, img.getRGB(i, j));

		return newImage;
	}

	public static void delete(File file) throws IOException{

		if(file.isDirectory()){
			//directory is empty, then delete it
			if(file.list().length==0){
				file.delete();
			}
			else {
				//list all the directory contents
				String files[] = file.list();

				for (String temp : files) {
					//construct the file structure
					File fileDelete = new File(file, temp);
					//recursive delete
					delete(fileDelete);
				}

				//check the directory again, if empty then delete it
				if(file.list().length==0){
					file.delete();
				}
			}

		}else{
			//if file, then delete it
			file.delete();
		}
	}




	public static int flashBootAnimation() {
		
		// WINDOWS - BROKEN
//		String path = new File("").getAbsolutePath();
//		System.out.println("STEP 0");
//		execute("taskkill /f /IM adb.exe"); // kill adb ONLY if it's not being run as administrator
//		System.out.println("STEP 1");
//		execute(path + "\\ADB\\adb.exe start-server");
//		System.out.println("STEP 2");
//		execute("\\ADB\\adb.exe wait-for-device");
//		System.out.println("STEP 3");
//		execute("\\ADB\\adb.exe push bootanimation.zip /data/local/");
		
		
		//LINUX
		System.out.println(System.getProperty("os.name"));
		String path = new File("").getAbsolutePath();
		execute("killall adblinux");
		execute("killall adb");
		
		String output = execute("gksudo --description gksudo.config " + path + "/ADB/adblinux devices").toLowerCase();
		if ( output.contains("device not found") ) {
			return 1;
		}
		else if ( removeWhitespace(output).isEmpty() ) {
			return 2;
		}
		
		String output2 = execute("gksudo --description gksudo.config " + path + "/ADB/adblinux push bootanimation.zip /data/local/").toLowerCase();
		if ( output2.contains("device not found") ) {
			return 1;
		}
		else if ( removeWhitespace(output2).isEmpty() ) {
			return 2;
		}
		System.out.println("DONE FLASHING.");
		return 0;

	}


	private static String removeWhitespace(String output) {
		return output.replaceAll(" ", "").replaceAll("\n", "").replaceAll("\t", "");
	}




	private static String execute(String command) {
		String output = "";
		try {
			Process proc = Runtime.getRuntime().exec(command);
			InputStream in = proc.getInputStream();
			InputStream err = proc.getErrorStream();
			
			Scanner scin = new Scanner(in);
			while (scin.hasNext()) {
				output += scin.useDelimiter("\\A").next();
			}
			
			Scanner scerr = new Scanner(err);
			while (scerr.hasNext()) {
				output += scerr.useDelimiter("\\A").next();
			}			
			System.out.println(output);
		} catch (IOException e) { e.printStackTrace(); }
		
		return output;
	}
	
	

}
