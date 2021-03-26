package co.freea.tsn.Training.Core.Util;

import java.util.Arrays;

public final class Logger {
    public static void debug(Object... obj) {
        if (obj != null && obj.length > 0) {
            try {
                System.out.println(Arrays.toString(obj));
            } catch (Exception ignored) {
            }
        }
    }
}
