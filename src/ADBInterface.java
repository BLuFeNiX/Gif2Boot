import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class ADBInterface {

	private String OS_NAME;
	private int OS; // int representing OS
	private String FS; // file separator. '/' or '\'
	private String PWD; // no trailing slash
	private String ADBPATH;
	
	private final int WINDOWS = 1; 
	private final int MAC 	  = 2;
	private final int LINUX   = 3;
	private final int OTHER   = 4;
	
	public final int DEVICE_NOT_FOUND = 1;
	public final int OUTPUT_EMPTY = 2;
	public final int FAILED = 4;
	public final int NO_SUCH_FILE = 5;
	
	ADBInterface() {
		this.OS_NAME = System.getProperty("os.name");
		this.FS = System.getProperty("file.separator");
		PWD = new File("").getAbsolutePath();
		if (OS_NAME.contains("Windows")) {
			OS = WINDOWS;
			ADBPATH = PWD+FS+"ADB"+FS+"adb.exe"; 
		} else if (OS_NAME.contains("Mac")) {
			OS = MAC;
			ADBPATH = PWD+FS+"ADB"+FS+"adbmac";
		} else if (OS_NAME.contains("Linux")) {
			OS = LINUX;
			ADBPATH = PWD+FS+"ADB"+FS+"adblinux";
		} else {
			OS = OTHER;
			ADBPATH = PWD+FS+"ADB"+FS+"adblinux"; // default to Linux, so we have minimal support for Unix, BSD, etc.
		}	
	}
	
	// not done
	public int startDaemon() {
		// LINUX
		if (OS == LINUX) {
			execute(new String[] { ADBPATH, "kill-server" }); // kill any unprivileged instances of ADB
			execute(new String[]{"killall", "adblinux"});     // kill any unprivileged instances of ADB
			execute(new String[]{"killall", "adb"});		  // kill any unprivileged instances of ADB
			//output = execute(new String[]{"gksudo", "--description", "gksudo.config", pwd + "/ADB/adblinux", "devices"}).toLowerCase();
			//output2 = execute(new String[]{"gksudo", "--description", "gksudo.config", pwd + "/ADB/adblinux", "push", "bootanimation.zip", "/data/local/"}).toLowerCase();
		} // WINDOWS
		else if (OS == WINDOWS) {			
			execute(new String[]{"taskkill", "/f", "/IM", "adb.exe"});
			//output = execute(new String[]{pwd + "\\ADB\\adb.exe", "devices"}, "daemon not running").toLowerCase();
			System.out.println("BEGIN FLASH");
			//output2 = execute(new String[]{pwd + "\\ADB\\adb.exe", "push", "bootanimation.zip", "/data/local/"}).toLowerCase();
		}		
		else { return 3; }
		return 0;
	}

	public int push(String sourceFile, String destFile) {
		String output = null;
		switch (OS) {
		case LINUX:
			output = execute(new String[] { "gksudo", "--description", "gksudo.config", ADBPATH, "push", sourceFile, destFile });
			break;
		case WINDOWS:
			output = execute(new String[] { ADBPATH, "push", sourceFile, destFile });
			break;
		case MAC:
			output = execute(new String[] { ADBPATH, "push", sourceFile, destFile });
			break;
		default:
			output = execute(new String[] { ADBPATH, "push", sourceFile, destFile });
			break;
		}
		int result = parseOutput(output);
		return result;
	}
	
	public int pull(String sourceFile, String destFile) {
		String output = null;
		switch (OS) {
		case LINUX:
			output = execute(new String[] { "gksudo", "--description", "gksudo.config", ADBPATH, "pull", sourceFile, destFile });
			break;
		case WINDOWS:
			output = execute(new String[] { ADBPATH, "pull", sourceFile, destFile });
			break;
		case MAC:
			output = execute(new String[] { ADBPATH, "pull", sourceFile, destFile });
			break;
		default:
			output = execute(new String[] { ADBPATH, "pull", sourceFile, destFile });
			break;
		}
		int result = parseOutput(output);
		return result;
	}
	
	
	private int parseOutput(String output) {
		output = output.toLowerCase();
		if ( output.contains("device not found") ) { return DEVICE_NOT_FOUND; }
		else if ( removeWhitespace(output).isEmpty() ) { return OUTPUT_EMPTY; }
		else if ( output.contains("failed") ) { return FAILED; }
		else if ( output.contains("no such file or directory") ) { return NO_SUCH_FILE; }
		else { return 0; }
	}

	// sometimes things don't return, so we make them return with this sentinel value
	String execute(String[] strings, String sentinel) {
		String output = "";
		try {
			ProcessBuilder builder = new ProcessBuilder(strings);
			builder.redirectErrorStream(true);
			Process proc = builder.start();			
			InputStream processOutput = proc.getInputStream();

			int c = 0;
			byte[] buffer = new byte[2048];
			while((c = processOutput.read(buffer)) != -1 ) {
				output += new String(buffer);
				if (output.toLowerCase().contains(sentinel)) { return output; }
				System.out.write(buffer, 0, c);
			}
		} catch (IOException e) { e.printStackTrace(); }
		return output;		
	}

	
	private static String execute(String[] strings) {
		String output = "";
		try {
			ProcessBuilder builder = new ProcessBuilder(strings);
			builder.redirectErrorStream(true);
			Process proc = builder.start();			
			InputStream processOutput = proc.getInputStream();

			int c = 0;
			byte[] buffer = new byte[2048];
			while((c = processOutput.read(buffer)) != -1 ) {
				output += new String(buffer);
				System.out.write(buffer, 0, c);
			}
		} catch (IOException e) { e.printStackTrace(); }
		return output;
	}
	
	private static String removeWhitespace(String output) {
		return output.replaceAll(" ", "").replaceAll("\n", "").replaceAll("\t", "");
	}	
	
	
}
