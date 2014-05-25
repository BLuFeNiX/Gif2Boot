package net.blufenix.gif2boot;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
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
	
	static int numImages = 0;
	static String PWD;

	public static int createBootZip(File file, final Dimension deviceDimensions, final String options, final JProgressBar progressBar, final JLabel progressLabel, String pwdTemp) {
		
		if ((int)deviceDimensions.getWidth() == 0 || (int)deviceDimensions.getHeight() == 0) {
			return 4;
		}
		
		PWD = pwdTemp;
		int framerate = 10; //default to initialize with, will change later
		
		// does the GIF exist?
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
		try {
			numImages = reader.getNumImages(true);
		} catch (IOException e1) { return 3; }
		System.out.println("Number of frames: " + numImages);
		
		// set progress bar MAX to numImages
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setMaximum(numImages);
			}
		});
		
		final String[] filenames = new String[numImages+1]; // These are the files to include in the bootanimation.zip file at the end (+1 for desc.txt)
		final BufferedImageManipulator[] images = new BufferedImageManipulator[numImages];
		for (int i = 0; i < images.length; i++) {
			images[i] = new BufferedImageManipulator();
		}

		// pre-process images
		for (int i = 0; i < images.length; i++) {
			// get metadata (in order to detect framerate and top/left positioning for each frame)
			IIOImage frame = null;
			try {
				frame = reader.readAll(i,null);
			} catch (IOException e1) { return 3; }
			IIOMetadata meta = frame.getMetadata();
			IIOMetadataNode imgRootNode = (IIOMetadataNode) meta.getAsTree("javax_imageio_gif_image_1.0");
			
			// get framerate for animation (only from the first frame, since it should all be the same)
			if (i == 0) {
				IIOMetadataNode gce = (IIOMetadataNode) imgRootNode.getElementsByTagName("GraphicControlExtension").item(0);
				int delay = Integer.parseInt(gce.getAttribute("delayTime"));
				System.out.println("gif frame delay: " + delay);
				if (delay < 1) {
					System.out.println("improper GIF delay, setting delay to 10");
					delay = 10;
				}
				framerate = (100 / delay);  // get framerate from delay
				System.out.println("framerate: " + framerate);
			}
			
			System.out.println("Pre-processing frame " + (i+1) + "... ");

			// get top/left position for current frame
			IIOMetadataNode imgDescr = (IIOMetadataNode) imgRootNode.getElementsByTagName("ImageDescriptor").item(0);
			int offsetX = Integer.parseInt(imgDescr.getAttribute("imageLeftPosition"));  // find offsets for each
			int offsetY = Integer.parseInt(imgDescr.getAttribute("imageTopPosition"));   // frame in the animated GIF

			// set current frame as overlay and previous frame as background, in order to create a complete frame
			try {
				images[i].setImage(reader.read(i));
			} catch (IOException e1) { return 3; }
			
			if (i > 0) { //don't need to process first image, since it will always be a complete frame
				// create new object to store the complete frame
				BufferedImage combined = new BufferedImage(images[i-1].getImage().getWidth(), images[i-1].getImage().getHeight(), BufferedImage.TYPE_INT_ARGB);
				// paint both images to the new object, preserving the alpha channels
				Graphics g = combined.getGraphics();
				g.drawImage(images[i-1].getImage(), 0, 0, null);
				g.drawImage(images[i].getImage(), offsetX, offsetY, null);			
				images[i].setImage(combined); // save our most recently created frame as the background for the next frame (in case of optimized GIFs)
			}
			
		}
		
		//close ImageIO stream, since we've stored all the images in an array
		try {
			stream.close();
		} catch (IOException e) { return 3; }
		
		// clean up old files and create new directory for images
		try {
			delete(new File("part0/"));
			delete(new File("desc.txt"));
		} catch (IOException e) { return 3; }
		new File("part0/").mkdirs();
		
		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println("Using " + cores + " cores for processing.");
		for (int j = 0; j < cores; j++) {
			new Thread( new Runnable() { public void run() {
				// apply extra options (algorithms) to images, and save them to disk
				for (int i = 0; i < images.length; i++) {
					if (images[i].getStatus() == Status.NOT_DONE) {
						//System.out.println("BEGIN" + i);
						images[i].setStatus(Status.PROCESSING);
						// check for options
						if (options.contains("centerFrame")) {
							images[i].cropSidesToMatchAspectRatio((int)deviceDimensions.getWidth(), (int)deviceDimensions.getHeight());
						}
						else if (options.contains("zoomFrame")) {
							images[i].CropOrLetterboxToDimensions((int)deviceDimensions.getWidth(), (int)deviceDimensions.getHeight());
						}// OTHERWISE, rotate frame if image is wider than it is high (this way we don't squash it on resize)	
						else if (images[i].getImage().getWidth() > images[i].getImage().getHeight()) {
							images[i].rotate270();
						}
			
						// resize frame to fit screen
						images[i].resizeToScreen((int)deviceDimensions.getWidth(), (int)deviceDimensions.getHeight());
						
						// invert the image colors
						if (options.contains("negative")) {
							System.out.println("INVERTING: " + i);
							images[i].invertColors();
							System.out.println("DONE INVERTING: " + i);
						}
						
					    try {
					    	PNGImageEncoder encoder = new PNGImageEncoder(new FileOutputStream("part0/img" + String.format("%04d", i) + ".png"), null);
							encoder.encode((RenderedImage) images[i].getImage());
						} catch (IOException e) { /*return 3;*/ }
					    
					    //add filename to list of files to zip
					    filenames[i] = "part0/img" + String.format("%04d", i) + ".png";
					    images[i].setStatus(Status.DONE);
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								progressBar.setValue(progressBar.getValue()+1);
								progressLabel.setText( (int)(( (double)progressBar.getValue()/(double)images.length) *100) + "%");
							}
						});
					}
				}
			} }).start();
		}
		
		
		// count backwards, since the last image is more likely to not be done, making the loop less CPU-intensive
		int numDone = 0;
		while (numDone < images.length) {
			for (int i = images.length-1; i > -1; i--) {
				if (images[i].getStatus() == Status.DONE) {
					numDone++;
				}
				else {
					numDone = 0;
					i = -1;
				}
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) { e.printStackTrace();	}
		}
		
		
		System.out.println("creating desc.txt");
		createDescFile((int)deviceDimensions.getWidth(), (int)deviceDimensions.getHeight(), framerate);
		System.out.println("packing files into bootanimation.zip");
		filenames[numImages] = "desc.txt"; //don't need to add one because of 0-based array
		try {
			zipIt(filenames);
			System.out.println("cleaning up...");
			delete(new File("part0"));
			delete(new File("desc.txt"));
			delete(new File("part0")); //in case files were locked
			delete(new File("desc.txt")); //in case files were locked
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


	public static void createDescFile(int width, int height, int framerate){
		try {
			PrintWriter out = new PrintWriter(new FileWriter("desc.txt"));
			out.println(width + " " + height + " " + framerate);
			out.println("p 0 0 part0");
			out.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
		
	public static void zipIt(String[] filenames) throws IOException {
        File zipFile = new File(PWD+"bootanimation.zip");
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = new ZipOutputStream(fos);
        int bytesRead;
        byte[] buffer = new byte[1024];
        CRC32 crc = new CRC32();
        for (int i = 0; i < filenames.length; i++) {
            String name = filenames[i];
            File file = new File(name);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            crc.reset();
            while ((bytesRead = bis.read(buffer)) != -1) {
                crc.update(buffer, 0, bytesRead);
            }
            bis.close();
            // Reset to beginning of input stream
            bis = new BufferedInputStream(new FileInputStream(file));
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
	
	public static int flashBootAnimation(String path) {	
		ADBInterface adb = new ADBInterface();
		int status = adb.startDaemon();
		if (status > 0) {
			return status; 
		}
		System.out.println("Trying to flash: " + path);
		return adb.push(path, "/data/local/bootanimation.zip");
	}

}
