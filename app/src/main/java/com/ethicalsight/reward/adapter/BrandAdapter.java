package com.ethicalsight.reward.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.ethicalsight.reward.BuildConfig;
import com.ethicalsight.reward.R;
import com.ethicalsight.reward.data.Account;
import com.ethicalsight.reward.data.Brand;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.BrandViewHolder> implements Filterable {

    private Context context;
    private List<Brand> brands, filtredBrands;
    private RecyclerViewClickListener recyclerViewClickListener;

    // Provide a suitable constructor (depends on the kind of dataset)
    public BrandAdapter(Context context, List<Brand> brands, RecyclerViewClickListener recyclerViewClickListener) {
        this.context = context;
        this.brands = brands;
        this.recyclerViewClickListener = recyclerViewClickListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public BrandAdapter.BrandViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.brand_item, parent, false);
        return new BrandViewHolder(view, recyclerViewClickListener);
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(BrandViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Brand brand = Optional.ofNullable(filtredBrands).orElse(brands).get(position);
        holder.idTextView.setText(String.valueOf(brand.getId()));
        holder.containerCardView.setCardBackgroundColor(Color.parseColor(Optional.ofNullable(brand.getBgColor()).orElse("#FFFFFF")));
        Picasso.get().load(brand.getLogo()).fit().centerInside().into(holder.logoImageView);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return Optional.ofNullable(filtredBrands).orElse(brands).size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class BrandViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // each data item is just a string in this case
        public TextView idTextView;
        public CardView containerCardView;
        public ImageView logoImageView;
        private RecyclerViewClickListener recyclerViewClickListener;

        public BrandViewHolder(View view, RecyclerViewClickListener recyclerViewClickListener) {
            super(view);
            idTextView = view.findViewById(R.id.id_textView);
            containerCardView = view.findViewById(R.id.container_cardView);
            logoImageView = view.findViewById(R.id.logo_imageView);
            this.recyclerViewClickListener = recyclerViewClickListener;
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            recyclerViewClickListener.onClick(view, Long.parseLong(idTextView.getText().toString()));
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();
                if (charSequence.length() == 0) {
                    filterResults.count = brands.size();
                    filterResults.values = brands;
                }
                else {
                    filterResults.count = (int) brands.stream().filter(b -> b.getName().toLowerCase().contains(charSequence.toString().toLowerCase()) || b.getName().isEmpty()).count();
                    filterResults.values = brands.stream().filter(b -> b.getName().toLowerCase().contains(charSequence.toString().toLowerCase()) || b.getName().isEmpty()).collect(Collectors.toList());
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filtredBrands = (List) Optional.ofNullable(filterResults.values).orElse(Collections.emptyList());
                notifyDataSetChanged();
            }
        };
    }
}
