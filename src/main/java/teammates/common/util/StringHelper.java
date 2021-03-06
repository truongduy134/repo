package teammates.common.util;

import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/** Holds String-related helper functions
 */
public class StringHelper {

    public static String generateStringOfLength(int length) {
        return StringHelper.generateStringOfLength(length, 'a');
    }

    public static String generateStringOfLength(int length, char character) {
        assert (length >= 0);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(character);
        }
        return sb.toString();
    }

    public static boolean isWhiteSpace(String string) {
        return string.trim().isEmpty();
    }
    
    /**
     * Check whether the input string matches the regex repression
     * @param input The string to be matched
     * @param regex The regex repression used for the matching
     */
    public static boolean isMatching(String input, String regex) {
        // Important to use the CANON_EQ flag to make sure that canonical characters
        // such as é is correctly matched regardless of single/double code point encoding
        return Pattern.compile(regex, Pattern.CANON_EQ).matcher(input).matches();
    }

    public static String getIndent(int length) {
        return generateStringOfLength(length, ' ');
    }

    /**
     * Checks whether the {@code inputString} is longer than a specified length
     * if so returns the truncated name appended by ellipsis,
     * otherwise returns the original input. <br>
     * E.g., "12345678" truncated to length 6 returns "123..."
     */
    public static String truncate(String inputString, int truncateLength){
        if(!(inputString.length()>truncateLength)){
            return inputString;
        }
        String result = inputString;
        if(inputString.length()>truncateLength){
            result = inputString.substring(0,truncateLength-3)+"...";
        }
        return result;
    }

    public static String encrypt(String value) {
        try {
            SecretKeySpec sks = new SecretKeySpec(
                    hexStringToByteArray(Config.ENCRYPTION_KEY), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, sks, cipher.getParameters());
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return byteArrayToHexString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(String message) {
        try {
            SecretKeySpec sks = new SecretKeySpec(
                    hexStringToByteArray(Config.ENCRYPTION_KEY), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, sks);
            byte[] decrypted = cipher.doFinal(hexStringToByteArray(message));
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Concatenates a list of strings to a single string, separated by line breaks.
     * @return Concatenated string.
     */
    public static String toString(List<String> strings) {
        return toString(strings, Const.EOL);    
    }

    /**
     * Concatenates a list of strings to a single string, separated by the given delimiter.
     * @return Concatenated string.
     */
    public static String toString(List<String> strings, String delimiter) {
        String returnValue = "";
        
        if(strings.size()==0){
            return returnValue;
        }
        
        for(int i=0; i < strings.size()-1; i++){
            String s = strings.get(i);
            returnValue += s + delimiter;
        }
        //append the last item
        returnValue += strings.get(strings.size()-1);
        
        return returnValue;        
    }
    
    public static String toDecimalFormatString(double doubleVal) {
        DecimalFormat df = new DecimalFormat("0.#");
        return df.format(doubleVal);
    }

    public static String toUtcFormat(double hourOffsetTimeZone) {
        String utcFormatTimeZone = "UTC";
        if (hourOffsetTimeZone != 0) {
            if ((int) hourOffsetTimeZone == hourOffsetTimeZone)
                utcFormatTimeZone += String.format(" %+03d:00",
                        (int) hourOffsetTimeZone);
            else
                utcFormatTimeZone += String.format(
                        " %+03d:%02d",
                        (int) hourOffsetTimeZone,
                        (int) (Math.abs(hourOffsetTimeZone
                                - (int) hourOffsetTimeZone) * 300 / 5));
        }

        return utcFormatTimeZone;
    }
    
    private static String byteArrayToHexString(byte[] b) {
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase();
    }

    private static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

}
