import java.io.InputStream;
import java.io.IOException;

public class ProcessInterface  {

//	private ProcessBuilder builder;
//	private Process proc;
//	private InputStream processOutput;
//	private String path = null;
//
//	public ProcessInterface(String path) throws Exception  {
//		System.out.println("CREATE");
//		this.path = path;
//	}

	public String sendCommand(String[] strings) {
		System.out.println("BEGIN SEND");
		String output = "";
		try {
			ProcessBuilder builder = new ProcessBuilder(strings);
			builder.redirectErrorStream(true);
			Process proc = builder.start();			
			InputStream processOutput = proc.getInputStream();

			int c = 0;
			byte[] buffer = new byte[2048];
			System.out.println("BEFORE WHILE");
			while((c = processOutput.read(buffer)) != -1 ) {
				System.out.println("READING");
				System.out.println(buffer[buffer.length-1]);
				output += new String(buffer);
				System.out.write(buffer, 0, c);
			}
		} catch (IOException e) { e.printStackTrace(); }
		return output;
	}


}