package net.codebot.pdfviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MotionEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

// PDF sample code from
// https://medium.com/@chahat.jain0/rendering-a-pdf-document-in-android-activity-fragment-using-pdfrenderer-442462cb8f9a
// Issues about cache etc. are not at all obvious from documentation, so we should expect people to need this.
// We may wish to provide them with this code.

public class MainActivity extends AppCompatActivity {

    final String LOGNAME = "pdf_viewer";
    final String FILENAME = "shannon1948.pdf";
    final int FILERESID = R.raw.shannon1948;

    // manage the pages of the PDF, see below
    PdfRenderer pdfRenderer;
    private ParcelFileDescriptor parcelFileDescriptor;
    private PdfRenderer.Page currentPage;

    // custom ImageView class that captures strokes and draws them over the image
    PDFimage pageImage;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout layout = findViewById(R.id.pdfLayout);

        Button prevPage = findViewById(R.id.prevpage);
        Button nextPage = findViewById(R.id.nextpage);

        ImageButton penButton = findViewById(R.id.penButton);
        ImageButton highlighterButton = findViewById(R.id.highlighterButton);
        ImageButton eraserButton = findViewById(R.id.eraserButton);

        final TextView fileName = findViewById(R.id.fileName);
        final TextView pageNum = findViewById(R.id.page);
        final String[] pageNumText = new String[1];

        pageImage = new PDFimage(this);
        layout.addView(pageImage);
        layout.setEnabled(true);
        pageImage.setMinimumWidth(1000);
        pageImage.setMinimumHeight(2000);

        // open page 0 of the PDF
        // it will be displayed as an image in the pageImage (above)
        try {
            openRenderer(this);
            showPage(0);
            pageImage.setCurrPage(0);

            //closeRenderer();
        } catch (IOException exception) {
            Log.d(LOGNAME, "Error opening PDF");
        }

        fileName.setText(FILENAME);
        pageNumText[0] = "Page " + Integer.toString(currentPage.getIndex() + 1) + "/" + Integer.toString(pdfRenderer.getPageCount() + 1);
        pageNum.setText(pageNumText[0]);

        prevPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pagePrev();
                pageImage.setCurrPage(currentPage.getIndex());
                pageNumText[0] = "Page " + Integer.toString(currentPage.getIndex() + 1) + "/" + Integer.toString(pdfRenderer.getPageCount() + 1);
                pageNum.setText(pageNumText[0]);
            }
        });
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNext();
                pageImage.setCurrPage(currentPage.getIndex());
                pageNumText[0] = "Page " + Integer.toString(currentPage.getIndex() + 1) + "/" + Integer.toString(pdfRenderer.getPageCount() + 1);
                pageNum.setText(pageNumText[0]);
            }
        });
        penButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageImage.setBrushID(1);
            }
        });
        highlighterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageImage.setBrushID(2);
            }
        });
        eraserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageImage.setBrushID(3);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onStop() {
        super.onStop();
        try {
            closeRenderer();
        } catch (IOException ex) {
            Log.d(LOGNAME, "Unable to close PDF renderer");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openRenderer(Context context) throws IOException {
        // In this sample, we read a PDF from the assets directory.
        File file = new File(context.getCacheDir(), FILENAME);
        if (!file.exists()) {
            // pdfRenderer cannot handle the resource directly,
            // so extract it into the local cache directory.
            InputStream asset = this.getResources().openRawResource(FILERESID);
            FileOutputStream output = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            int size;
            while ((size = asset.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            asset.close();
            output.close();
        }
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);

        // capture PDF data
        // all this just to get a handle to the actual PDF representation
        if (parcelFileDescriptor != null) {
            pdfRenderer = new PdfRenderer(parcelFileDescriptor);
        }
    }

    // do this before you quit!
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void closeRenderer() throws IOException {
        if (null != currentPage) {
            currentPage.close();
        }
        pdfRenderer.close();
        parcelFileDescriptor.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showPage(int index) {
        if (pdfRenderer.getPageCount() <= index) {
            return;
        }
        // Close the current page before opening another one.
        if (null != currentPage) {
            currentPage.close();
        }
        // Use `openPage` to open a specific page in PDF.
        currentPage = pdfRenderer.openPage(index);
        // Important: the destination bitmap must be ARGB (not RGB).
        Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(), Bitmap.Config.ARGB_8888);

        // Here, we render the page onto the Bitmap.
        // To render a portion of the page, use the second and third parameter. Pass nulls to get the default result.
        // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        // Display the page
        pageImage.setImage(bitmap);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void pagePrev() {
        if (currentPage.getIndex() == 0) {
            return;
        }
        try {
            openRenderer(this);
            showPage(currentPage.getIndex() - 1);
            //closeRenderer();
        } catch (IOException exception) {
            Log.d(LOGNAME, "Error accessing previous page");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void pageNext() {
        if (currentPage.getIndex() == pdfRenderer.getPageCount()) {
            return;
        }
        try {
            openRenderer(this);
            showPage(currentPage.getIndex() + 1);
            //closeRenderer();
        } catch (IOException exception) {
            Log.d(LOGNAME, "Error accessing next page");
        }
    }
}
