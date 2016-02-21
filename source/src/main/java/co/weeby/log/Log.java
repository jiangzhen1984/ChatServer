package co.weeby.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;

public class Log {
	
	public static final int LEVEL_DEBUG = 1;
	public static final int LEVEL_INFO = 2;
	public static final int LEVEL_WARN = 3;
	public static final int LEVEL_ERROR = 4;
	

	public static void w(String tag, String msg) {
		nativeLog(LEVEL_DEBUG, tag, msg, null);
	}

	public static void i(String tag, String msg) {
		nativeLog(LEVEL_INFO, tag, msg, null);
	}

	public static void e(String tag, String msg) {
		nativeLog(LEVEL_WARN, tag, msg, null);
	}

	public static void d(String tag, String msg) {
		nativeLog(LEVEL_DEBUG, tag, msg, null);
	}
	
	
	public static void w(String tag, String msg, Throwable t) {
		nativeLog(LEVEL_DEBUG, tag, msg, t);
	}

	public static void i(String tag, String msg, Throwable t) {
		nativeLog(LEVEL_INFO, tag, msg, t);
	}

	public static void e(String tag, String msg, Throwable t) {
		nativeLog(LEVEL_WARN, tag, msg, t);
	}

	public static void d(String tag, String msg, Throwable t) {
		nativeLog(LEVEL_ERROR, tag, msg, t);
	}
	
	
	
	public static void w(String tag,  Throwable t) {
		nativeLog(LEVEL_DEBUG, tag, t.getMessage(), t);
	}

	public static void i(String tag,  Throwable t) {
		nativeLog(LEVEL_INFO, tag, t.getMessage(), t);
	}

	public static void e(String tag,  Throwable t) {
		nativeLog(LEVEL_WARN, tag, t.getMessage(), t);
	}

	public static void d(String tag,  Throwable t) {
		nativeLog(LEVEL_ERROR, tag, t.getMessage(), t);
	}
	
	public static void nativeLog(int level, String tag, String msg, Throwable t) {
		String tm = "";
		if (t != null) {
			tm = getStackTraceString(t);
		}
		switch (level) {
		case LEVEL_DEBUG:
			System.out.println("[DEBUG]  " +"["+tag+"]"+"  "+ msg+"  "+ tm);
			break;
		case LEVEL_INFO:
			System.out.println("[INFO]  " +"["+tag+"]"+"  "+ msg+"  "+ tm);
			break;
		case LEVEL_WARN:
			System.out.println("[WARN]  " +"["+tag+"]"+"  "+ msg+"  "+ tm);
			break;
		case LEVEL_ERROR:
			System.out.println("[ERROR]  " +"["+tag+"]"+"  "+ msg+"  "+ tm);
			break;
		}
	}
	
	
	
    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new FastPrintWriter(sw, false, 256);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
}
