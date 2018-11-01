package com.lwang.lrcview.lrctwo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.lwang.lrcview.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 自定义的歌词同步控件
 *
 * @author Administrator
 *         <p>
 *         1. 居中（水平、垂直）画一行歌词
 *         2. 居中（水平）画多行歌词
 *         3. 按行滚动
 *         4. 平滑滚动
 */
public class LrcTextView extends TextView {
    private Paint paint;
    private int lrcTextViewHalfHeight;
    private int lrcTextViewHalfWidth;

    //歌词集合
    private List<LrcBean> lrcBeans = new ArrayList<LrcBean>();
    //当前行索引
    private int currentLine = 0;
    private int lineHeight = 0;
    //当前播放进度
    private int currentPosition;
    private int white;
    private int green;
    private Paint mPaint;

    public LrcTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        //获取color颜色
        white = getResources().getColor(R.color.colorPrimaryDark);
        green = getResources().getColor(R.color.green);
        int testSize = (int) getResources().getDimension(R.dimen.lyric_text_size);
        lineHeight = (int) getResources().getDimension(R.dimen.lyric_line_height);
        paint = new Paint();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);


        mPaint.setTextSize(testSize);
        //颜色
        paint.setColor(green);
        //文字大小
        paint.setTextSize(testSize);

        lineHeight = 50;  //TODO

        //假数据  TODO 
//        for (int i = 0; i < 10; i++) {
//        }
        lrcBeans.add(new LrcBean(0, "我0是是歌词是", 0));
        lrcBeans.add(new LrcBean(0, "我0safs是歌词", 0));
        lrcBeans.add(new LrcBean(0, "我0as是dffdsa是as歌词", 0));
        lrcBeans.add(new LrcBean(0, "我0a是ssdf是歌词", 0));
        lrcBeans.add(new LrcBean(0, "是我0阿道夫阿是斯蒂芬是歌词fasdfasdd", 0));
        lrcBeans.add(new LrcBean(0, "我是0阿斯蒂芬是歌词", 0));
        currentLine = 3;
    }

    public LrcTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LrcTextView(Context context) {
        super(context);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);//测量
        //测量完成
        lrcTextViewHalfHeight = getMeasuredHeight() / 2;
        lrcTextViewHalfWidth = getMeasuredWidth() / 2;
    }

    /**
     * 结论： centerX = LrcTextView.width/2 - text.width/2
     * centerY = LrcTextView.height/2 + text.height/2
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //没有歌词
        if (lrcBeans.size() == 1) {
            drawCenterSingleLine(canvas);
            return;
        }
        //有歌词
        if (lrcBeans.size() > 1) {
            //平滑滚动
            /**
             * 1. 已经消耗时间 --  当前进度 - 开始时间
             2. 已经消耗时间百分百 -- 已经消耗时间/总时长
             3. 滚动距离 -- 已经消耗时间百分百*行高
             4. 滚
             */
