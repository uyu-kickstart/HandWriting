package net.azurewebsites.transam3y.handwriting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by iguchiyusuke on 2015/04/09.
 */
public class DrawView extends View {
    private Paint paint = new Paint();
    private Path path = new Path();
    private Bitmap bitmap;
    private Canvas canvas;
    private ArrayList<Path> pathList = new ArrayList<Path>();
    public static final float STROKEWIDTH = 10;

    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, AttributeSet attributes) {
        this(context, attributes, R.attr.drawView);
    }

    public DrawView(Context context, AttributeSet attributes, int defStyleAttr) {
        super(context, attributes, defStyleAttr);
        initPaint();
    }

    private void initPaint() {
        path = new Path();
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(STROKEWIDTH);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    //サイズ変わった時のみ
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        Log.v("onSizeChanged", "onSizeChanged");
    }

    //描画されると毎回
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawPath(path, paint);
        Log.v("onDraw", "onDraw");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                path.lineTo(x, y);
                pathList.add(path);
                canvas.drawPath(path, paint);
                invalidate();
                break;
            case MotionEvent.ACTION_DOWN:
                path.reset();
                path.moveTo(x, y);
                invalidate();
                break;
        }
        return true;
    }

    public void clearCanvas() {
        paint.setColor(Color.WHITE);
        Log.v("PATHLIST", "" + pathList.size());
        for (int i = 0; i < pathList.size(); i++) {
            canvas.drawPath(pathList.get(i), paint);
        }
        invalidate();
        pathList.clear();
        initPaint();
    }
}
