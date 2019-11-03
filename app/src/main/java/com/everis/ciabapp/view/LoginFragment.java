package com.everis.ciabapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.everis.ciabapp.R;
import com.everis.ciabapp.model.EmailVO;
import com.everis.ciabapp.viewmodel.API;
import com.everis.ciabapp.viewmodel.APIUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    public static final String EXTRA_ID_USUARIO = "EXTRA_ID_USUARIO";
    private View view;
    private EditText etLoginEmail;
    private API mAPI;
    private int id;
    private EmailVO emailVO;
    private Button btBack;
    private Fragment cadastroFragment;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);
        initVariable();
        backLogin();
        return view;
    }

    private void initVariable() {
        mAPI = APIUtils.getAPIService();
        etLoginEmail = view.findViewById(R.id.et_login_email);

        Button btLoginLogar = view.findViewById(R.id.bt_login_logar);
        btLoginLogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etLoginEmail.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "You must fill in an e-mail.", Toast.LENGTH_SHORT).show();
                } else {
                    String email = etLoginEmail.getText().toString();

                    emailVO = new EmailVO();
                    emailVO.setEmail(email);
                    logarUsuario(email);
                }
            }
        });
    }

    private void backLogin() {

        btBack = view.findViewById(R.id.bt_cadastro_signUp);
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cadastroFragment = new CadastroFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fl_onboard_fragment, cadastroFragment);
                fragmentTransaction.commit();

            }
        });

    }


    public void logarUsuario(String email) {
        mAPI.getEmailUser(email).enqueue(new Callback<EmailVO>() {
            @Override
            public void onResponse(@NonNull Call<EmailVO> call, @NonNull Response<EmailVO> response) {
                switch (response.code()) {
                    case 200:
                        emailVO = response.body();
                        if (emailVO != null) {
                            id = emailVO.getId();
                        }
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                Intent intent = new Intent(getActivity(), HUBActivity.class);
                                intent.putExtra(EXTRA_ID_USUARIO, id);
                                startActivity(intent);
                            }
                        }, 1000);
                        break;
                    case 404:
                        Toast.makeText(getActivity(), "There is no user with this e-mail.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmailVO> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), "Fail to get API", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
