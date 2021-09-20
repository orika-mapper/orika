package ma.glasnost.orika.test.community.issue121.util;

import java.util.Random;

/**
 * @author: Ilya Krokhmalyov YC14IK1
 * @since: 8/23/13
 */

public class RandomUtils {
    private static final Random random = new Random();

    public static int randomInt() {
        return random.nextInt();
    }

    public static String randomString() {
    	String candidateChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    	StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            sb.append(candidateChars.charAt(random.nextInt(candidateChars
                    .length())));
        }
    	
        return sb.toString();
    }
}
