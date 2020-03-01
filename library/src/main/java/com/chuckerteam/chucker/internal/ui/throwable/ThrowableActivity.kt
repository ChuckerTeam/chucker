package com.chuckerteam.chucker.internal.ui.throwable

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.app.ShareCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowable
import com.chuckerteam.chucker.internal.ui.BaseChuckerActivity
import java.text.DateFormat

internal class ThrowableActivity : BaseChuckerActivity() {

    private lateinit var viewModel: ThrowableViewModel

    private var throwableId: Long = 0

    private lateinit var title: TextView
    private lateinit var tag: TextView
    private lateinit var clazz: TextView
    private lateinit var message: TextView
    private lateinit var date: TextView
    private lateinit var stacktrace: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chucker_activity_throwable)
        setSupportActionBar(findViewById(R.id.chuckerThrowableToolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = findViewById(R.id.chuckerErrorToolbarTitle)
        tag = findViewById(R.id.chuckerItemThrowableTag)
        clazz = findViewById(R.id.chuckerItemThrowableClazz)
        message = findViewById(R.id.chuckerItemThrowableMessage)
        date = findViewById(R.id.chuckerItemThrowableDate)
        stacktrace = findViewById(R.id.chuckerThrowableStacktrace)
        date.visibility = View.GONE

        throwableId = intent.getLongExtra(EXTRA_THROWABLE_ID, 0)

        viewModel = ViewModelProvider(this, ThrowableViewModelFactory(throwableId))
            .get(ThrowableViewModel::class.java)

        viewModel.throwable.observe(
            this,
            Observer {
                populateUI(it)
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.chucker_throwable, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.share_text) {
            viewModel.throwable.value?.let { share(it) }
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun share(throwable: RecordedThrowable) {
        val throwableDetailsText = getString(
            R.string.chucker_share_throwable_content,
            throwable.formattedDate,
            throwable.clazz,
            throwable.tag,
            throwable.message,
            throwable.content
        )
        startActivity(
            ShareCompat.IntentBuilder.from(this)
                .setType(MIME_TYPE)
                .setChooserTitle(getString(R.string.chucker_share_throwable_title))
                .setSubject(getString(R.string.chucker_share_throwable_subject))
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
            val intent = Intent(context, ThrowableActivity::class.java)
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
