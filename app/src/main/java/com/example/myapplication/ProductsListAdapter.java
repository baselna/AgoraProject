package com.example.myapplication;

import android.content.Context;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ProductsListAdapter extends ArrayAdapter<minimal_product> {
    private Context mContext;
    private int mResource;
    private int lastPosition = -1;

    private static class ViewHolder {
        TextView name;
        TextView city;
        TextView cond;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //get the persons information
        String name = getItem(position).getName();
        String city = getItem(position).getCity();
        int ID = getItem(position).getId();
        String rating = getItem(position).getCond();
        int numCond=0;
        switch (rating){
            case "very poor":
                numCond = 1;
                break;
            case "poor":
                numCond = 2;
                break;
            case "fair":
                numCond = 3 ;
                break;
            case "good":
                numCond = 4;
                break;
            case "very good":
                numCond = 5;
        }
        minimal_product prod = new minimal_product(name,city,numCond,ID);

        final View result;

        //ViewHolder object
        ViewHolder holder;

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder= new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.textView1);
            holder.city = (TextView) convertView.findViewById(R.id.textView2);
            holder.cond = (TextView) convertView.findViewById(R.id.textView3);

            result = convertView;

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
            result = convertView;
        }


        Animation animation = AnimationUtils.loadAnimation(mContext,
                (position > lastPosition) ? R.anim.load_down_anim : R.anim.load_up_anim);
        result.startAnimation(animation);
        lastPosition = position;

        holder.name.setText(prod.getName());
        holder.city.setText(prod.getCity());
        holder.cond.setText(prod.getCond());


        return convertView;
    }

    public ProductsListAdapter(Context context, int resource, ArrayList<minimal_product> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
    }
}
