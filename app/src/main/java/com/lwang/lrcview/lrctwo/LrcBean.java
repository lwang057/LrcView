package com.lwang.lrcview.lrctwo;

/**
 * Created by Administrator on 2016/10/18 0018.
 * 歌词对象
 */
public class LrcBean implements Comparable<LrcBean> {
    /**
     * 1. 开始时间
     2. 歌词内容
     3. 歌词演唱时长 （下一句歌词开始时间 - 当前歌词开始时间）
     */
    public int startTime;
    public String content;
    public int duration;

    public LrcBean(int startTime, String content, int duration) {
        this.startTime = startTime;
        this.content = content;
        this.duration = duration;
    }

    public LrcBean(int startTime, String content) {
        this.startTime = startTime;
        this.content = content;
    }

    public LrcBean() {
        super();
    }

    @Override
    public String toString() {
        return "LrcBean{" +
                "startTime=" + startTime +
                ", content='" + content + '\'' +
                ", duration=" + duration +
                '}';
    }

    @Override
    public int compareTo(LrcBean another) {
        //按照开始时间正序排序
        return this.startTime - another.startTime;
    }
}
