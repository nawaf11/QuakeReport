package com.example.android.quakereport;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

class EqAdapter extends ArrayAdapter<Earthquake> {

     EqAdapter(Context context , ArrayList<Earthquake> objects) {
        super(context, 0 , objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView==null) listItemView= LayoutInflater.from(getContext()).inflate(R.layout.eq_view,parent,false);

        TextView mag_txtV = (TextView) listItemView.findViewById(R.id.txt_mag);
        TextView dist_txtV = (TextView) listItemView.findViewById(R.id.txt_dist);
        TextView loc_txtV = (TextView)listItemView.findViewById(R.id.txt_location);
        TextView time_txtV = (TextView) listItemView.findViewById(R.id.txt_time);


        mag_txtV.setText(String.valueOf(getItem(position).getMag()));
        dist_txtV.setText(getItem(position).getDistance());
       loc_txtV.setText(getItem(position).getLocation());
        time_txtV.setText(getItem(position).getDateTime());

        // change magBackgroundColor
        GradientDrawable circle_mag = (GradientDrawable) mag_txtV.getBackground();
        int mag_color = getMagnitudeColor(getItem(position).getMag());
        circle_mag.setColor(mag_color);
       // mag_txtV.setWidth(120);
       // mag_txtV.setHeight(120);


        return listItemView;
    }
     private int getMagnitudeColor(double magnitude) {
         int magnitudeColorResourceId;
         int magnitudeFloor = (int) Math.floor(magnitude);
         switch (magnitudeFloor) {
             case 0:
             case 1:magnitudeColorResourceId = R.color.magnitude1;break;
             case 2:magnitudeColorResourceId = R.color.magnitude2;break;
             case 3:magnitudeColorResourceId = R.color.magnitude3;break;
             case 4:magnitudeColorResourceId = R.color.magnitude4;break;
             case 5:magnitudeColorResourceId = R.color.magnitude5;break;
             case 6:magnitudeColorResourceId = R.color.magnitude6;break;
             case 7:magnitudeColorResourceId = R.color.magnitude7;break;
             case 8:magnitudeColorResourceId = R.color.magnitude8;break;
             case 9:magnitudeColorResourceId = R.color.magnitude9;break;
             default:magnitudeColorResourceId = R.color.magnitude10plus;break;
         }
         return ContextCompat.getColor(getContext(), magnitudeColorResourceId);
     }


}

 class Earthquake{
        private final double mag;
        private String date_Time;
        private String distFromPlace;
        private String location;
        private String url;

      Earthquake(double mag,String unix,String place,String url0){
          DecimalFormat decFormat=(DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH);
          decFormat.applyPattern("0.0");
         this.mag=Double.parseDouble(decFormat.format(mag));
         date_Time=getDateTimeFromUnix(Long.parseLong(unix));
         distFromPlace=getDistPart(place);
         location=geLocationPart(place);
          url=url0;
     }

     private String getDateTimeFromUnix(long unix){

         SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
         SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm a", Locale.US);
         return dateFormatter.format(new Date(unix))+"\n"+timeFormatter.format(new Date(unix));
     }
     private String getDistPart(String place){
         if (!place.contains(" of ")) return "";

         return place.substring(0,place.indexOf(" of ")+4);
     }
     private String geLocationPart(String place){
         return place.substring(place.indexOf(" of ")+4,place.length());
     }



      double getMag() {
         return mag;
     }

      String getDateTime() {

         return date_Time;
     }

      String getLocation() {
         return location;
     }

     public String getUrl() {
         return url;
     }
      String getDistance() {
         return distFromPlace;
     }
 }
