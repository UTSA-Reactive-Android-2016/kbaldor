package edu.utsa.cs.kbaldor.examplesecuremessagingapp.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
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
    Rect senderBounds = new Rect();
    Rect ttlBounds = new Rect();
    Rect subjectBounds = new Rect();

    Paint senderPaint;
    Paint ttlPaint;
    Paint subjectPaint;
    Paint barPaint;
    Paint backPaint;

    ValueAnimator animator;

    int width = 0;
    int height = 0;

    public Message getMessage(){
        return message;
    }

    public void setMessage(Message message){
        this.message = message;
        senderPaint.getTextBounds(message.sender,0,message.sender.length(), senderBounds);
        subjectPaint.getTextBounds(message.subject,0,message.subject.length(), subjectBounds);

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

        computeLayout();
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
        senderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        senderPaint.setColor(textColor);
        senderPaint.setTextSize(textSize);
        senderPaint.setTypeface(Typeface.DEFAULT_BOLD);

        ttlPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ttlPaint.setColor(textColor);
        ttlPaint.setTextSize(textSize);
        ttlPaint.setTypeface(Typeface.MONOSPACE);

        subjectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subjectPaint.setColor(textColor);
        subjectPaint.setTextSize(textSize);

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

    int senderX = 0;
    int senderY = 0;
    int subjectX = 0;
    int subjectY = 0;

    private void computeLayout(){
        barLeft = getPaddingLeft();
        barTop = getPaddingTop();
        barRight = width - getPaddingRight();
        barBottom = height - getPaddingBottom();

        senderX = 0;
        senderY = -senderBounds.top;

        subjectX = 0;
        subjectY = height-subjectBounds.bottom;

//        Log.d("MessageView",senderBounds.top+" "+senderBounds.bottom+" "+senderBounds.exactCenterY());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(w!=width || h!=height) {
            width = w;
            height = h;
//            Log.d(LOG, "Message size is " + w + " " + h);
        }
    }

    String getTTLString(){
        long remaining = message.born_on_date+message.time_to_live - System.currentTimeMillis();
        long hours = remaining / 3600000;
        remaining -= hours*3600000;
        long minutes = remaining/60000;
        remaining -= minutes * 60000;
        long seconds = remaining / 1000;
        return String.format("%02d:%02d:%02d",hours,minutes,seconds);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        String ttlString = getTTLString();
        ttlPaint.getTextBounds(ttlString,0,ttlString.length(), ttlBounds);

//        Log.d(LOG,"Drawing Message");

        int offset = (int)((barRight-barLeft)*(1-message.percentLeftToLive()));

        canvas.drawRect(barLeft, barTop, barRight, barBottom, backPaint);
        canvas.drawRect(barLeft+offset, barTop, barRight, barBottom, barPaint);
        canvas.drawText(message.sender, senderX, senderY, senderPaint);
        canvas.drawText(ttlString, width-ttlBounds.right, -ttlBounds.top, ttlPaint);
        canvas.drawText(message.subject, subjectX, subjectY, subjectPaint);
    }
}
