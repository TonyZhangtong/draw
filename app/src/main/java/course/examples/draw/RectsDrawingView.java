package course.examples.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashSet;
import java.util.Random;

/**
 * Created by South on 11/30/2016.
 */

public class RectsDrawingView extends View {

    private static final String TAG = "RectDrawingView";

    /** Main bitmap */
    private Bitmap mBitmap = null;

    private Rect mMeasuredRect;

    /** Stores data about single circle */
    private static class RectArea {
        float length;
        float width;
        float X;
        float Y;

        RectArea(float X, float Y, float length, float width) {
            this.X = X;
            this.Y = Y;
            this.length = length;
            this.width = width;
        }

        @Override
        public String toString() {
            return "Rectangle[" + X + ", " + Y + ", "  + length + "," + width + "]";
        }
    }

    /** Paint to draw rects */
    private Paint mRectPaint;

    private final Random mRadiusGenerator = new Random();
    // Radius limit in pixels
    private final static int RECT_LIMIT = 50;

    private static final int RECTS_LIMIT = 5;

    /** All available circles */
    private HashSet<RectArea> mRects = new HashSet<RectArea>(RECTS_LIMIT);
    private SparseArray<RectArea> mCirclePointer = new SparseArray<RectArea>(RECTS_LIMIT);

    /**
     * Default constructor
     *
     * @param ct {@link Context}
     */
    public RectsDrawingView(final Context ct) {
        super(ct);

        init(ct);
    }

    public RectsDrawingView(final Context ct, final AttributeSet attrs) {
        super(ct, attrs);

        init(ct);
    }

    public RectsDrawingView(final Context ct, final AttributeSet attrs, final int defStyle) {
        super(ct, attrs, defStyle);

        init(ct);
    }

    private void init(final Context ct) {
        // Generate bitmap used for background
        mBitmap = BitmapFactory.decodeResource(ct.getResources(), R.drawable.up_image);

        mRectPaint = new Paint();

        mRectPaint.setColor(Color.GREEN);
        mRectPaint.setStrokeWidth(40);
        mRectPaint.setStyle(Paint.Style.FILL);
    }


    @Override
    public void onDraw(final Canvas canv) {
        // background bitmap to cover all area
        canv.drawBitmap(mBitmap, null, mMeasuredRect, null);

        for (RectArea rect : mRects) {
            canv.drawRect(rect.X - rect.length/2, rect.Y - rect.width/2, rect.X + rect.length/2, rect.Y + rect.width/2, mRectPaint);
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        boolean handled = false;

        RectArea touchedRect;
        int xTouch;
        int yTouch;
        int pointerId;
        int actionIndex = event.getActionIndex();

        // get touch event coordinates and make transparent circle from it
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // it's the first pointer, so clear all existing pointers data
                clearCirclePointer();

                xTouch = (int) event.getX(0);
                yTouch = (int) event.getY(0);

                // check if we've touched inside some circle
                touchedRect = obtainTouchedRect(xTouch, yTouch);
                touchedRect.X = xTouch;
                touchedRect.Y = yTouch;
                touchedRect.length = 50;
                touchedRect.width = 50;
                mCirclePointer.put(event.getPointerId(0), touchedRect);

                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                Log.w(TAG, "Pointer down");
                // It secondary pointers, so obtain their ids and check circles
                pointerId = event.getPointerId(actionIndex);

                xTouch = (int) event.getX(actionIndex);
                yTouch = (int) event.getY(actionIndex);

                // check if we've touched inside some circle
                touchedRect = obtainTouchedRect(xTouch, yTouch);

                mCirclePointer.put(pointerId, touchedRect);
                touchedRect.X = xTouch;
                touchedRect.Y = yTouch;
                touchedRect.length = 50;
                touchedRect.width = 50;
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_MOVE:
                final int pointerCount = event.getPointerCount();

                Log.w(TAG, "Move");

                for (actionIndex = 0; actionIndex < pointerCount; actionIndex++) {
                    // Some pointer has moved, search it by pointer id
                    pointerId = event.getPointerId(actionIndex);

                    xTouch = (int) event.getX(actionIndex);
                    yTouch = (int) event.getY(actionIndex);

                    touchedRect = mCirclePointer.get(pointerId);

                    if (null != touchedRect) {
                        touchedRect.X = xTouch;
                        touchedRect.Y = yTouch;
                    }
                }
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_UP:
                clearCirclePointer();
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                // not general pointer was up
                pointerId = event.getPointerId(actionIndex);

                mCirclePointer.remove(pointerId);
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_CANCEL:
                handled = true;
                break;

            default:
                // do nothing
                break;
        }

        return super.onTouchEvent(event) || handled;
    }

    /**
     * Clears all CircleArea - pointer id relations
     */
    private void clearCirclePointer() {
        Log.w(TAG, "clearCirclePointer");

        mCirclePointer.clear();
    }

    /**
     * Search and creates new (if needed) circle based on touch area
     *
     * @param xTouch float x of touch
     * @param yTouch float y of touch
     *
     * @return obtained {@link RectArea}
     */
    private RectArea obtainTouchedRect(final float xTouch, final float yTouch) {
        RectArea touchedRect = getTouchedRect(xTouch, yTouch);

        if (null == touchedRect) {
            touchedRect = new RectArea(xTouch-25, yTouch-25, xTouch + 25, yTouch + 25);

            if (mRects.size() == RECTS_LIMIT) {
                Log.w(TAG, "Clear all circles, size is " + mRects.size());
                // remove first circle
                mRects.clear();
            }

            Log.w(TAG, "Added rect " + touchedRect);
            mRects.add(touchedRect);
        }

        return touchedRect;
    }

    /**
     * Determines touched circle
     *
     * @param xTouch int x touch coordinate
     * @param yTouch int y touch coordinate
     *
     * @return {@link RectArea} touched circle or null if no circle has been touched
     */
    private RectArea getTouchedRect(final float xTouch, final float yTouch) {
        RectArea touched = null;

        for (RectArea rect : mRects) {
            if ((xTouch >= rect.X-25  && xTouch <= rect.X + 25 && yTouch  >= rect.Y -25 && yTouch <= rect.Y + 25)) {
                Log.i("Touched","");
                touched = rect;
                break;
            }
        }

        return touched;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mMeasuredRect = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }
}