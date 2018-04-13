package me.ajax.loadingview.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

/**
 * Created by aj on 2018/4/2
 */

public class LoadingView extends View {

    Paint bigBlockPaint = new Paint();
    Paint smallBlockPaint;

    int bigBlockRadius = dp2Dx(20);
    int bigBlockLength = bigBlockRadius * 2;

    RectF rectF = new RectF(-bigBlockRadius, -bigBlockRadius, bigBlockRadius, bigBlockRadius);
    RectF leftSmallRectF = new RectF(rectF.left - dp2Dx(5), rectF.bottom + dp2Dx(10),
            rectF.left + dp2Dx(5), rectF.bottom + bigBlockRadius);
    RectF centerSmallRectF = new RectF(rectF.right - dp2Dx(5), rectF.bottom + dp2Dx(10),
            rectF.right + dp2Dx(5), rectF.bottom + bigBlockRadius);
    RectF rightSmallRectF = new RectF(rectF.right + bigBlockLength - dp2Dx(5), rectF.bottom + dp2Dx(10),
            rectF.right + bigBlockLength + dp2Dx(5), rectF.bottom + bigBlockRadius);

    Path path = new Path();
    Path leftSmallPath = new Path();
    Path centerSmallPath = new Path();
    Path rightSmallPath = new Path();

    int animationValue = 0;
    float animatedFraction;
    ValueAnimator animator;

    public LoadingView(Context context) {
        super(context);
        init();
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init() {


        setLayerType(View.LAYER_TYPE_SOFTWARE, null);//关闭硬件加速

        //画笔
        bigBlockPaint.setColor(Color.WHITE);
        bigBlockPaint.setPathEffect(new CornerPathEffect(dp2Dx(9)));

        smallBlockPaint = new Paint(bigBlockPaint);
        smallBlockPaint.setPathEffect(new CornerPathEffect(dp2Dx(3)));

        post(new Runnable() {
            @Override
            public void run() {

                animator = ValueAnimator.ofInt(0, 90);
                animator.setDuration(600);
                animator.setInterpolator(new AccelerateInterpolator());
                animator.setRepeatCount(Integer.MAX_VALUE - 1);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        animationValue = (int) animation.getAnimatedValue();
                        animatedFraction = animation.getAnimatedFraction();
                        invalidateView();
                    }
                });
                animator.start();
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int mWidth = getWidth();
        int mHeight = getHeight();

        canvas.save();
        canvas.translate(mWidth / 2 - bigBlockLength * animatedFraction, mHeight / 2);

        //绘制大正方形
        canvas.save();
        canvas.rotate(animationValue, rectF.right, rectF.bottom);
        canvas.drawPath(rectToPath(rectF, path), bigBlockPaint);
        canvas.restore();

        //绘制小正方形
        canvas.clipRect(-dp2Dx(25) + bigBlockLength * animatedFraction, dp2Dx(30),
                bigBlockLength + dp2Dx(5) + bigBlockLength * animatedFraction, dp2Dx(40));
        canvas.drawPath(rectToPath(leftSmallRectF, leftSmallPath), smallBlockPaint);
        canvas.drawPath(rectToPath(rightSmallRectF, rightSmallPath), smallBlockPaint);
        canvas.drawPath(rectToPath(centerSmallRectF, centerSmallPath), smallBlockPaint);

        canvas.restore();
    }

    Path rectToPath(RectF rectF, Path path) {
        if (rectF == null || path == null) {
            return new Path();
        }
        path.reset();
        path.moveTo(rectF.left, rectF.top);
        path.lineTo(rectF.right, rectF.top);
        path.lineTo(rectF.right, rectF.bottom);
        path.lineTo(rectF.left, rectF.bottom);
        path.close();

        return path;
    }


    int dp2Dx(int dp) {
        return (int) (getResources().getDisplayMetrics().density * dp);
    }

    void l(Object o) {
        Log.e("######", o.toString());
    }


    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            //  当前线程是主UI线程，直接刷新。
            invalidate();
        } else {
            //  当前线程是非UI线程，post刷新。
            postInvalidate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimAndRemoveCallbacks();
    }

    private void stopAnimAndRemoveCallbacks() {

        if (animator != null) animator.end();

        Handler handler = this.getHandler();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
