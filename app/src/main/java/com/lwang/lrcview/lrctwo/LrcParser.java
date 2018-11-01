package com.lwang.lrcview.lrctwo;

import android.text.TextUtils;
import android.text.format.DateUtils;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/10/18 0018.
 * 歌词解析工具
 * 目的： 将歌词文件的所有内容 --- List<LrcBean>
 * <p/>
 * 1. 按行解析 -- （开始时间，歌词内容）
 * 2. 按照开始时间的正序排序
 * 3. 获取演唱时长
 */
public class LrcParser {

    private static BufferedReader br;

    /**
     * 解析歌词文件
     *
     * @param lrcFile
     * @return
     */
    public static List<LrcBean> parseLrcFile(File lrcFile) {
        List<LrcBean> lrcBeans = new ArrayList<>();
        //一.如果歌词文件不存在
        if (lrcFile == null || !lrcFile.exists()) {
            lrcBeans.add(new LrcBean(0, "未找到歌词", 0));
            return lrcBeans;
        }
        //二.如果歌词文件存在

        try {
            //1. 按行解析 -- （开始时间，歌词内容）
//            BufferedReader br = new BufferedReader(new FileReader(lrcFile));
            //InputStreamReader 可以指定解析文件的编码格式（文件本身的格式）
            br = new BufferedReader(new InputStreamReader(new FileInputStream(lrcFile), getCharset(lrcFile)));
            String lineContent = "";
            while((lineContent= br.readLine())!=null){
                ArrayList<LrcBean> lineLrcBeans = parseLineContent(lineContent);
                lrcBeans.addAll(lineLrcBeans);
            }
            //  2. 按照开始时间的正序排序
            //如何对集合排序
            Collections.sort(lrcBeans);
            // 3. 获取演唱时长
            getDuration(lrcBeans);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return lrcBeans;
    }

    /**
     * 按行解析   -- （开始时间，歌词内容）
     * 1. [00:02.00]My Heart Will Go On
     *      [00:02.00
     *      My Heart Will Go On
     * 2. [00:10.00]
     *      [00:10.00
     *
     * 3. [01:32.23][02:51.73][03:50.85]And my heart will go on and on
     *      按照】分割
     *        [01:32.23
     *        [02:51.73
     *        [03:50.85
     *        And my heart will go on and on
     *      按照【分割
     *          01:32.23]
     *          02:51.73
     *          03:50.85]And my heart will go on and on
     *
     * 步骤：
     * 1. 按照 】 分割
     * 2. 解析没有歌词内容
     * 3. 解析有歌词内容
     * 4. 获取歌词内容
     * 5. 循环剩下的开始时间，分别取出
     * @param lineContent
     * @return
     */
    private static ArrayList<LrcBean> parseLineContent(String lineContent) {
        ArrayList<LrcBean> lineLrcBeans = new ArrayList<>();
        //1. 按照 】 分割
        String[] split = lineContent.split("]");
//        2. 解析没有歌词内容
        if(split.length==1){
            lineLrcBeans.add(new LrcBean(parseStartTime(split[0]),"",0));
            return lineLrcBeans;
        }
//        * 3. 解析有歌词内容
//        * 4. 获取歌词内容
        String content = split[split.length-1];
//        * 5. 循环剩下的开始时间，分别取出
        for (int i=0;i<split.length-1;i++){
            String startTime = split[i];
            lineLrcBeans.add(new LrcBean(parseStartTime(startTime),content,0));
        }
        return lineLrcBeans;
    }

    /**
     * 解析开始时间
     * @param startTime  [01:32.23  ---- ms
     *                   1. 去掉 【
     *                   2.按照:分割
     *                   3. m * 60 *1000
     *                   4. s * 10000
     * @return
     */
    private static int parseStartTime(String startTime) {
        try{
//            1. 去掉 【
            startTime = startTime.replace("[","");
//            2.按照:分割
            String[] split = startTime.split(":");
//            3. m * 60 *1000
            int min = Integer.parseInt(split[0]);
//            4. s * 10000
            float sec = Float.parseFloat(split[1]);
            return (int) (min*60*1000+sec*1000);
        }catch (Exception e){
            return -1;
        }
    }


    /**
     * 获取演唱时长 （下一句歌词开始时间 - 当前歌词开始时间）
     * @param lrcBeans
     */
    private static void getDuration(List<LrcBean> lrcBeans) {
        for (int i=0;i<lrcBeans.size();i++){
            LrcBean currentLrcBean = lrcBeans.get(i);
            if((i+1)<lrcBeans.size()){ // 不用给最后一个歌词赋演唱时长 （歌词最后一句只有开始时间，不需要唱）
                LrcBean nextLrcBean = lrcBeans.get(i+1);
                currentLrcBean.duration = nextLrcBean.startTime - currentLrcBean.startTime;
            }
        }

    }

    /**
     * 获取当前文件的编码格式 避免读取的歌词文件，是乱码
     * @param file
     * @return
     */
    public static String getCharset(File file) {
        String charset = "gbk";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1)
                return charset;
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8";
                checked = true;
            }
            bis.reset();
            if (!checked) {
                int loc = 0;
                while ((read = bis.read()) != -1) {
                    loc++;
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF)
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF)
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return charset;
    }



    public static List<LrcBean> parseLrc(String lrcText) {
        if (TextUtils.isEmpty(lrcText)) {
            return null;
        }

        List<LrcBean> entryList = new ArrayList<>();
        String[] array = lrcText.split("\\n");
        for (String line : array) {
            List<LrcBean> list = parseLine(line);
            if (list != null && !list.isEmpty()) {
                entryList.addAll(list);
            }
        }

        Collections.sort(entryList);
        return entryList;
    }

    private static List<LrcBean> parseLine(String line) {
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
        List<LrcBean> entryList = new ArrayList<>();

        Matcher timeMatcher = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d\\d)\\]").matcher(times);
        while (timeMatcher.find()) {
            long min = Long.parseLong(timeMatcher.group(1));
            long sec = Long.parseLong(timeMatcher.group(2));
            long mil = Long.parseLong(timeMatcher.group(3));
            long time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil * 10;
            entryList.add(new LrcBean((int)time, text, 0));
        }
        return entryList;
    }

}
