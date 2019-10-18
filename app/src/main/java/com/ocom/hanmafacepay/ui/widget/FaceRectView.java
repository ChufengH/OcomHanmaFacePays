package com.ocom.hanmafacepay.ui.widget;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.hanma.faceservice.aidl.Face;


/*定义一个画矩形框的类*/
public class FaceRectView extends View {
    private Face rect;

    public FaceRectView(Context context) {
        this(context, null);
    }

    public FaceRectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceRectView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPaint(context);
    }

    private void initPaint(Context context) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(7);
        mPaint.setColor(Color.BLUE);
    }

    private Paint mPaint;

    /**
     * 开始画矩形框
     *
     * @param rect
     */
    public void drawFaceRect(Face rect, int width, int pwidth) {
        this.rect = rect;
        if (rect != null) {
            Log.e("face rect", "rect.left=" + rect.left + "/rect.right=" + rect.right + "/rect.top=" + rect.top + "/rect.bottom=" + rect.bottom + "width=" + width);
            //将屏幕人脸框转换为视频区域的人脸框
            rect.left = (int) (((float) (rect.left) * 1.25 * 100) / 100);
            rect.right = (int) (((float) (rect.right) * 1.25 * 100) / 100);//rect.right * width / pwidth;
            rect.top = (int) ((((float) (rect.top) * 1.25 * 100) / 100));//rect.top * width / pwidth;
            rect.bottom = (int) ((((float) (rect.bottom) * 1.25 * 100) / 100));//rect.bottom * width / pwidth;
            //   Log.e("rect","rect.left="+rect.left+"/rect.right="+rect.right+"/rect.top="+rect.top+"/rect.bottom="+rect.bottom+"width="+width);
            //在主线程发起绘制请求

            postInvalidate();
        }
    }

    public void clearRect() {
        if (rect != null) {
            rect = null;
            postInvalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (rect != null) {
            /**
             * 左上角的竖线
             */
            canvas.drawLine(rect.left, rect.top, rect.left, rect.top + 40, mPaint);
            /**
             * 左上角的横线
             */
            canvas.drawLine(rect.left, rect.top, rect.left + 40, rect.top, mPaint);

            /**
             * 右上角的竖线
             */
            canvas.drawLine(rect.right, rect.top, rect.right - 40, rect.top, mPaint);
            /**
             * 右上角的横线
             */
            canvas.drawLine(rect.right, rect.top, rect.right, rect.top + 40, mPaint);
            /**
             * 左下角的竖线
             */
            canvas.drawLine(rect.left, rect.bottom, rect.left, rect.bottom - 40, mPaint);
            /**
             * 左下角的横线
             */
            canvas.drawLine(rect.left, rect.bottom, rect.left + 40, rect.bottom, mPaint);

            /**
             * 右下角的竖线
             */
            canvas.drawLine(rect.right, rect.bottom, rect.right, rect.bottom - 40, mPaint);
            /**
             * 右下角的横线
             */
            canvas.drawLine(rect.right, rect.bottom, rect.right - 40, rect.bottom, mPaint);
        } else {
            //清除掉
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
    }
}