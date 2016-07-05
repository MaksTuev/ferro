package com.agna.ferro.sample.ui.screen.catalog.grid;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.agna.ferro.sample.R;
import com.agna.ferro.sample.domain.entity.Book;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

/**
 * ViewHolder for Book
 */
public class BookHolder extends RecyclerView.ViewHolder {

    private final TextView name;
    private final ImageView image;
    private final Button downloadBtn;
    private final View readBtn;
    private Book book;

    public static BookHolder newInstance(ViewGroup parent, BookItemListener itemListener) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookHolder(v, itemListener);
    }

    public BookHolder(View itemView, BookItemListener itemListener) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.book_item_name);
        image = (ImageView) itemView.findViewById(R.id.book_item_image);
        downloadBtn = (Button)itemView.findViewById(R.id.book_item_download_btn);
        readBtn = itemView.findViewById(R.id.book_item_read_btn);
        itemView.setOnClickListener(v -> itemListener.onClick(book));
        downloadBtn.setOnClickListener(v -> {
            if(!book.isDownloading()) {
                itemListener.onDownloadClick(book);
            }
        });
        readBtn.setOnClickListener(v -> itemListener.onReadClick(book));
    }

    public void bind(Book book) {
        this.book = book;
        name.setText(book.getName());
        Glide.with(image.getContext())
                .load(book.getImageUrl())
                .fitCenter()
                .placeholder(R.drawable.book_placeholder)
                .error(R.drawable.book_placeholder)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(image);
        if (book.isDownloaded()) {
            downloadBtn.setVisibility(View.GONE);
            readBtn.setVisibility(View.VISIBLE);
        } else if (book.isDownloading()){
            downloadBtn.setVisibility(View.VISIBLE);
            String downloadBtnText = downloadBtn.getResources()
                    .getString(R.string.downloading_btn, book.getDownloadProgress());
            downloadBtn.setText(downloadBtnText);
            readBtn.setVisibility(View.GONE);
        } else {
            downloadBtn.setVisibility(View.VISIBLE);
            downloadBtn.setText(R.string.download_btn);
            readBtn.setVisibility(View.GONE);
        }
    }


}
