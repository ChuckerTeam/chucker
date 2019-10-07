package com.chuckerteam.chucker.internal.ui.error

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.lifecycle.Observer
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowable
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import java.text.DateFormat

private const val EXTRA_THROWABLE_ID = "EXTRA_THROWABLE_ID"
private const val TEXT_PLAIN = "text/plain"

internal class ErrorActivity : AppCompatActivity() {

    private var throwableId: Long = 0
    private var throwable: RecordedThrowable? = null

    private lateinit var title: TextView
    private lateinit var tag: TextView
    private lateinit var clazz: TextView
    private lateinit var message: TextView
    private lateinit var date: TextView
    private lateinit var stacktrace: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chucker_activity_error)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = findViewById(R.id.toolbar_title)
        tag = findViewById(R.id.tag)
        clazz = findViewById(R.id.clazz)
        message = findViewById(R.id.message)
        date = findViewById(R.id.date)
        stacktrace = findViewById(R.id.stacktrace)
        date.visibility = View.GONE

        throwableId = intent.getLongExtra(EXTRA_THROWABLE_ID, 0)
    }

    override fun onResume() {
        super.onResume()
        RepositoryProvider.throwable()
            .getRecordedThrowable(throwableId)
            .observe(
                this,
                Observer { recordedThrowable ->
                    recordedThrowable?.let {
                        throwable = it
                        populateUI(it)
                    }
                }
            )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.chucker_error, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.share_text) {
            throwable?.let { share(it) }
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun share(throwable: RecordedThrowable) {
        val text = getString(
            R.string.chucker_share_error_content,
            throwable.formattedDate,
            throwable.clazz,
            throwable.tag,
            throwable.message,
            throwable.content
        )

        startActivity(
            ShareCompat.IntentBuilder.from(this)
                .setType(TEXT_PLAIN)
                .setSubject(getString(R.string.chucker_share_error_title))
                .setText(text)
                .createChooserIntent()
        )
    }

    private fun populateUI(throwable: RecordedThrowable) {
        title.text = throwable.formattedDate
        tag.text = throwable.tag
        clazz.text = throwable.clazz
        message.text = throwable.message
        stacktrace.text = throwable.content
    }

    companion object {
        @JvmStatic
        fun newInstance(context: Context, throwableId: Long) =
            Intent(context, ErrorActivity::class.java).apply {
                putExtra(EXTRA_THROWABLE_ID, throwableId)
            }
    }

    private val RecordedThrowable.formattedDate: String
        get() {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                .format(this.date)
        }
}
