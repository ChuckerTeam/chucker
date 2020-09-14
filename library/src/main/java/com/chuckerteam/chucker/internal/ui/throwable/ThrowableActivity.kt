package com.chuckerteam.chucker.internal.ui.throwable

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ShareCompat
import androidx.lifecycle.Observer
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerActivityThrowableBinding
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowable
import com.chuckerteam.chucker.internal.ui.BaseChuckerActivity
import java.text.DateFormat

internal class ThrowableActivity : BaseChuckerActivity() {

    private val viewModel: ThrowableViewModel by viewModels {
        ThrowableViewModelFactory(intent.getLongExtra(EXTRA_THROWABLE_ID, 0))
    }

    private lateinit var errorBinding: ChuckerActivityThrowableBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        errorBinding = ChuckerActivityThrowableBinding.inflate(layoutInflater)

        with(errorBinding) {
            setContentView(root)
            setSupportActionBar(toolbar)
            throwableItem.date.visibility = View.GONE
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
        errorBinding.apply {
            toolbarTitle.text = throwable.formattedDate
            throwableItem.tag.text = throwable.tag
            throwableItem.clazz.text = throwable.clazz
            throwableItem.message.text = throwable.message
            throwableStacktrace.text = throwable.content
        }
    }

    private val RecordedThrowable.formattedDate: String
        get() {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                .format(this.date)
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
}
