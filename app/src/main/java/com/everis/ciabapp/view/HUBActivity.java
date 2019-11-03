package com.everis.ciabapp.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.everis.ciab.UnityPlayerActivity;
import com.everis.ciabapp.R;
import com.everis.ciabapp.model.CadastroVO;
import com.everis.ciabapp.viewmodel.API;
import com.everis.ciabapp.viewmodel.APIUtils;
import com.everis.ciabapp.viewmodel.CustomTypefaceSpan;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.support.constraint.Constraints.TAG;
import static com.everis.ciabapp.view.CadastroFragment.EXTRA_ID_USUARIO;

public class HUBActivity extends AppCompatActivity {

    private Handler handler = new Handler();
    private TextView saldo;
    private int id;
    private CadastroVO cadastroVO;
    private API mAPI;
    public static final String EXTRA_CREDITOR_ID = "EXTRA_CREDITOR_ID";
    Runnable updateCurrency = new Runnable() {
        public void run() {
            getUser(id);
            handler.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub);
        initVars();
        getUser(id);
        handler.post(updateCurrency);
    }

    private void initValues() {
        saldo.setText(String.valueOf(cadastroVO.getSaldo()));
    }

    @Override
    public void onBackPressed() {
        logoutConfirmation();
    }

    private void initVars() {
        mAPI = APIUtils.getAPIService();
        id = (int) getIntent().getSerializableExtra(EXTRA_ID_USUARIO);

        TextView evercoin = findViewById(R.id.tv_hub_total_evercoins);
        evercoin.setLetterSpacing((float) 0.2);
        saldo = findViewById(R.id.tv_saldo_evercoins);
        saldo.setLetterSpacing((float) 0.1);

        Button btCoffee = findViewById(R.id.bt_hub_coffee);
        Button btCard = findViewById(R.id.bt_hub_card);
        Button btPig = findViewById(R.id.bt_hud_pig);
        Button btPub = findViewById(R.id.bt_hud_pub);
        Button btTransfer = findViewById(R.id.bt_hud_transfer);
        Button btGift = findViewById(R.id.bt_hub_gift);
        Button btLogout = findViewById(R.id.bt_hud_logout);

        btGift.setText(misturarFontes("Take your", "gift", true));
        btCoffee.setText(misturarFontes("Rethinking", "the cities - Checkpoint", false));
        btCard.setText(misturarFontes("Shifting", "the world", false));
        btPig.setText(misturarFontes("Shifting", "the world - Checkpoint", false));
        btPub.setText(misturarFontes("Connecting", "people - Checkpoint", false));
        btTransfer.setText(misturarFontes("Connecting", "people", false));

        btCoffee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HUBActivity.this, UnityPlayerActivity.class);
                intent.putExtra("sceneName", "CoffeeScene");
                intent.putExtra("playerID", id);
                startActivity(intent);
            }
        });

        btCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HUBActivity.this, UnityPlayerActivity.class);
                intent.putExtra("sceneName", "CardScene");
                intent.putExtra("playerID", id);
                startActivity(intent);
            }
        });

        btPig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HUBActivity.this, UnityPlayerActivity.class);
                intent.putExtra("sceneName", "PigScene");
                intent.putExtra("playerID", id);
                startActivity(intent);
            }
        });

        btPub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HUBActivity.this, UnityPlayerActivity.class);
                intent.putExtra("sceneName", "PubScene");
                intent.putExtra("playerID", id);
                startActivity(intent);
            }
        });

        btTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HUBActivity.this, TransferActivity.class);
                intent.putExtra(EXTRA_CREDITOR_ID, id);
                startActivity(intent);
            }
        });

        btGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishJourney();
            }
        });

        btLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutConfirmation();
            }
        });
    }

    private Spannable misturarFontes(String palavra1, String palavra2, boolean inverter) {
        Spannable spannable = new SpannableString(palavra1 + " " + palavra2);
        if (inverter) {
            spannable.setSpan(
                    new CustomTypefaceSpan("helvetica_neue_thin.otf", Typeface.DEFAULT),
                    0,
                    palavra1.length() + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            spannable.setSpan(
                    new CustomTypefaceSpan("helvetica_neue_bold.ttf", Typeface.DEFAULT_BOLD),
                    palavra1.length() + 1,
                    palavra1.length() + 1 + palavra2.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        } else
            spannable.setSpan(
                    new CustomTypefaceSpan("helvetica_neue_bold.ttf", Typeface.DEFAULT_BOLD),
                    0,
                    palavra1.length() + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        spannable.setSpan(
                new CustomTypefaceSpan("helvetica_neue_thin.otf", Typeface.DEFAULT),
                palavra1.length() + 1,
                palavra1.length() + 1 + palavra2.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        return spannable;
    }

    public void getUser(int id) {
        mAPI.getUser(id).enqueue(new Callback<CadastroVO>() {
            @Override
            public void onResponse(@NonNull Call<CadastroVO> call, @NonNull Response<CadastroVO> response) {
                cadastroVO = response.body();
                if (cadastroVO != null) {
                    initValues();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CadastroVO> call, @NonNull Throwable t) {
                Log.e(TAG, "Unable to get user from API");
            }

        });
    }

    public void endJourney(int id, CadastroVO cadastro) {
        mAPI.endJourney(id, cadastro).enqueue(new Callback<CadastroVO>() {
            @Override
            public void onResponse(@NonNull Call<CadastroVO> call, @NonNull Response<CadastroVO> response) {
                switch (response.code()) {
                    case 200:
                        cadastroVO = response.body();
                        if (cadastroVO != null) {
                            Intent intent = new Intent(HUBActivity.this, EndActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        break;
                    case 400:
                        Toast.makeText(HUBActivity.this, "BAD REQUEST.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onFailure(@NonNull Call<CadastroVO> call, @NonNull Throwable t) {
                Log.e(TAG, "Unable to get user from API");
            }
        });
    }

    private void logoutConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HUBActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Confirmar Saída");
        builder.setMessage("Você realmente quer encerrar sua jornada?");
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(HUBActivity.this, OnboardActivity.class);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void finishJourney() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HUBActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Finalizar Jornada");
        builder.setMessage("Deseja retirar seu brinde e encerrar a jornada?");
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cadastroVO.setSaldo(0);
                endJourney(id, cadastroVO);
            }
        });
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
