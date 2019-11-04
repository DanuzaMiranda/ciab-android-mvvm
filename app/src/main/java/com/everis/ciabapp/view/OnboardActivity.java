package com.everis.ciabapp.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.everis.ciabapp.R;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import static com.everis.ciabapp.view.OCRActivity.EXTRA_OCR_TEXTO;

public class OnboardActivity extends AppCompatActivity {

    public static final String EXTRA_BUNDLE_OCR = "EXTRA_BUNDLE_OCR";
    private CarouselView carouselView;
    private String ocr;
    private Fragment cadastroFragment;


    int[] images = {R.drawable.everis_onboard1, R.drawable.everis_onboard2};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboard);
        ocr = (String) getIntent().getSerializableExtra(EXTRA_OCR_TEXTO);
        initVariables();
    }

    private void initVariables() {
        carouselView = findViewById(R.id.crv_onboard_carousel_inicio);
        carouselView.setPageCount(images.length);

        ImageListener imageListener = new ImageListener() {
            @Override
            public void setImageForPosition(int position, ImageView imageView) {
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setImageResource(images[position]);
            }
        };
        carouselView.setImageListener(imageListener);

        cadastroFragment = new CadastroFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fl_onboard_fragment, cadastroFragment);
        fragmentTransaction.commit();

    }

}
