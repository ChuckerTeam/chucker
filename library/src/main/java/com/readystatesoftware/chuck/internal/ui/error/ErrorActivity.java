package com.readystatesoftware.chuck.internal.ui.error;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.readystatesoftware.chuck.R;
import com.readystatesoftware.chuck.internal.data.entity.RecordedThrowable;
import com.readystatesoftware.chuck.internal.data.repository.ChuckerRepositoryProvider;

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
        setContentView(R.layout.chuck_activity_error);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title = findViewById(R.id.toolbar_title);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

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
        ChuckerRepositoryProvider.it().getRecordedThrowable(throwableId).observe(this, new Observer<RecordedThrowable>() {
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
        inflater.inflate(R.menu.chuck_error, menu);
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
        String text = getString(R.string.chuck_share_error_content,
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(throwable.getDate()),
                throwable.getClazz(),
                throwable.getTag(),
                throwable.getMessage(),
                throwable.getContent());

        startActivity(ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setSubject(getString(R.string.chuck_share_error_title))
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
