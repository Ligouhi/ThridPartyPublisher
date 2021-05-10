package com.youmuyun.thridpartpublisherapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SegmentAdapter extends RecyclerView.Adapter<SegmentAdapter.ViewHolder> {
    OnItemClickListener onItemClickListener;
    Context context;
    List<SegmentData> segmentDataList = new ArrayList<>();

    public SegmentAdapter(Context context, List<SegmentData> segmentDataList) {
        this.context = context;
        this.segmentDataList = segmentDataList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView segImage;
        TextView segName;
        FrameLayout segRoot;

        public ViewHolder(View view) {
            super(view);
            segImage = (ImageView) view
                    .findViewById(R.id.filter_image);
            segName = (TextView) view
                    .findViewById(R.id.filter_name);
            segRoot = (FrameLayout)view
                    .findViewById(R.id.filter_root);
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.filter_item_layout,
                parent, false);
       ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
       SegmentData segmentData = segmentDataList.get(position);
       holder.segName.setText(segmentData.getName());
       holder.segImage.setImageResource(segmentData.getImg_src());
       holder.segRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyItemChanged(position);
                onItemClickListener.onItemClick(segmentDataList.get(position).getName());
            }
        });

    }


    public interface OnItemClickListener {
        void onItemClick(String name);
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }
    @Override
    public int getItemCount() {
        return segmentDataList.size();
    }
}
