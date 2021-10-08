package com.ethicalsight.reward.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ethicalsight.reward.BuildConfig;
import com.ethicalsight.reward.R;
import com.ethicalsight.reward.data.Account;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private Context context;
    private Account account;
    private int itemCount;

    // Provide a suitable constructor (depends on the kind of dataset)
    public AccountAdapter(Context context, Account account, int itemCount) {
        this.context = context;
        this.account = account;
        this.itemCount = itemCount;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AccountAdapter.AccountViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.account_item, parent, false);
        return new AccountViewHolder(view);
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(AccountViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        switch (position) {
            case 0:
                holder.nameTextView.setText(context.getText(R.string.account_id));
                holder.valueTextView.setText(account.getId());
                break;
            case 1:
                holder.nameTextView.setText(context.getText(R.string.version));
                holder.valueTextView.setText(BuildConfig.VERSION_NAME);
                break;
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return itemCount;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class AccountViewHolder extends RecyclerView.ViewHolder {

        // each data item is just a string in this case
        public TextView nameTextView;
        public TextView valueTextView;

        public AccountViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.name_textView);
            valueTextView = view.findViewById(R.id.value_textView);
        }
    }
}
