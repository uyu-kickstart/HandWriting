package net.azurewebsites.transam3y.handwriting;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Created by iguchiyusuke on 2015/04/09.
 */
public class DrawView extends View {
    private Paint paint = new Paint();
    private Path path = new Path();
    private Bitmap bitmap;
    private Canvas canvas;
    public static final float STROKEWIDTH = 20;
    private static int RECOGNIZE_IMAGE_WIDTH = 32;

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
        Log.v("BitmapSize1", "width:" + w + ",height:" + h);
        Log.v("BitmapSize2", "width:" + bitmap.getWidth() + ",height:" + bitmap.getHeight());
        Log.v("CanvasSize", "width:" + canvas.getWidth() + ",height:" + canvas.getHeight());
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
                Log.v("DrawPoint", "" + x);
                path.lineTo(x, y);
                canvas.drawPath(path, paint);
                invalidate();
                break;
            case MotionEvent.ACTION_DOWN:
                Log.v("DrawPoint", "" + x);
                path.reset();
                path.moveTo(x, y);
                invalidate();
                break;
        }
        return true;
    }

    public void clearCanvas() {
        canvas.drawColor(Color.WHITE);
        invalidate();
        initPaint();
    }

    public void recognize_1() {
        Bitmap recognizedBitmap = TrimBitmap(bitmap);
        if (recognizedBitmap == null) {
            return;
        } else {
            int[] colorDatas = MakeData(recognizedBitmap);
        }
        try {
            LoadAiueo();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {

        }
    }

    private Bitmap TrimBitmap(Bitmap sourceBitmap) {

        //左上(0,0)
        int h = sourceBitmap.getHeight(), w = sourceBitmap.getWidth();
        //変数(t,u)
        int t = 0, u;
        int[][] pixelColors = new int[h][w];
        boolean break_flag = false;
        while (t < w) {
            sourceBitmap.getPixels(pixelColors[t], 0, w, 0, t, w, 1);
            ++t;
        }
        int left = 0, bottom = 0, right = 0, top = 0;
        t = 0;
        while (t < w) {
            u = 0;
            while (u < h) {
                if (sourceBitmap.getPixel(t, u) == Color.BLACK) {
                    left = t;
                    break_flag = true;
                }
                ++u;
            }
            if (break_flag) break;
            ++t;
        }
        Log.v("values", "left  :" + left);
        t = 0;
        break_flag = false;
        while (t < h) {
            u = 0;
            while (u < w) {
                if (sourceBitmap.getPixel(u, t) == Color.BLACK) {
                    top = t;
                    break_flag = true;
                }
                ++u;
            }
            if (break_flag) break;
            ++t;
        }
        Log.v("values", "top   :" + top);
        t = w - 1;
        break_flag = false;
        while (t >= 0) {
            u = 0;
            while (u < h) {
                if (sourceBitmap.getPixel(t, u) == Color.BLACK) {
                    right = t;
                    break_flag = true;
                }
                ++u;
            }
            if (break_flag) break;
            --t;
        }
        Log.v("values", "right :" + right);
        t = h - 1;
        break_flag = false;
        while (t >= 0) {
            u = 0;
            while (u < w) {
                if (sourceBitmap.getPixel(u, t) == Color.BLACK) {
                    bottom = t;
                    break_flag = true;
                }
                ++u;
            }
            if (break_flag) break;
            --t;
        }
        Log.v("values", "bottom:" + bottom);
        if (left + top + right + bottom == 0) {
            return null;
        } else {
            return Bitmap.createBitmap(sourceBitmap, left, top, right - left, bottom - top);
        }
    }

    private int[] MakeData(Bitmap sourceImage) {
        int RIW2 = RECOGNIZE_IMAGE_WIDTH * RECOGNIZE_IMAGE_WIDTH;
        int[] colorDatas = new int[RIW2];
        //ピクセルが32の倍数ではないときに、元画像に余白を追加してピクセル数を32の倍数にしてそれを32分割する。
        int remainder_x = sourceImage.getWidth() % RECOGNIZE_IMAGE_WIDTH, remainder_y = sourceImage.getHeight() % RECOGNIZE_IMAGE_WIDTH;
        //一つの区画の大きさ決定
        int mesh_x = (sourceImage.getWidth() + 32 - remainder_x) / 32;
        int mesh_y = (sourceImage.getHeight() + 32 - remainder_y) / 32;
        //1区画のピクセル数
        int mesh = mesh_x * mesh_y;
        int[][] meshPixelArray = new int[mesh_x][mesh_y];
        int limit_x = mesh_x, limit_Y = mesh_y;
        int start_x = 0, start_y = 0;

        for (int i = 0; i < RIW2; i++) {
            for (int x = start_x; x < limit_x; x++) {
                for (int y = start_y; y < limit_Y; y++) {
                    //ここ
                    meshPixelArray[x][y] = 1;
                }
            }
        }

        return colorDatas;
    }

    private void LoadAiueo() throws IOException, JSONException, IllegalArgumentException {
        InputStream inputStream = getResources().openRawResource(R.raw.aiueo);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int counter;
        counter = inputStream.read();
        while (counter != -1) {
            byteArrayOutputStream.write(counter);
            counter = inputStream.read();
        }
        inputStream.close();
        JSONObject aiueObject = new JSONObject(byteArrayOutputStream.toString());
        JSONArray names = aiueObject.names();
        Log.v("JSONObject", "" + names);
    }
}
