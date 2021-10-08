package com.ethicalsight.reward.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ethicalsight.reward.R;
import com.ethicalsight.reward.activity.BarcodeActivity;
import com.ethicalsight.reward.activity.BrandActivity;
import com.ethicalsight.reward.adapter.CardAdapter;
import com.ethicalsight.reward.adapter.RecyclerViewClickListener;
import com.ethicalsight.reward.data.Account;
import com.ethicalsight.reward.data.Brand;
import com.ethicalsight.reward.data.Card;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CardFragment extends Fragment {

    private FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private Context context;
    private RecyclerView cardRecyclerView;
    private TextView emptyTextView;
    private CardAdapter cardAdapter;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigationView;
    private AdView adView;
    private FrameLayout adFrameLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card, container, false);
        context = view.getContext();
        cardRecyclerView = view.findViewById(R.id.card_recyclerView);
        cardRecyclerView.setHasFixedSize(true);
        cardRecyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        emptyTextView = view.findViewById(R.id.empty_TextView);
        progressBar = view.findViewById(R.id.progressBar);
        bottomNavigationView = getActivity().findViewById(R.id.nav_view);
        bottomNavigationView.setVisibility(View.VISIBLE);
        adFrameLayout = view.findViewById(R.id.ad_frameLayout);

        FloatingActionButton addCardFAB = (FloatingActionButton) view.findViewById(R.id.add_floatingActionButton);
        addCardFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), BrandActivity.class);
                intent.putExtra("account_id", getActivity().getIntent().getStringExtra("account_id"));
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadCards();
        adFrameLayout.post(new Runnable() {
            @Override
            public void run() {
                loadBannerAd();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void loadCards() {
        progressBar.setVisibility(View.VISIBLE);
        String accountId = getActivity().getIntent().getStringExtra("account_id");
        firestore.collection(Account.COLLECTION).whereEqualTo(Account.ID, accountId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot accountDocumentSnapshot = task.getResult().getDocuments().get(0);
                    List<Map<String, Object>> objects = (List<Map<String, Object>>) accountDocumentSnapshot.get("cards");
                    if (!objects.isEmpty()) {
                        List<Card> cards = new ArrayList<>();
                        RecyclerViewClickListener recyclerViewClickListener = (v, n) -> {
                            Intent intent = new Intent(context, BarcodeActivity.class);
                            intent.putExtra("account_id", getActivity().getIntent().getStringExtra("account_id"));
                            intent.putExtra("card", cards.stream().filter(c -> c.getId() == n).findFirst().get());
                            startActivity(intent);
                        };
                        cardAdapter = new CardAdapter(context, cards, recyclerViewClickListener);
                        cardRecyclerView.setAdapter(cardAdapter);
                        cardRecyclerView.setVisibility(View.VISIBLE);
                        emptyTextView.setVisibility(View.GONE);
                        for (Map<String, Object> entry : objects) {
                            progressBar.setVisibility(View.VISIBLE);
                            Card card = new Card();
                            card.setId(Long.parseLong(entry.get(Card.ID).toString()));
                            card.setNumber(entry.get(Card.NUMBER).toString());
                            DocumentReference brandDocumentReference = (DocumentReference) entry.get(Card.BRAND);
                            brandDocumentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        card.setBrand(new Brand());
                                        card.getBrand().setId(task.getResult().getLong(Brand.ID));
                                        card.getBrand().setName(task.getResult().getString(Brand.NAME));
                                        card.getBrand().setFormat(task.getResult().getString(Brand.FORMAT));
                                        card.getBrand().setBgColor(task.getResult().getString(Brand.BG_COLOR));
                                        storage.getReference(Brand.COLLECTION).child(card.getBrand().getId() + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                card.getBrand().setLogo(uri.toString());
                                                cards.add(card);
                                                cards.sort(Comparator.comparing(c -> c.getBrand().getName()));
                                                cardAdapter.notifyDataSetChanged();
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                crashlytics.log("loadCards: Error getting documents: " + exception);
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        emptyTextView.setVisibility(View.VISIBLE);
                    }
                } else {
                    crashlytics.log("loadCards: Error getting documents: " + task.getException());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                crashlytics.log("loadCards: Error getting documents: " + exception);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        cardRecyclerView.setVisibility(View.GONE);
        loadCards();
    }

    private void loadBannerAd() {
        // Create an ad request.
        adView = new AdView(getActivity());
        adView.setAdUnitId(getString(R.string.BANNER_AD_UNIT_ID));
        adView.removeAllViews();
        adFrameLayout.addView(adView);

        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);

        AdRequest adRequest = new AdRequest.Builder().build();

        // Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        // Determine the screen width (less decorations) to use for the ad width.
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = adView.getWidth();

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(getActivity(), adWidth);
    }
}
