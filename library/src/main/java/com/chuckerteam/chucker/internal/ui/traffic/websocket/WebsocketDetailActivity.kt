package com.chuckerteam.chucker.internal.ui.traffic.websocket

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.WebsocketOperation
import com.chuckerteam.chucker.internal.support.formatBytes
import com.chuckerteam.chucker.internal.ui.BaseChuckerActivity

class WebsocketDetailActivity : BaseChuckerActivity() {
    private lateinit var viewModel: WebsocketTrafficViewModel
    private lateinit var operation: TextView
    private lateinit var timestamp: TextView
    private lateinit var url: TextView
    private lateinit var ssl: TextView
    private lateinit var size: TextView
    private lateinit var body: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chucker_activity_websocket_detail)

        timestamp = findViewById(R.id.timestamp)
        operation = findViewById(R.id.operation)
        url = findViewById(R.id.url)
        ssl = findViewById(R.id.ssl)
        size = findViewById(R.id.size)
        body = findViewById(R.id.body)

        val trafficId = intent.getLongExtra(ARG_TRAFFIC_ID, 0)
        viewModel = ViewModelProviders.of(
            this,
            WebsocketTrafficViewModelFactory(
                trafficId,
                getString(WebsocketOperation.SEND.descriptionId),
                getString(WebsocketOperation.MESSAGE.descriptionId)
            )
        )[WebsocketTrafficViewModel::class.java]
        viewModel.loadWebsocketTraffic()

        setSupportActionBar(findViewById(R.id.toolbar))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onResume() {
        super.onResume()

        viewModel.trafficTitle.observe(
            this,
            Observer {
                findViewById<TextView>(R.id.toolbar_title).text = it
            }
        )

        viewModel.traffic.observe(
            this,
            Observer { traffic ->
                timestamp.text = "${traffic.timestamp}"
                url.text = traffic.url
                body.text = traffic.contentText
                size.text = traffic.contentText?.length?.formatBytes()
                operation.text = getString(traffic.operation.descriptionId)
                ssl.text = getString(if (traffic.ssl == true) R.string.chucker_yes else R.string.chucker_no)
            }
        )
    }

    companion object {
        private const val ARG_TRAFFIC_ID = "transaction_id"

        fun start(context: Context, trafficId: Long) {
            context.startActivity(
                Intent(context, WebsocketDetailActivity::class.java).apply {
                    putExtra(ARG_TRAFFIC_ID, trafficId)
                }
            )
        }
    }
}
