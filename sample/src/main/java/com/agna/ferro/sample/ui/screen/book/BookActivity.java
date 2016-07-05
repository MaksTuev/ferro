package com.agna.ferro.sample.ui.screen.book;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.agna.ferro.sample.R;
import com.agna.ferro.sample.ui.base.BaseActivity;

/**
 * Container for {@link BookFragmentView}
 */
public class BookActivity extends BaseActivity {
    private static final String EXTRA_BOOK_ID = "EXTRA_BOOK_ID";

    public static void start(Activity activity, String bookId) {
        Intent i = new Intent(activity, BookActivity.class);
        i.putExtra(EXTRA_BOOK_ID, bookId);
        activity.startActivity(i);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(BookFragmentView.class.getSimpleName()) == null) {
            Fragment fragment = BookFragmentView.create(getIntent().getStringExtra(EXTRA_BOOK_ID));
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.add(R.id.container, fragment, BookFragmentView.class.getSimpleName());
            fragmentTransaction.commit();
        }
    }
}
