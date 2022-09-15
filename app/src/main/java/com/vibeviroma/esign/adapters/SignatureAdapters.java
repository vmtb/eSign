package com.vibeviroma.esign.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.vibeviroma.esign.R;
import com.vibeviroma.esign.ViewImage;
import com.vibeviroma.esign.objects.Signature;
import com.vibeviroma.esign.tools.Cte;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class SignatureAdapters extends RecyclerView.Adapter<SignatureAdapters.viewHolder> {
    private Context ctx;
    private List<Signature> signatureList;

    public SignatureAdapters(Context ctx, List<Signature> signatureList) {
        this.ctx = ctx;
        this.signatureList = signatureList;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new viewHolder(LayoutInflater.from(ctx).inflate(R.layout.item_signature, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Signature signature= signatureList.get(position);
        if(!signature.getLien_image().isEmpty()){
            Glide.with(ctx).load(signature.getLien_image()).placeholder(R.drawable.logo).into(holder.img_sign);
        }else {
            holder.img_sign.setImageURI(Uri.fromFile(new File(signature.getLocal_image())));
        }

        Calendar c=Calendar.getInstance();
        c.setTimeInMillis(signature.getTime());
        @SuppressLint("SimpleDateFormat") String date= new SimpleDateFormat("dd MMM yyyy, hh:mm").format(c.getTime());

        String very_type="Fond transparent";
        if(signature.getType().equals(Cte.TYPE_JPG))
            very_type="Fond plein";
        holder.leg.setText(very_type.concat("\n").concat(date));

        holder.itemView.setOnClickListener(v -> ctx.startActivity(new Intent(ctx, ViewImage.class).putExtra("SIGNATURE", signature)));

    }

    @Override
    public int getItemCount() {
        return signatureList.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        private final RoundedImageView img_sign;
        private final TextView leg;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            img_sign= itemView.findViewById(R.id.img_sign);
            leg= itemView.findViewById(R.id.leg);
        }
    }
}
