package com.ethicalsight.reward.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ethicalsight.reward.R;
import com.ethicalsight.reward.data.Account;
import com.ethicalsight.reward.data.Brand;
import com.ethicalsight.reward.data.Card;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BarcodeActivity extends AppCompatActivity {

    private FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();


    private ProgressBar progressBar;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bardcode);
        progressBar = findViewById(R.id.progressBar);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.barcode_menu, menu);
        this.menu = menu;
        initUI();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.back:
                finish();
                return true;
            case R.id.save:
                saveCard();
            case R.id.edit:
                editCard();
                return true;
            case R.id.delete:
                showDeleteDialog(getIntent().getParcelableExtra("card"));
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, getString(R.string.MESSAGE_CARD_CANCEL), Toast.LENGTH_LONG);
            } else {
                Toast.makeText(this, getString(R.string.MESSAGE_CARD_SCAN), Toast.LENGTH_LONG);
                ((EditText) findViewById(R.id.number_editText)).setText(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initUI() {

        Brand brand = getIntent().hasExtra("brand") ? getIntent().getParcelableExtra("brand") : ((Card) getIntent().getParcelableExtra("card")).getBrand();

        ConstraintLayout containerConstraintLayout = findViewById(R.id.container_constraintLayout);
        containerConstraintLayout.setBackgroundColor(Color.parseColor(Optional.ofNullable(brand.getBgColor()).orElse("#FFFFFF")));

        ImageView logoImageView = findViewById(R.id.logo_imageView);
        Picasso.get().load(brand.getLogo()).into(logoImageView);

        EditText numberEditText = findViewById(R.id.number_editText);
        numberEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (numberEditText.getCompoundDrawables()[2] != null && event.getX() >= (numberEditText.getRight() - numberEditText.getLeft() - numberEditText.getCompoundDrawables()[2].getBounds().width())) {
                        IntentIntegrator intentIntegrator = new IntentIntegrator(BarcodeActivity.this);
                        intentIntegrator.setOrientationLocked(true);
                        intentIntegrator.initiateScan();
                    }
                }
                return false;
            }
        });

        EditText brandEditText = findViewById(R.id.brand_editText);
        if (brand.getId() == 0) {
            brandEditText.setVisibility(View.VISIBLE);
            brandEditText.setText(brand.getName());
        }

        if (getIntent().hasExtra("card")) {
            Card card = getIntent().getParcelableExtra("card");

            ImageView barcodeImageView = findViewById(R.id.barcode_imageView);
            barcodeImageView.setImageBitmap(generateBarcode(card.getBrand().getFormat(), card.getNumber()));
            barcodeImageView.setVisibility(View.VISIBLE);

            TextView numberTextView = findViewById(R.id.number_textView);
            numberTextView.setText(card.getNumber());
            numberTextView.setVisibility(View.VISIBLE);

            numberEditText.setText(card.getNumber());

            findViewById(R.id.scan_imageView).setVisibility(View.GONE);
            findViewById(R.id.number_editText).setVisibility(View.GONE);
            findViewById(R.id.brand_editText).setVisibility(View.GONE);
            menu.findItem(R.id.delete).setVisible(true);
            menu.findItem(R.id.edit).setVisible(true);
            menu.findItem(R.id.save).setVisible(false);
        } else {
            findViewById(R.id.scan_imageView).setVisibility(View.VISIBLE);
            numberEditText.setVisibility(View.VISIBLE);
            findViewById(R.id.barcode_imageView).setVisibility(View.GONE);
            findViewById(R.id.number_textView).setVisibility(View.GONE);
            menu.findItem(R.id.delete).setVisible(false);
            menu.findItem(R.id.edit).setVisible(false);
            menu.findItem(R.id.save).setVisible(true);
        }
    }

    public void saveCard() {
        progressBar.setVisibility(View.VISIBLE);
        String accountId = getIntent().getStringExtra("account_id");
        Brand brand = getIntent().hasExtra("brand") ? getIntent().getParcelableExtra("brand") : ((Card) getIntent().getParcelableExtra("card")).getBrand();

        EditText numberEditText = findViewById(R.id.number_editText);
        if (numberEditText.getText().length() == 0) {
            numberEditText.setHintTextColor(getResources().getColor(R.color.red_500));
            return;
        }

        if (brand.getId() == 0) {
            EditText brandEditText = findViewById(R.id.brand_editText);
            if (brandEditText.getText().length() == 0) {
                brandEditText.setHintTextColor(getResources().getColor(R.color.red_500));
                return;
            }
            Map<String, Object> request = new HashMap<>();
            request.put(Account.ID, accountId);
            request.put(Card.NUMBER, numberEditText.getText().toString());
            request.put(Brand.NAME, brandEditText.getText().toString());
            firestore.collection("REQUESTS").add(request);
        }

        firestore.collection(Brand.COLLECTION).whereEqualTo(Brand.ID, brand.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot brandDocumentSnapshot = task.getResult().getDocuments().get(0);

                    firestore.collection(Account.COLLECTION).whereEqualTo(Account.ID, accountId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot accountDocumentSnapshot = task.getResult().getDocuments().get(0);
                                if (((List) accountDocumentSnapshot.get(Account.CARDS)).size() >= 50) {
                                    Toast.makeText(BarcodeActivity.this, getString(R.string.ERROR_MAX_CARD), Toast.LENGTH_LONG);
                                    return;
                                }


                                Map<String, Object> cardMap = new HashMap<>();
                                cardMap.put(Card.ID, ((List) accountDocumentSnapshot.get("cards")).size() + 1);
                                cardMap.put(Card.BRAND, brandDocumentSnapshot.getReference());
                                cardMap.put(Card.NUMBER, numberEditText.getText().toString());

                                Map<String, Object> oldCard = new HashMap<>();
                                if (getIntent().hasExtra("card")) {
                                    Card card = getIntent().getParcelableExtra("card");
                                    cardMap.put(Card.ID, card.getId());
                                    oldCard.put(Card.ID, card.getId());
                                    oldCard.put(Card.BRAND, brandDocumentSnapshot.getReference());
                                    oldCard.put(Card.NUMBER, card.getNumber());
                                }

                                accountDocumentSnapshot.getReference().update("cards", FieldValue.arrayRemove(oldCard), "cards", FieldValue.arrayUnion(cardMap)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(BarcodeActivity.this, "Card saved.", Toast.LENGTH_LONG);
                                        Card card = new Card();
                                        card.setId(Long.parseLong(cardMap.get(Card.ID).toString()));
                                        card.setBrand(brand);
                                        card.setNumber(numberEditText.getText().toString());
                                        getIntent().putExtra("card", card);
                                        initUI();
                                        Toast.makeText(BarcodeActivity.this, getString(R.string.MESSAGE_CARD_SAVE), Toast.LENGTH_LONG);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        progressBar.setVisibility(View.GONE);
                                        crashlytics.log("saveCard: Error updating cards: " + exception);
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressBar.setVisibility(View.GONE);
                            crashlytics.log("saveCard: Error getting documents: " + exception);
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                progressBar.setVisibility(View.GONE);
                crashlytics.log("saveCard: Error getting documents: " + exception);
            }
        });
    }


    public void editCard() {
        findViewById(R.id.scan_imageView).setVisibility(View.VISIBLE);
        findViewById(R.id.number_editText).setVisibility(View.VISIBLE);
        findViewById(R.id.barcode_imageView).setVisibility(View.GONE);
        findViewById(R.id.number_textView).setVisibility(View.GONE);
        menu.findItem(R.id.delete).setVisible(true);
        menu.findItem(R.id.edit).setVisible(false);
        menu.findItem(R.id.save).setVisible(true);
    }

    private void showDeleteDialog(Card card) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        alertDialogBuilder
                .setMessage(getString(R.string.card_delete))
                .setCancelable(false)
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        deleteCard();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    public void deleteCard() {
        Card card = getIntent().getParcelableExtra("card");
        progressBar.setVisibility(View.VISIBLE);
        String accountId = getIntent().getStringExtra("account_id");
        firestore.collection(Account.COLLECTION).whereEqualTo(Account.ID, accountId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot accountDocumentSnapshot = task.getResult().getDocuments().get(0);
                    List<Map<String, Object>> objects = (List<Map<String, Object>>) accountDocumentSnapshot.get("cards");
                    Map<String, Object> oldCard = (Map) objects.stream().filter(o -> Long.parseLong(o.get(Card.ID).toString()) == card.getId()).findFirst().get();
                    accountDocumentSnapshot.getReference().update("cards", FieldValue.arrayRemove(oldCard)).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(BarcodeActivity.this, getString(R.string.MESSAGE_CARD_SAVE), Toast.LENGTH_LONG);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressBar.setVisibility(View.GONE);
                            crashlytics.log("saveCard: Error updating cards: " + exception);
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                progressBar.setVisibility(View.GONE);
                crashlytics.log("saveCard: Error getting documents: " + exception);
            }
        });
    }


    private Bitmap generateBarcode(String format, String number) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.encodeBitmap(number, BarcodeFormat.valueOf(format), 800, 300);
        } catch (Exception e) {
            e.getMessage();
        }
        return null;
    }
}