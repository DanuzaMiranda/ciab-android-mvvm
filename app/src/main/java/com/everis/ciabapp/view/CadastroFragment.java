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
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.everis.ciabapp.R;
import com.everis.ciabapp.model.CadastroVO;
import com.everis.ciabapp.viewmodel.API;
import com.everis.ciabapp.viewmodel.APIUtils;
import com.santalu.maskedittext.MaskEditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.support.constraint.Constraints.TAG;

public class CadastroFragment extends Fragment {

    private EditText etCadastroNome;
    private EditText etCadastroEmpresa;
    private EditText etCadastroPhone;
    private EditText etCadastroEmail;
    private CircleImageView btTirarFoto;
    private Button btCadastrar;
    private Button btLogin;
    private ProgressBar progressBar;
    private View view;
    private boolean fotoCapturada = false;
    private ConstraintLayout infoFoto;
    private Bitmap finalBitmap;

    public static final String EXTRA_ID_USUARIO = "EXTRA_ID_USUARIO";
    private CadastroVO cadastro;
    private API mAPI;
    private int id;
    private int erroFoto;

    private String ocr;

    private MultipartBody.Part body;
    private MaskEditText editText;

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri fotoURI;
    private String mCurrentPhotoPath;
    private String nomeFoto = "";

