package com.everis.ciabapp.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.everis.ciabapp.R;
import com.everis.ciabapp.model.CadastroVO;
import com.everis.ciabapp.viewmodel.API;
import com.everis.ciabapp.viewmodel.APIUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.support.constraint.Constraints.TAG;
import static com.everis.ciabapp.view.CadastroFragment.EXTRA_ID_USUARIO;
import static com.everis.ciabapp.view.CadastroFragment.REQUEST_IMAGE_CAPTURE;
import static com.everis.ciabapp.view.HUBActivity.EXTRA_CREDITOR_ID;

public class TransferActivity extends AppCompatActivity {

    private String mCurrentPhotoPath;
    private ProgressBar progressBar;

    private API mAPI;
    private int creditorId;
    private int debtorId;
    private String debtorName;
    private CadastroVO cadastro;
    private MultipartBody.Part body;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transference);
        mAPI = APIUtils.getAPIService();
        creditorId = (int) getIntent().getSerializableExtra(EXTRA_CREDITOR_ID);
        progressBar = findViewById(R.id.pb_tranference_progressbar);
        tirarFotoIntent();
    }

    private void tirarFotoIntent() {

        if (ContextCompat.checkSelfPermission(TransferActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(TransferActivity.this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            Intent intentFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (intentFoto.resolveActivity(TransferActivity.this.getPackageManager()) != null) {

                File fotoFile = null;
                try {
                    fotoFile = criarImagemArquivo();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (fotoFile != null) {
                    Uri fotoURI = FileProvider.getUriForFile(TransferActivity.this,
                            "com.everis.ciab.fileprovider",
                            fotoFile
                    );
                    intentFoto.putExtra(MediaStore.EXTRA_OUTPUT, fotoURI);
                    startActivityForResult(intentFoto, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }

    private File criarImagemArquivo() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageNameFile = "JPG_" + timeStamp + "_";

        File storageDir = TransferActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

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
            progressRecognition(true);
            try {
                transformToImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
            recog(body);
        } else {
            Intent intent = new Intent(TransferActivity.this, HUBActivity.class);
            intent.putExtra(EXTRA_ID_USUARIO, creditorId);
            startActivity(intent);
        }
    }

    public void recog(MultipartBody.Part body) {
        mAPI.imageRecog(body).enqueue(new Callback<CadastroVO>() {

            @Override
            public void onResponse(@NonNull Call<CadastroVO> call, @NonNull Response<CadastroVO> response) {
                switch (response.code()) {
                    case 200:
                        Toast.makeText(TransferActivity.this, "User found.", Toast.LENGTH_SHORT).show();
                        cadastro = response.body();
                        if (cadastro != null) {
                            debtorId = cadastro.getId();
                            debtorName = cadastro.getNome();
                            tranferConfirmation();
                        }
                        break;
                    case 400:
                        Toast.makeText(TransferActivity.this, "Failed to send image.", Toast.LENGTH_SHORT).show();
                        goBack();
                        break;
                    case 404:
                        Toast.makeText(TransferActivity.this, "No user found.", Toast.LENGTH_SHORT).show();
                        goBack();
                        break;
                    case 409:
                        Toast.makeText(TransferActivity.this, "Insufficient funds.", Toast.LENGTH_SHORT).show();
                        goBack();
                    case 500:
                        Toast.makeText(TransferActivity.this, "Invalid face, make recognition again.", Toast.LENGTH_SHORT).show();
                        goBack();
                        break;
                }
            }

            @Override
            public void onFailure(@NonNull Call<CadastroVO> call, @NonNull Throwable t) {
                Log.e(TAG, "Unable to submit post to API.");
            }
        });
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private void tranferConfirmation() {
        progressRecognition(false);
        AlertDialog.Builder builder = new AlertDialog.Builder(TransferActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Confirm Transfer");
        builder.setMessage("Do you want to transfer to " + debtorName + "?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                transfer();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                goBack();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void transfer() {
        mAPI.makeTransfer(creditorId, debtorId).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                switch (response.code()) {
                    case 200:
                        Intent intent = new Intent(TransferActivity.this, EndActivity.class);
                        startActivity(intent);
                        break;
                    case 404:
                        Toast.makeText(TransferActivity.this, "User found.", Toast.LENGTH_SHORT).show();
                        Intent goBack = new Intent(TransferActivity.this, HUBActivity.class);
                        startActivity(goBack);
                        finish();
                        break;
                    case 409:
                        Toast.makeText(TransferActivity.this, "Insufficient funds.", Toast.LENGTH_SHORT).show();
                        Intent goBack2 = new Intent(TransferActivity.this, HUBActivity.class);
                        startActivity(goBack2);
                        finish();
                        break;
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(TransferActivity.this, "fail to get API.", Toast.LENGTH_SHORT).show();
            }
        });
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

        ByteArrayOutputStream bous = new ByteArrayOutputStream();
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 10, bous);
        byte[] bitmapdata = bous.toByteArray();
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        body = MultipartBody.Part.createFormData("file", f.getName(), requestBody);
    }

    private void progressRecognition(Boolean iniciado) {
        if (iniciado) {
            progressBar.setVisibility(View.VISIBLE);
        } else
            progressBar.setVisibility(View.GONE);
    }

    private void goBack() {
        Intent intent = new Intent(TransferActivity.this, HUBActivity.class);
        intent.putExtra(EXTRA_ID_USUARIO, creditorId);
        startActivity(intent);
    }
}