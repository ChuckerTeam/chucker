package com.chuckerteam.chucker.api.internal.ui.error;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ShareCompat;
import androidx.lifecycle.Observer;

import com.chuckerteam.chucker.R;
import com.chuckerteam.chucker.api.internal.data.entity.RecordedThrowable;
import com.chuckerteam.chucker.api.internal.data.repository.RepositoryProvider;

import java.text.DateFormat;

/**
 * @author Olivier Perez
 */
public class ErrorActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "EXTRA_ID";
    private long throwableId;
    private RecordedThrowable throwable;

    private TextView title;
    private TextView tag;
    private TextView clazz;
    private TextView message;
    private TextView date;
    private TextView stacktrace;

    public static void start(Context context, Long id) {
        Intent intent = new Intent(context, ErrorActivity.class);
        intent.putExtra(EXTRA_ID, id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chucker_activity_error);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title = findViewById(R.id.toolbar_title);

        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        tag = findViewById(R.id.tag);
        clazz = findViewById(R.id.clazz);
        message = findViewById(R.id.message);
        date = findViewById(R.id.date);
        stacktrace = findViewById(R.id.stacktrace);

        date.setVisibility(View.GONE);

        throwableId = getIntent().getLongExtra(EXTRA_ID, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RepositoryProvider.throwable().getRecordedThrowable(throwableId).observe(this, new Observer<RecordedThrowable>() {
            @Override
            public void onChanged(@Nullable RecordedThrowable recordedThrowable) {
                if (recordedThrowable != null) {
                    populateUI(recordedThrowable);
                    throwable = recordedThrowable;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chucker_error, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.share_text) {
            share(throwable);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void share(RecordedThrowable throwable) {
        String text = getString(R.string.chucker_share_error_content,
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(throwable.getDate()),
                throwable.getClazz(),
                throwable.getTag(),
                throwable.getMessage(),
                throwable.getContent());

        startActivity(ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setSubject(getString(R.string.chucker_share_error_title))
                .setText(text)
                .createChooserIntent());
    }

    private void populateUI(RecordedThrowable throwable) {
        String dateStr = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(throwable.getDate());
        title.setText(dateStr);
        tag.setText(throwable.getTag());
        clazz.setText(throwable.getClazz());
        message.setText(throwable.getMessage());
        stacktrace.setText(throwable.getContent());
    }
}
