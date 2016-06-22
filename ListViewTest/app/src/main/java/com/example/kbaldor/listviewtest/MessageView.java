package com.example.kbaldor.listviewtest;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class MessageView extends View {
    Message message;
    int barColor;
    float textSize;
    Rect  textBounds = new Rect();

    ValueAnimator animator;

    public void setMessage(Message message){
        this.message = message;
        textPaint.getTextBounds(message.getMessage(),0,message.getMessage().length(), textBounds);

        animator = new ValueAnimator();

        long ttl_ms = message.timeToLive_ms;

        animator.setDuration(ttl_ms);

        animator.setFloatValues(0,1);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });

        animator.start();

    }

    public MessageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MessageView,
                0, 0);

        try {
            barColor = a.getColor(R.styleable.MessageView_barColor, Color.BLUE);
            textSize = a.getDimension(R.styleable.MessageView_textSize, 24);
        } finally {
            a.recycle();
        }

        init();
    }

    Paint textPaint;
    Paint barPaint;
//    Paint redPaint;
//    Paint yellowPaint;

    private void init() {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(textSize);

        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setColor(barColor);

//        redPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        redPaint.setStyle(Paint.Style.FILL);
//        redPaint.setColor(Color.RED);
//
//        yellowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        yellowPaint.setStyle(Paint.Style.FILL);
//        yellowPaint.setColor(Color.YELLOW);
    }

    int barLeft = 0;
    int barTop = 0;
    int barRight = 0;
    int barBottom = 0;

    int textX = 0;
    int textY = 0;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        barLeft = getPaddingLeft();
        barTop = getPaddingTop();
        barRight = w - getPaddingRight();
        barBottom = h - getPaddingBottom();

        textX = w/2 - textBounds.centerX();
        textY = h-getPaddingBottom()-textBounds.bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int offset = (int)((barRight-barLeft)*(1-message.percentLeftToLive()));

        canvas.drawRect(barLeft+offset, barTop, barRight, barBottom, barPaint);
        canvas.drawText(message.getMessage(), textX,textY, textPaint);
//        canvas.drawRect(0,0,20,20,redPaint);
//        canvas.drawRect(100,50,110,60,yellowPaint);

    }
}
