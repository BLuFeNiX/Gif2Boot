import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


public class test {

	public static void main(String[] args) throws UnsupportedEncodingException {
		
		String PWD = ClassLoader.getSystemClassLoader().getResource(".").getPath();
		System.out.println(PWD);
		PWD = URLDecoder.decode(PWD, "UTF-8");
		System.out.println(PWD);
		new File("/C:/Users/Chase/workspace/gif2boot/bin/TESTING/").mkdirs();
		
//		System.out.println(System.getProperty("os.name"));
//		System.out.println(System.getProperty("os.arch"));
//		System.out.println(System.getProperty("os.version"));
//		System.out.println(System.getProperty("file.separator"));
//		System.out.println(System.getProperty("path.separator"));
//		System.out.println(System.getProperty("java.class.path"));
//		System.out.println(System.getProperty("java.home"));
//		System.out.println(System.getProperty("java.vendor"));
//		System.out.println(System.getProperty("java.vendor.url"));
//		System.out.println(System.getProperty("java.version"));
//		System.out.println(System.getProperty("user.dir"));
//		System.out.println(System.getProperty("user.home"));
//		System.out.println(System.getProperty("user.name"));
	}

}
