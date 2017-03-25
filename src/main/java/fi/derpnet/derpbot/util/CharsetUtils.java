package fi.derpnet.derpbot.util;

import java.nio.charset.Charset;
import org.mozilla.universalchardet.UniversalDetector;

public class CharsetUtils {

    public static String convertToUTF8(byte[] input) {
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(input, 0, input.length);
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        if (encoding == null) {
            encoding = "UTF-8";
        }
        return new String(input, Charset.forName(encoding));
    }
}
