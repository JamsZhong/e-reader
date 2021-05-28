package net.codebot.pdfviewer;

import android.graphics.Path;

// Path wrapper so that lines are rendered on the correct page with the correct tool
public class MyPath extends Path {
    int page;
    int id;

    void setId (int id) { this.id = id; }
    void setPage (int page) { this.page = page; }
}
