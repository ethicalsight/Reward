package com.ethicalsight.reward.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.ethicalsight.reward.R;
import com.ethicalsight.reward.data.Account;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends CommonBaseActivity {

    private FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initUi();
    }

    private void initUi() {
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        SharedPreferences sharedPreferences = getSharedPreferences(Account.DOCUMENT, Context.MODE_PRIVATE);
        if (!sharedPreferences.contains(Account.ID)) {
            register();
        } else {
            String account_id = sharedPreferences.getString(Account.ID, null);
            login(account_id);
        }

    }


    private void startMainActivity(String account_id) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Thread.sleep(2500);
                } catch (Exception e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("account_id", account_id);
                startActivity(intent);
                finish();
            }
        }.execute();
    }


    private void register() {
        Map<String, Object> account = new HashMap<>();
        account.put(Account.ID, String.valueOf(new Random().nextInt(9999999) + 10000000));
        account.put(Account.CARDS, new ArrayList<>());
        account.put(Account.RECEIPTS, new ArrayList<>());
        firestore.collection(Account.COLLECTION).document()
                .set(account)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        SharedPreferences sharedPreferences = getSharedPreferences(Account.DOCUMENT, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(Account.ID, account.get(Account.ID).toString());
                        editor.commit();
                        login(account.get(Account.ID).toString());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        crashlytics.log("register: get failed with " + e.getMessage());
                    }
                });
    }


    private void login(String account_id) {
        firestore.collection(Account.COLLECTION).whereEqualTo(Account.ID, account_id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        startMainActivity(account_id);
                    }
                } else {
                    crashlytics.log("login: get failed with " + task.getException());
                }
            }
        });
    }
}
