package com.lwang.lrcview.lrc;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author lwang
 * @Date 2018/5/2 21:14
 * @Description
 */

public class LrcParse {

    public static LinkedHashMap<Long, String> parseLrc(String lrcText) {
        if (TextUtils.isEmpty(lrcText)) {
            return null;
        }

        LinkedHashMap<Long, String> maps = new LinkedHashMap<>();
        String[] array = lrcText.split("\\n");
        for (String line : array) {
            LinkedHashMap<Long, String> list = parseLine(line);
            if (list != null && !list.isEmpty()) {
                maps.putAll(list);
            }
        }
        return maps;
    }


    private static LinkedHashMap<Long, String> parseLine(String line) {
        if (TextUtils.isEmpty(line)) {
            return null;
        }

        line = line.trim();
        Matcher lineMatcher = Pattern.compile("((\\[\\d\\d:\\d\\d\\.\\d\\d\\])+)(.+)").matcher(line);
        if (!lineMatcher.matches()) {
            return null;
        }

        String times = lineMatcher.group(1);
        String text = lineMatcher.group(3);
        LinkedHashMap<Long, String> maps = new LinkedHashMap<>();

        Matcher timeMatcher = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d\\d)\\]").matcher(times);
        while (timeMatcher.find()) {
            long min = Long.parseLong(timeMatcher.group(1));
            long sec = Long.parseLong(timeMatcher.group(2));
            long mil = Long.parseLong(timeMatcher.group(3));
            long time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil * 10;

            maps.put(time, text);
//            Log.i("wang", maps.get(time));
        }
        return maps;
    }

}
