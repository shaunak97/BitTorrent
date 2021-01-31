import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtil {
	public static String getTime() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(cal.getTime());

	}
}
