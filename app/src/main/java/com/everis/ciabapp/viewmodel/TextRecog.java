package com.everis.ciabapp.viewmodel;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

public class TextRecog {

    private Bitmap bitmap;
    private Context context;

    public TextRecog(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String invoke(Context context) {
        String textoReconhecido = "";
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context.getApplicationContext()).build();
        if (!textRecognizer.isOperational())
            Log.e("ERROR", "Detector dependencies are not yet available");
        else {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = textRecognizer.detect(frame);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < items.size(); ++i) {
                TextBlock item = items.valueAt(i);
                stringBuilder.append(item.getValue());
                stringBuilder.append("\n");
            }
            textoReconhecido = stringBuilder.toString();
        }

        return textoReconhecido;
    }
}
