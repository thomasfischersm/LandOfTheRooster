package com.playposse.landoftherooster.util;

import android.text.Editable;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

/**
 * A utility class for dealing with
 */
public final class StringUtil {

    private static final String DATE_TIME_SKELETON = "Mdyhm";

    @Nullable
    public static String getCleanString(TextView textView) {
        return getCleanString(textView.getText().toString());
    }

    @Nullable
    public static String getCleanString(EditText editText) {
        return getCleanString(editText.getText());
    }

    @Nullable
    public static String getCleanString(Editable editable) {
        return getCleanString(editable.toString());
    }

    @Nullable
    public static String getCleanString(String str) {
        if (str == null) {
            return null;
        } else {
            str = str.trim();
            return (str.length() > 0) ? str : null;
        }
    }

    public static boolean equals(Editable editable, @Nullable String str) {
        String editableStr = getCleanString(editable);
        str = getCleanString(str);

        if ((editableStr == null) && (str == null)) {
            return true;
        } else if ((editableStr == null) || (str == null)) {
            return false;
        } else {
            return editableStr.equals(str);
        }
    }

    public static boolean equals(EditText editText, @Nullable String str) {
        return equals(editText.getText(), str);
    }

    public static boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }

    public static boolean isEmpty(@Nullable String str) {
        return (str == null) || (str.trim().length() == 0);
    }
    public static String formatDateTime(long timeInMillis) {
        String pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), DATE_TIME_SKELETON);
        return DateFormat.format(pattern, timeInMillis).toString();
    }

    public static List<Integer> splitToIntList(@Nullable String str) {
        List<Integer> numbers = new ArrayList<>();

        if (str != null) {
            for (String split : str.split(",")) {
                numbers.add(Integer.parseInt(split.trim()));
            }
        }

        return numbers;
    }

    public static List<Long> splitToLongList(@Nullable String str) {
        List<Long> numbers = new ArrayList<>();

        if (str != null) {
            for (String split : str.split(",")) {
                numbers.add(Long.parseLong(split.trim()));
            }
        }

        return numbers;
    }

    public static String capitalize(@Nullable String str) {
        if ((str == null) || (str.length() == 0)) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
