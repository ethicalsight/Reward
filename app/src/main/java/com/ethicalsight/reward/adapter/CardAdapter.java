package com.ethicalsight.reward.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.ethicalsight.reward.data.Card;
import com.ethicalsight.reward.R;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Optional;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private Context context;
    private List<Card> cards;
    private RecyclerViewClickListener recyclerViewClickListener;

    // Provide a suitable constructor (depends on the kind of dataset)
    public CardAdapter(Context context, List<Card> cards, RecyclerViewClickListener recyclerViewClickListener) {
        this.context = context;
        this.cards = cards;
        this.recyclerViewClickListener = recyclerViewClickListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CardAdapter.CardViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_item, parent, false);
        CardViewHolder vh = new CardViewHolder(view, recyclerViewClickListener);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Card card = cards.get(position);
        holder.idTextView.setText(String.valueOf(card.getId()));
        holder.logoCardView.setCardBackgroundColor(Color.parseColor(Optional.ofNullable(card.getBrand().getBgColor()).orElse("#FFFFFF")));
        Picasso.get().load(card.getBrand().getLogo()).fit().centerInside().into(holder.logoImageView);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return cards.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class CardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // each data item is just a string in this case
        public CardView logoCardView;
        public ImageView logoImageView;
        public TextView idTextView;
        private RecyclerViewClickListener recyclerViewClickListener;

        public CardViewHolder(View view, RecyclerViewClickListener recyclerViewClickListener) {
            super(view);
            logoCardView = view.findViewById(R.id.logo_cardView);
            logoImageView = view.findViewById(R.id.logo_imageView);
            idTextView = view.findViewById(R.id.number_textView);
            this.recyclerViewClickListener = recyclerViewClickListener;
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
           recyclerViewClickListener.onClick(view, Long.parseLong(idTextView.getText().toString()));
        }
    }
}
