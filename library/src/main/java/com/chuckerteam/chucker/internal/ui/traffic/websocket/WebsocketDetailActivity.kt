package com.chuckerteam.chucker.internal.ui.traffic.websocket

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.WebsocketOperation
import com.chuckerteam.chucker.internal.ui.BaseChuckerActivity

class WebsocketDetailActivity : BaseChuckerActivity() {
    private lateinit var viewModel: WebsocketTrafficViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chucker_activity_websocket_detail)

        // Create the instance now, so it can be shared by the
        // various fragments in the view pager later.
        val trafficId = intent.getLongExtra(ARG_TRAFFIC_ID, 0)
        viewModel = ViewModelProviders
            .of(
                this, WebsocketTrafficViewModelFactory(
                    trafficId,
                    getString(WebsocketOperation.SEND.descriptionId),
                    getString(WebsocketOperation.MESSAGE.descriptionId)
                )
            )
            .get(WebsocketTrafficViewModel::class.java)
        viewModel.loadWebsocketTraffic()

        setSupportActionBar(findViewById(R.id.toolbar))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onResume() {
        super.onResume()

        viewModel.trafficTitle.observe(this, Observer {
            findViewById<TextView>(R.id.toolbar_title).text = it
        })

        viewModel.traffic.observe(this, Observer { traffic ->
        })
    }

    companion object {
        private const val ARG_TRAFFIC_ID = "transaction_id"

        fun start(context: Context, trafficId: Long) {
            context.startActivity(Intent(context, WebsocketDetailActivity::class.java).apply {
                putExtra(ARG_TRAFFIC_ID, trafficId)
            })
        }
    }
}