//            LrcBean currentLrcBean = lrcBeans.get(currentLine);
//            float costTime = currentPosition - currentLrcBean.startTime;
//            float costPercent = costTime / currentLrcBean.duration;
//            float disY = costPercent * lineHeight;
//            canvas.translate(0,-disY);//dx,dy 水平/垂直方向的滚动距离

            drawCenterMultLine(canvas);
        }

    }

    private void drawCenterMultLine(Canvas canvas) {
        for (int i = 0; i < lrcBeans.size(); i++) {
            LrcBean lrcBean = lrcBeans.get(i);
            //居中（水平）画一行歌词
            String text = lrcBean.content;

            //getTextBounds获取文本的边界
            //(1)文本内容(2)start，end 开始索引结束索引
            Rect bounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), bounds);//会给bounds赋值
            int textHalfWidth = bounds.width() / 2;
            int textHalfHeight = bounds.height() / 2;
            textHalfWidth = (int) (paint.measureText(text) / 2);//兼容预览sdk16
            int centerX = lrcTextViewHalfWidth - textHalfWidth;
            int centerY = lrcTextViewHalfHeight + textHalfHeight;
            /**
             * 计算每一行与居中行相差几个行高：
             每一行在集合中的索引 - 居中行的索引
             */
            int disLine = i - currentLine;
            int drawY = centerY + disLine * lineHeight;
            //如何画文本
            //(1)画的文本内容，(2.3)xy坐标(4).画笔
            if (i == currentLine) {
                paint.setColor(green);
                canvas.drawText(text, centerX, drawY, paint);
            } else {

//                Rect bound = new Rect();
//                paint.getTextBounds(text, 0, text.length(), bounds);//会给bounds赋值
//                int textHalfWidths = bound.width() / 2;
//                textHalfWidth = (int) (paint.measureText(text) / 2);//兼容预览sdk16
//                int centerXX = lrcTextViewHalfWidth - textHalfWidth;

                String substring = "";
                String substring2 = "";
                String substring3 = "";
                String substring4 = "";

                int widths = 0;
                int b = 0;
                ArrayList<Integer> abc = new ArrayList<>();
                for (int a = 0; a < text.length(); a++) {
                    String c = String.valueOf(text.charAt(a));
                    if (c.equals("是")) {
                        abc.add(a);
                    }
                }

                if (1 == abc.size()) {
                    int a = abc.get(0);
                    substring = text.substring(0, a);
                    paint.setColor(white);
                    canvas.drawText(substring, centerX, drawY, paint);

                    widths = (int) mPaint.measureText(substring);
                    canvas.drawText("是", centerX + widths, drawY, mPaint);

                    substring2 = text.substring(a + 1, text.length());
                    int sss = (int) mPaint.measureText("是");
                    canvas.drawText(substring2, centerX + widths + sss, drawY, paint);
                } else if (2 == abc.size()) {
                    int a = abc.get(0);
                    int a1 = abc.get(1);

                    substring = text.substring(0, a);
                    paint.setColor(white);
                    canvas.drawText(substring, centerX, drawY, paint);

                    widths = (int) mPaint.measureText(substring);
                    canvas.drawText("是", centerX + widths, drawY, mPaint);

                    substring2 = text.substring(a + 1, a1);
                    int sss = (int) mPaint.measureText("是");
                    canvas.drawText(substring2, centerX + widths + sss, drawY, paint);

                    int ssss = (int) mPaint.measureText(substring2);
                    canvas.drawText("是", centerX + widths + sss + ssss, drawY, mPaint);

                    substring3 = text.substring(a1 + 1, text.length());
                    canvas.drawText(substring3, centerX + widths + sss + sss + ssss, drawY, paint);
                } else if (3 == abc.size()) {
                    int a = abc.get(0);
                    int a1 = abc.get(1);
                    int a2 = abc.get(2);

                    substring = text.substring(0, a);
                    paint.setColor(white);
                    canvas.drawText(substring, centerX, drawY, paint);

                    widths = (int) mPaint.measureText(substring);
                    canvas.drawText("是", centerX + widths, drawY, mPaint);

                    substring2 = text.substring(a + 1, a1);
                    int sss = (int) mPaint.measureText("是");
                    canvas.drawText(substring2, centerX + widths + sss, drawY, paint);

                    int ssss = (int) mPaint.measureText(substring2);
                    canvas.drawText("是", centerX + widths + sss + ssss, drawY, mPaint);

                    substring3 = text.substring(a1 + 1, a2);
                    canvas.drawText(substring3, centerX + widths + sss + sss + ssss, drawY, paint);

                    int sssss = (int) mPaint.measureText(substring3);
                    canvas.drawText("是", centerX + widths + sss + sss + ssss + sssss, drawY, mPaint);

                    substring4 = text.substring(a2 + 1, text.length());
                    canvas.drawText(substring4, centerX + widths + sss + sss + sss + ssss + sssss, drawY, paint);
                }


            }

        }
    }

    private void drawCenterSingleLine(Canvas canvas) {
        //1. 居中（水平、垂直）画一行歌词
        String text = "未找到歌词";
        //getTextBounds获取文本的边界
        //(1)文本内容(2)start，end 开始索引结束索引
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);//会给bounds赋值
        int textHalfWidth = bounds.width() / 2;
        int textHalfHeight = bounds.height() / 2;
        textHalfWidth = (int) (paint.measureText(text) / 2);//兼容预览sdk16
        int centerX = lrcTextViewHalfWidth - textHalfWidth;
        int centerY = lrcTextViewHalfHeight + textHalfHeight;
        //如何画文本
        //(1)画的文本内容，(2.3)xy坐标(4).画笔
        canvas.drawText(text, centerX, centerY, paint);
    }

    /**
     * 更新当前行索引 currentLine
     * 根据当前进度 ， 判断如果大于当前歌词开始时间，并且小于下一句歌词开始时间
     * currentLine = 当前歌词的索引
     */
    public void updateCurrentLine(int currentPosition) {
        this.currentPosition = currentPosition;
        if (lrcBeans.size() == 1) {
            //没有歌词
            currentLine = 0;
            invalidate();
            return;
        }
        for (int i = 0; i < lrcBeans.size(); i++) {
            LrcBean currentLrcBean = lrcBeans.get(i);
            if ((i + 1) < lrcBeans.size()) {
                LrcBean nextLrcBean = lrcBeans.get(i + 1);
                if (currentPosition > currentLrcBean.startTime && currentPosition < nextLrcBean.startTime) {
                    currentLine = i;
                    break;
                }
            }

        }
        //刷新view -- ondraw
        invalidate();

    }

    public void setLrcBeans(List<LrcBean> lrcBeans) {
        Log.i("wang", "lrcBeans.size():::" + lrcBeans.size());
        this.lrcBeans = lrcBeans;
    }
/**
 *   paint.setColor(white);
 canvas.drawText(text, centerX, drawY, paint);
 int widths = 0;
 for (int a = 0; a < text.length(); a++) {
 String c = String.valueOf(text.charAt(a));
 if (c.equals("词")) {
 String substring = text.substring(0, a);
 widths = (int) mPaint.measureText(substring);
 }
 }
 canvas.drawText("词", centerX + widths, drawY, mPaint);
 */
}
