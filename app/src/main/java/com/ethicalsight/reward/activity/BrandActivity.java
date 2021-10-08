package com.ethicalsight.reward.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ethicalsight.reward.R;
import com.ethicalsight.reward.adapter.BrandAdapter;
import com.ethicalsight.reward.adapter.RecyclerViewClickListener;
import com.ethicalsight.reward.data.Account;
import com.ethicalsight.reward.data.Brand;
import com.ethicalsight.reward.data.Card;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BrandActivity extends AppCompatActivity {

    private FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private BrandAdapter brandAdapter;
    private ProgressBar progressBar;
    private RecyclerView brandRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand);
        initUI();
        loadBrands();
    }

    private void initUI() {
        progressBar = findViewById(R.id.progressBar);
        brandRecyclerView = findViewById(R.id.brand_recyclerView);
        brandRecyclerView.setHasFixedSize(true);
        brandRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        EditText searchEditText = findViewById(R.id.search_editText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                brandAdapter.getFilter().filter(s);
                brandAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        ImageButton clearImageButton = findViewById(R.id.clear_imageButton);
        clearImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.getText().clear();
            }
        });
    }

    private void loadBrands() {
        progressBar.setVisibility(View.VISIBLE);
        firestore.collection(Brand.COLLECTION).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<Brand> brands = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Brand brand = new Brand();
                        brand.setId(document.getLong(Brand.ID));
                        brand.setName(document.getString(Brand.NAME));
                        brand.setFormat(document.getString(Brand.FORMAT));
                        brand.setBgColor(document.getString(Brand.BG_COLOR));
                        storage.getReference(Brand.COLLECTION).child(brand.getId() + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                brand.setLogo(uri.toString());
                                brands.add(brand);
                                brands.sort(Comparator.comparing(Brand::getName));
                                brandAdapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                crashlytics.log("loadBrands: Error getting documents: " + exception);
                            }
                        });
                    }
                    RecyclerViewClickListener recyclerViewClickListener = (v, n) -> {
                        Intent intent = new Intent(BrandActivity.this, BarcodeActivity.class);
                        intent.putExtra("account_id", getIntent().getStringExtra("account_id"));
                        intent.putExtra("brand", brands.stream().filter(b ->b.getId() == n).findFirst().get());
                        startActivity(intent);
                        finish();

                    };
                    brandAdapter = new BrandAdapter(BrandActivity.this, brands, recyclerViewClickListener);
                    brandRecyclerView.setAdapter(brandAdapter);
                    progressBar.setVisibility(View.GONE);
                } else {
                    crashlytics.log("loadBrands: Error getting documents: " + task.getException());
                }
            }
        });
    }
}