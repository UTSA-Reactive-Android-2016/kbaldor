package edu.utsa.cs.kbaldor.examplesecuremessagingapp.views;

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

import edu.utsa.cs.kbaldor.examplesecuremessagingapp.R;
import edu.utsa.cs.kbaldor.examplesecuremessagingapp.models.Message;

/**
 * TODO: document your custom view class.
 */
public class MessageView extends View {
    static String LOG = "MessageView";

    Message message;
    int barColor;
    int backColor;
    int textColor;
    float textSize;
    Rect textBounds = new Rect();

    Paint textPaint;
    Paint barPaint;
    Paint backPaint;

    ValueAnimator animator;

    public void setMessage(Message message){
        this.message = message;
        textPaint.getTextBounds(message.body,0,message.body.length(), textBounds);

        animator = new ValueAnimator();

        long ttl_ms = message.time_to_live;

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
            backColor = a.getColor(R.styleable.MessageView_backColor, Color.GRAY);
            textColor = a.getColor(R.styleable.MessageView_textColor, Color.WHITE);
            textSize = a.getDimension(R.styleable.MessageView_textSize, 24);
        } finally {
            a.recycle();
        }

        init();
    }

//    Paint redPaint;
//    Paint yellowPaint;

    private void init() {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);

        backPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backPaint.setStyle(Paint.Style.FILL);
        backPaint.setColor(backColor);

        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setColor(barColor);

    }

    int barLeft = 0;
    int barTop = 0;
    int barRight = 0;
    int barBottom = 0;

    int textX = 0;
    int textY = 0;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(LOG,"Message size is "+w+" "+h);
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

        Log.d(LOG,"Drawing Message");

        int offset = (int)((barRight-barLeft)*(1-message.percentLeftToLive()));

        canvas.drawRect(barLeft, barTop, barRight, barBottom, backPaint);
        canvas.drawRect(barLeft+offset, barTop, barRight, barBottom, barPaint);
        canvas.drawText(message.sender, textX,textY, textPaint);
    }
}
