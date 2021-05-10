package com.youmuyun.thridpartpublisherapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.seu.magicfilter.utils.MagicFilterType;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterHolder>{
    private Context context;
    private MagicFilterType[] filters;
    private onFilterChangeListener onFilterChangeListener;

    public FilterAdapter(Context context, MagicFilterType[] filters) {
        this.filters = filters;
        this.context = context;
    }

    @NonNull
    @Override
    public FilterAdapter.FilterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.filter_item_layout,
                parent, false);
        FilterHolder viewHolder = new FilterHolder(view);
        viewHolder.filterImage = (ImageView) view
                .findViewById(R.id.filter_image);
        viewHolder.filterName = (TextView) view
                .findViewById(R.id.filter_name);
        viewHolder.filterRoot = (FrameLayout)view
                .findViewById(R.id.filter_root);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FilterAdapter.FilterHolder holder, final int position) {
        holder.filterImage.setImageResource(FilterTypeHelper.FilterType2Thumb(filters[position]));
        holder.filterName.setText(FilterTypeHelper.FilterType2Name(filters[position]));

        holder.filterRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyItemChanged(position);
                onFilterChangeListener.onFilterChanged(filters[position]);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filters == null ? 0 : filters.length;
    }

    class FilterHolder extends RecyclerView.ViewHolder {
        ImageView filterImage;
        TextView filterName;
        FrameLayout filterRoot;

        public FilterHolder(View itemView) {
            super(itemView);
        }
    }

    public interface onFilterChangeListener{
        void onFilterChanged(MagicFilterType filterType);
    }

    public void setOnFilterChangeListener(onFilterChangeListener onFilterChangeListener){
        this.onFilterChangeListener = onFilterChangeListener;
    }
}
