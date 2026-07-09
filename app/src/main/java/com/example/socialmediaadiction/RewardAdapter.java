package com.example.socialmediaadiction;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.socialmediaadiction.Model.RewardModel;

import java.util.List;
public class RewardAdapter extends ArrayAdapter<RewardModel> {

    public RewardAdapter(Context context, List<RewardModel> list) {
        super(context, 0, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_reward, parent, false);
        }

        RewardModel item = getItem(position);

        TextView tvDate = convertView.findViewById(R.id.tvDate);
        TextView tvPoints = convertView.findViewById(R.id.tvPoints);
        TextView tvStatus = convertView.findViewById(R.id.tvStatus);
        TextView tvSub = convertView.findViewById(R.id.tvSub);

        tvDate.setText(item.date);

        if (item.points > 0) {
            tvPoints.setText("+"  +  item.points);
            tvStatus.setText("✔");
//            tvStatus.setBackgroundColor(Color.parseColor("#000000"));
            tvSub.setText("Rewarded");
        } else {
            tvPoints.setText("0 ");
            tvStatus.setText("✖");
           // tvStatus.setBackgroundColor(Color.parseColor("#000000"));
            tvSub.setText("Reward Missed ");
        }

        return convertView;
    }
}