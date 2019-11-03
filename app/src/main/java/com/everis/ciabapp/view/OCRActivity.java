package com.everis.ciabapp.view;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.everis.ciabapp.R;
import com.everis.ciabapp.viewmodel.TextRecog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.everis.ciabapp.view.CadastroFragment.REQUEST_IMAGE_CAPTURE;


public class OCRActivity extends AppCompatActivity {

//    private CropImageView cropImageView;

    public static final String EXTRA_OCR_TEXTO = "EXTRA_OCR_TEXTO";

    private String mCurrentPhotoPath;
    private String ocrText;
    private String[] ocrSplit;
    private String secondLine = "";

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);
        tirarFotoIntent();

    }

    private void tirarFotoIntent() {

        if (ContextCompat.checkSelfPermission(OCRActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(OCRActivity.this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            Intent intentFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (intentFoto.resolveActivity(OCRActivity.this.getPackageManager()) != null) {

                File fotoFile = null;
                try {
                    fotoFile = criarImagemArquivo();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (fotoFile != null) {
                    Uri uri = FileProvider.getUriForFile(OCRActivity.this,
                            "com.everis.ciab.fileprovider",
                            fotoFile
                    );
                    intentFoto.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intentFoto, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }

    private File criarImagemArquivo() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageNameFile = "JPG_" + timeStamp + "_";

        File storageDir = OCRActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageNameFile,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == -1) {
            try {
                transformToImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void transformToImage() throws IOException {
        File f = new File(this.getCacheDir(), "profile.jpg");

        ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
        Bitmap rotatedBitmap;
        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        ocrText = new TextRecog(rotatedBitmap).invoke(this);
        ocrSplit = ocrText.split("\n");
        saveData(ocrSplit);
    }

    public static final String MY_PREFS_NAME = "MyPrefsFile";

    private void saveData(String[] data){
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("nome", data[0]);
        editor.putString("empresa", data[1]);
        editor.apply();
        editor.commit();

        this.finish();
    }
}
