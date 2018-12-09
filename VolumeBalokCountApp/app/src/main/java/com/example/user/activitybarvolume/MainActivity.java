package com.example.user.activitybarvolume;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.et_panjang)EditText etPanjang;
    @BindView(R.id.et_lebar)EditText etLebar;
    @BindView(R.id.et_tinggi)EditText etTinggi;
    @BindView(R.id.model_image)ImageView modelImage;

    @BindView(R.id.btn_hitung)Button btnHitung;
    @BindView(R.id.txtView_hasil)TextView txtViewHasil;
    private static final String STATE_HASIL = "state_hasil";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (savedInstanceState !=null){
            String hasil = savedInstanceState.getString(STATE_HASIL);
            txtViewHasil.setText(hasil);
        }

        Picasso
                .get()
                .load("https://2.bp.blogspot.com/-0ymNWxwoAc8/Vu1j5hzW4qI/AAAAAAAAHUc/NsvXhBE9Wy0JnlLmHCvXKq9Kpv5P8pMNQ/s1600/math-luas-balok-contoh-02a.jpg")
                .into(modelImage);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        outState.putString(STATE_HASIL, txtViewHasil.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @OnClick(R.id.btn_hitung)
    public void onClick(View view){
        if (view.getId() == R.id.btn_hitung){
            String panjang = etPanjang.getText().toString().trim();
            String lebar = etLebar.getText().toString().trim();
            String tinggi = etTinggi.getText().toString().trim();
            boolean isEmptyFields = false;
            if (TextUtils.isEmpty(panjang)){
                isEmptyFields = true;
                etPanjang.setError("Field ini tidak boleh kosong");
            }
            if (TextUtils.isEmpty(lebar)){
                isEmptyFields = true;
                etLebar.setError("Field ini tidak boleh kosong");
            }
            if (TextUtils.isEmpty(tinggi)){
                isEmptyFields = true;
                etTinggi.setError("Field ini tidak boleh kosong");
            }
            if (!isEmptyFields){
                double p = Double.parseDouble(panjang);
                double l = Double.parseDouble(lebar);
                double t = Double.parseDouble(tinggi);
                double volume = p * l * t;
                txtViewHasil.setText(String.valueOf(volume));
            }
        }
    }

}
