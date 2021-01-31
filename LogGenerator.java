import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class LogGenerator {
	static FileOutputStream file;
	static OutputStreamWriter out;

	public static void start(String f) throws IOException {
		file = new FileOutputStream(f);
		out = new OutputStreamWriter(file, "UTF-8");
	}

	// Restore the original settings.
	public static void stop() {
		try {
			out.flush();
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeLog(String str) {
		try {
			out.write(str + '\n');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
