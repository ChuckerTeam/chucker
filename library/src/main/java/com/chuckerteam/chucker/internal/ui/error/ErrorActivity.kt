package com.chuckerteam.chucker.internal.ui.error

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.app.ShareCompat
import androidx.lifecycle.Observer
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowable
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.ui.BaseChuckerActivity
import java.text.DateFormat

internal class ErrorActivity : BaseChuckerActivity() {

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
        val throwableDetailsText = getString(
            R.string.chucker_share_error_content,
            throwable.formattedDate,
            throwable.clazz,
            throwable.tag,
            throwable.message,
            throwable.content
        )
        startActivity(
            ShareCompat.IntentBuilder.from(this)
                .setType(MIME_TYPE)
                .setChooserTitle(getString(R.string.chucker_share_error_title))
                .setSubject(getString(R.string.chucker_share_error_subject))
                .setText(throwableDetailsText)
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
        private const val MIME_TYPE = "text/plain"
        private const val EXTRA_THROWABLE_ID = "transaction_id"

        fun start(context: Context, throwableId: Long) {
            val intent = Intent(context, ErrorActivity::class.java)
            intent.putExtra(EXTRA_THROWABLE_ID, throwableId)
            context.startActivity(intent)
        }
    }

    private val RecordedThrowable.formattedDate: String
        get() {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                .format(this.date)
        }
}
