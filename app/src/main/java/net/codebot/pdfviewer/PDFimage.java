package net.codebot.pdfviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.view.View.OnTouchListener;

import java.util.ArrayList;

@SuppressLint("AppCompatCustomView")
public class PDFimage extends ImageView {

    final String LOGNAME = "pdf_image";

    // drawing path
    MyPath path = null;
    ArrayList<MyPath> paths = new ArrayList();
    Region r = new Region();
    RectF rectF = new RectF();

    // image to display
    Bitmap bitmap;
    Paint paint = new Paint(Color.BLUE);

    // brushes
    int brushID;
    Paint pen = new Paint(); // ID 1
    Paint highlighter = new Paint(); // ID 2

    // page num
    int currPage;

    // constructor
    public PDFimage(Context context) {
        super(context);

        brushID = 1;

        pen.setStyle(Paint.Style.STROKE);
        pen.setStrokeWidth(3);
        pen.setColor(Color.BLUE);

        highlighter.setStyle(Paint.Style.STROKE);
        highlighter.setStrokeWidth(10);
        highlighter.setAlpha(20);
        highlighter.setColor(Color.YELLOW);

    }

    // capture touch events (down/move/up) to create a path
    // and use that to create a stroke that we can draw
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(LOGNAME, "Action down");
                if(brushID == 3) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();

                    for(MyPath path : paths) {
                        path.computeBounds(rectF, true);
                        r.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
                        if(r.contains(x, y)) {
                            paths.remove(path);
                        }
                    }
                } else {
                    path = new MyPath();
                    path.setId(brushID);
                    path.setPage(currPage);
                    paths.add(path);
                    path.moveTo(event.getX(), event.getY());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(LOGNAME, "Action move");
                if (brushID != 3) {
                    path.lineTo(event.getX(), event.getY());
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.d(LOGNAME, "Action up");
                break;
        }
        return true;
    }

    // set image as background
    public void setImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    // set brush characteristics
    // e.g. color, thickness, alpha
    public void setBrush(Paint paint) {
        this.paint = paint;
    }

    // set brush ID
    public void setBrushID(int ID) {
        this.brushID = ID;
    }

    // set page num
    public void setCurrPage(int page) {
        this.currPage = page;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // draw background
        if (bitmap != null) {
            this.setImageBitmap(bitmap);
        }
        // draw lines over it
        for (MyPath path : paths) {
            if(path.page == currPage) {
                switch (path.id) {
                    case 1: //pen
                        canvas.drawPath(path, pen);
                        break;
                    case 2: //highlighter
                        canvas.drawPath(path, highlighter);
                        break;
                }
            }

        }
        super.onDraw(canvas);
    }
}