    public static final String MY_PREFS_NAME = "MyPrefsFile";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("TESTELEO", "onCreate");
        view = inflater.inflate(R.layout.fragment_cadastro, container, false);
        initVariable();
        return view;

    }

    private void initVariable() {
        mAPI = APIUtils.getAPIService();

        progressBar = view.findViewById(R.id.pb_cadastro_progressbar);
        etCadastroNome = view.findViewById(R.id.et_cadastro_nome);
        etCadastroEmpresa = view.findViewById(R.id.et_cadastro_empresa);
        etCadastroEmail = view.findViewById(R.id.et_cadastro_email);
        etCadastroPhone = view.findViewById(R.id.et_cadastro_phone);
        infoFoto = view.findViewById(R.id.cl_cadastro_info_botao);
        editText = view.findViewById(R.id.et_cadastro_phone);



        btTirarFoto = view.findViewById(R.id.civ_cadastro_foto);
        btTirarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tirarFotoIntent();
            }
        });

        btCadastrar = view.findViewById(R.id.bt_cadastro_register); //
        btCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (etCadastroNome.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "You must fill in a name.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (etCadastroEmpresa.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "You must fill in a company.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (editText.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "You must fill in a phone.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (etCadastroEmail.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "You must fill in an e-mail.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!fotoCapturada) {
                    Toast.makeText(getActivity(), "Need to take a photo.", Toast.LENGTH_SHORT).show();
                    return;
                }
                cadastrar();
            }
        });

        btLogin = view.findViewById(R.id.bt_cadastro_signUp);
        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initLoginFragment();
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        retreiveData();
    }

    private void retreiveData(){
        SharedPreferences prefs = getActivity().getSharedPreferences(MY_PREFS_NAME, getContext().MODE_PRIVATE);

        if(prefs.contains("nome") && prefs.contains("empresa")){
            String nome = prefs.getString("nome", "No name defined");
            String empresa = prefs.getString("empresa", "Everis");
            Log.i("TESTELEO", nome+ " - "+empresa);

            etCadastroNome.setText(nome);
            etCadastroEmpresa.setText(empresa);
            clearShared();
        }


    }

    private void clearShared(){
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(MY_PREFS_NAME, getActivity().MODE_PRIVATE).edit();
        editor.remove("nome");
        editor.remove("empresa");
        editor.apply();
        editor.commit();
    }

    private void tirarFotoIntent() {

        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            Intent intentFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (intentFoto.resolveActivity(Objects.requireNonNull(getActivity()).getPackageManager()) != null) {

                File fotoFile = null;
                try {
                    fotoFile = criarImagemArquivo();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (fotoFile != null) {
                    fotoURI = FileProvider.getUriForFile(view.getContext(),
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
        nomeFoto = imageNameFile + ".jpg";

        File storageDir = Objects.requireNonNull(getActivity()).getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageNameFile,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }

    private void cadastrar() {
        progressCadastro(true);
        String nome = etCadastroNome.getText().toString();
        String empresa = etCadastroEmpresa.getText().toString();
        String phone = etCadastroPhone.getText().toString();
        String email = etCadastroEmail.getText().toString();
        int saldoInicial = 100;

        cadastro = new CadastroVO();
        cadastro.setNome(nome);
        cadastro.setEmpresa(empresa);
        cadastro.setPhone(phone);
        cadastro.setEmail(email);
        cadastro.setSaldo(saldoInicial);

        if (erroFoto != 500) {
            mandarCadastro(cadastro);
        } else
            mandarFoto(body);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == -1) {
            Toast.makeText(getActivity(), "Successfully captured photo.", Toast.LENGTH_SHORT).show();
            try {
                transformToImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
            fotoCapturada = true;
        } else {
            fotoCapturada = false;
        }
    }

    public void mandarCadastro(CadastroVO cadastroVO) {
        mAPI.saveUser(cadastroVO).enqueue(new Callback<CadastroVO>() {
            @Override
            public void onResponse(@NonNull Call<CadastroVO> call, @NonNull Response<CadastroVO> response) {
                switch (response.code()) {
                    case 201:
                        cadastro = response.body();
                        if (cadastro != null) {
                            id = cadastro.getId();
                        }
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                mandarFoto(body);
                            }
                        }, 1000);
                        break;
                    case 400:
                        Toast.makeText(getActivity(), "E-mail is not in valid format", Toast.LENGTH_SHORT).show();
                        progressCadastro(false);
                        break;
                    case 409:
                        Toast.makeText(getActivity(), "This email is already registered.", Toast.LENGTH_SHORT).show();
                        progressCadastro(false);
                        break;
                    case 500:
                        Toast.makeText(getActivity(), "Offline database.", Toast.LENGTH_SHORT).show();
                        progressCadastro(false);
                        break;
                }
            }

            @Override
            public void onFailure(@NonNull Call<CadastroVO> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), "Fail to get API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void mandarFoto(MultipartBody.Part body) {
        mAPI.saveImage(id, body).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                switch (response.code()) {
                    case 201:
                        Toast.makeText(getActivity(), "Registration done.", Toast.LENGTH_SHORT).show();
                        progressCadastro(false);
                        Intent intent = new Intent(getActivity(), HUBActivity.class);
                        intent.putExtra(EXTRA_ID_USUARIO, id);
                        startActivity(intent);
                        break;
                    case 400:
                        Toast.makeText(getActivity(), "Erro no Upload.", Toast.LENGTH_SHORT).show();
                        progressCadastro(false);
                        break;
                    case 404:
                        Toast.makeText(getActivity(), "Invalid id.", Toast.LENGTH_SHORT).show();
                        progressCadastro(false);
                        break;
                    case 409:
                        Toast.makeText(getActivity(), "This photo has already been registered.", Toast.LENGTH_SHORT).show();
                        progressCadastro(false);
                        break;
                    case 500:
                        Toast.makeText(getActivity(), "Invalid photo, please take a new one.", Toast.LENGTH_SHORT).show();
                        erroFoto = 500;
                        progressCadastro(false);
                        break;
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "Unable to submit post to API.");
            }
        });
    }

    private void initLoginFragment() {
        Fragment fragment2 = new LoginFragment();
        assert getFragmentManager() != null;
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fl_onboard_fragment, fragment2);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private void transformToImage() throws IOException {
        File f = new File(Objects.requireNonNull(getActivity()).getCacheDir(), "profile.jpg");

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

        finalBitmap = rotatedBitmap;
        btTirarFoto.setImageBitmap(finalBitmap);
        infoFoto.setVisibility(View.GONE);
    }

    private void progressCadastro(Boolean iniciado) {
        if (iniciado) {
            progressBar.setVisibility(View.VISIBLE);
            btCadastrar.setClickable(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btCadastrar.setClickable(true);
        }
    }
}
