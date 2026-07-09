package com.example.socialmediaadiction;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaadiction.Model.UsageModel;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    List<UsageModel> list;
    Context context;

    public ReportAdapter(Context context, List<UsageModel> list) {
        this.context = context;
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvDate, tvYoutube, tvInstagram, tvFacebook, tvWhatsapp, tvChrome, tvTotal;

        // ✅ ICONS
        ImageView iconYoutube, iconInstagram, iconFacebook, iconWhatsapp, iconChrome;

        public ViewHolder(View itemView) {
            super(itemView);

            tvDate = itemView.findViewById(R.id.tvDate);
            tvYoutube = itemView.findViewById(R.id.tvYoutube);
            tvInstagram = itemView.findViewById(R.id.tvInstagram);
            tvFacebook = itemView.findViewById(R.id.tvFacebook);
            tvWhatsapp = itemView.findViewById(R.id.tvWhatsapp);
            tvChrome = itemView.findViewById(R.id.tvChrome);
            tvTotal = itemView.findViewById(R.id.tvTotal);

            // ✅ FIND ICONS
            iconYoutube = itemView.findViewById(R.id.iconYoutube);
            iconInstagram = itemView.findViewById(R.id.iconInstagram);
            iconFacebook = itemView.findViewById(R.id.iconFacebook);
            iconWhatsapp = itemView.findViewById(R.id.iconWhatsapp);
            iconChrome = itemView.findViewById(R.id.iconChrome);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        UsageModel item = list.get(position);

        // ✅ TEXT DATA
        holder.tvDate.setText(item.getDate());
        holder.tvYoutube.setText(item.getYoutube() + " min");
        holder.tvInstagram.setText(item.getInstagram() + " min");
        holder.tvFacebook.setText(item.getFacebook() + " min");
        holder.tvWhatsapp.setText(item.getWhatsapp() + " min");
        holder.tvChrome.setText(item.getChrome() + " min");
        holder.tvTotal.setText("Total: " + item.getTotal_minutes() + " min");

        // ✅ REAL APP ICONS (same as Insights)
        setIcon(holder.iconYoutube, "com.google.android.youtube");
        setIcon(holder.iconInstagram, "com.instagram.android");
        setIcon(holder.iconFacebook, "com.facebook.katana");
        setIcon(holder.iconWhatsapp, "com.whatsapp");
        setIcon(holder.iconChrome, "com.android.chrome");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ✅ SAME METHOD AS INSIGHTS (WORKING)
    private void setIcon(ImageView imageView, String packageName) {
        try {
            Drawable icon = context.getPackageManager().getApplicationIcon(packageName);
            imageView.setImageDrawable(icon);
        } catch (Exception e) {
            imageView.setImageResource(android.R.drawable.sym_def_app_icon);
        }
    }
}

//package com.example.socialmediaadiction;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.socialmediaadiction.Model.UsageModel;
//
//import java.util.List;
//
//public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
//
//    List<UsageModel> list;
//    Context context;
//
//    public ReportAdapter(Context context, List<UsageModel> list) {
//        this.context = context;
//        this.list = list;
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//
//        TextView tvDate, tvYoutube, tvInstagram, tvFacebook, tvWhatsapp, tvChrome, tvTotal;
//
//        public ViewHolder(View itemView) {
//            super(itemView);
//
//            tvDate = itemView.findViewById(R.id.tvDate);
//            tvYoutube = itemView.findViewById(R.id.tvYoutube);
//            tvInstagram = itemView.findViewById(R.id.tvInstagram);
//            tvFacebook = itemView.findViewById(R.id.tvFacebook);
//            tvWhatsapp = itemView.findViewById(R.id.tvWhatsapp);
//            tvChrome = itemView.findViewById(R.id.tvChrome);
//            tvTotal = itemView.findViewById(R.id.tvTotal);
//        }
//    }
//
//    @Override
//    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(ViewHolder holder, int position) {
//
//        UsageModel item = list.get(position);
//
//        holder.tvDate.setText(item.getDate());
//        holder.tvYoutube.setText("YouTube: " + item.getYoutube() + " min");
//        holder.tvInstagram.setText("Instagram: " + item.getInstagram() + " min");
//        holder.tvFacebook.setText("Facebook: " + item.getFacebook() + " min");
//        holder.tvWhatsapp.setText("WhatsApp: " + item.getWhatsapp() + " min");
//        holder.tvChrome.setText("Chrome: " + item.getChrome() + " min");
//        holder.tvTotal.setText("Total: " + item.getTotal_minutes() + " min");
//    }
//
//    @Override
//    public int getItemCount() {
//        return list.size();
//    }
//}