package com.chuckerteam.chucker.sample

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import kotlinx.android.synthetic.main.activity_main.do_http
import kotlinx.android.synthetic.main.activity_main.launch_chucker_directly
import kotlinx.android.synthetic.main.activity_main.trigger_exception
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("MagicNumber")
class MainActivity : AppCompatActivity() {

    private lateinit var collector: ChuckerCollector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        do_http.setOnClickListener { doHttpActivity() }
        trigger_exception.setOnClickListener { triggerException() }

        with(launch_chucker_directly) {
            visibility = if (Chucker.isOp) View.VISIBLE else View.GONE
            setOnClickListener { launchChuckerDirectly() }
        }

        collector = ChuckerCollector(
            context = applicationContext,
            showNotification = true,
            retentionPeriod = RetentionManager.Period.ONE_HOUR
        )

        Chucker.registerDefaultCrashHandler(collector)
    }

    private fun getClient(context: Context): OkHttpClient {
        val chuckerInterceptor = ChuckerInterceptor(
            context = context,
            collector = collector,
            maxContentLength = 250000L
        )

        return OkHttpClient.Builder()
            // Add a ChuckerInterceptor instance to your OkHttp client
            .addInterceptor(chuckerInterceptor)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }

    private fun launchChuckerDirectly() {
        // Optionally launch Chucker directly from your own app UI
        startActivity(Chucker.getLaunchIntent(this, Chucker.SCREEN_HTTP))
    }

    private fun doHttpActivity() {
        val cb = object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {}
            override fun onFailure(call: Call<Void>, t: Throwable) {
                t.printStackTrace()
            }
        }

        with(SampleApiService.getInstance(getClient(applicationContext))) {
            get().enqueue(cb)
            post(SampleApiService.Data("posted")).enqueue(cb)
            patch(SampleApiService.Data("patched")).enqueue(cb)
            put(SampleApiService.Data("put")).enqueue(cb)
            delete().enqueue(cb)
            status(201).enqueue(cb)
            status(401).enqueue(cb)
            status(500).enqueue(cb)
            delay(9).enqueue(cb)
            delay(15).enqueue(cb)
            redirectTo("https://http2.akamai.com").enqueue(cb)
            redirect(3).enqueue(cb)
            redirectRelative(2).enqueue(cb)
            redirectAbsolute(4).enqueue(cb)
            stream(500).enqueue(cb)
            streamBytes(2048).enqueue(cb)
            image("image/png").enqueue(cb)
            gzip().enqueue(cb)
            xml().enqueue(cb)
            utf8().enqueue(cb)
            deflate().enqueue(cb)
            cookieSet("v").enqueue(cb)
            basicAuth("me", "pass").enqueue(cb)
            drip(512, 5, 1, 200).enqueue(cb)
            deny().enqueue(cb)
            cache("Mon").enqueue(cb)
            cache(30).enqueue(cb)
        }

        with(SampleApiService.getGraphQLInstance(getClient(this))) {
            val filmsQuery = "query AllFilms {\n" +
                    "  allFilms(first: 10) {\n" +
                    "    totalCount\n" +
                    "    films {\n" +
                    "      id\n" +
                    "      title\n" +
                    "    }\n" +
                    "  }\n" +
                    "}\n"
            getAllFilms(GraphQLResponseBody(filmsQuery, "AllFilms")).enqueue(cb)

            val vehiclesQuery = "query AllVehicles {\n" +
                    "    allVehicles(first: 10) {\n" +
                    "        totalCount\n" +
                    "        pageInfo {\n" +
                    "            startCursor\n" +
                    "            endCursor\n" +
                    "            hasNextPage\n" +
                    "            hasPreviousPage\n" +
                    "        }\n" +
                    "        vehicles {\n" +
                    "            id\n" +
                    "            created\n" +
                    "            vehicleClass\n" +
                    "            passengers\n" +
                    "            name\n" +
                    "        }\n" +
                    "    }\n" +
                    "} "
            getAllVehicles(GraphQLResponseBody(vehiclesQuery, "AllVehicles")).enqueue(cb)

            val peopleQuery = "query AllPeople {\n" +
                    "    allPeople(first: 10) {\n" +
                    "        totalCount\n" +
                    "        pageInfo {\n" +
                    "            startCursor\n" +
                    "            endCursor\n" +
                    "            hasNextPage\n" +
                    "            hasPreviousPage\n" +
                    "        }\n" +
                    "        people {\n" +
                    "            id\n" +
                    "            birthYear\n" +
                    "            created\n" +
                    "            name\n" +
                    "        }\n" +
                    "    }\n" +
                    "} "
            getAllPeople(GraphQLResponseBody(peopleQuery, "AllPeople")).enqueue(cb)

            val planetsQuery = "query AllPlanets {\n" +
                    "    allPlanets(first: 10) {\n" +
                    "        totalCount\n" +
                    "        pageInfo {\n" +
                    "            startCursor\n" +
                    "            endCursor\n" +
                    "            hasNextPage\n" +
                    "            hasPreviousPage\n" +
                    "        }\n" +
                    "        planets {\n" +
                    "            id\n" +
                    "            gravity\n" +
                    "            created\n" +
                    "            name\n" +
                    "        }\n" +
                    "    }\n" +
                    "}"
            getAllPlanets(GraphQLResponseBody(planetsQuery, "AllPlanets")).enqueue(cb)

            val spaciesQuery = "query AllSpacies {\n" +
                    "    allSpecies(first: 10) {\n" +
                    "        totalCount\n" +
                    "        pageInfo {\n" +
                    "            startCursor\n" +
                    "            endCursor\n" +
                    "            hasNextPage\n" +
                    "            hasPreviousPage\n" +
                    "        }\n" +
                    "        species {\n" +
                    "            id\n" +
                    "            created\n" +
                    "            eyeColors\n" +
                    "            hairColors\n" +
                    "            skinColors\n" +
                    "            name\n" +
                    "        }\n" +
                    "    }\n" +
                    "} \n"
            getAllSpacies(GraphQLResponseBody(spaciesQuery, "AllSpacies")).enqueue(cb)

            val starshipsQuery = "query AllStarships {\n" +
                    "    allStarships(first: 10) {\n" +
                    "        totalCount\n" +
                    "        pageInfo {\n" +
                    "            startCursor\n" +
                    "            endCursor\n" +
                    "            hasNextPage\n" +
                    "            hasPreviousPage\n" +
                    "        }\n" +
                    "        starships {\n" +
                    "            id\n" +
                    "            created\n" +
                    "            starshipClass\n" +
                    "            passengers\n" +
                    "            name\n" +
                    "        }\n" +
                    "    }\n" +
                    "} "
            getAllStarships(GraphQLResponseBody(starshipsQuery, "AllStarships")).enqueue(cb)
        }
    }

    private fun triggerException() {
        collector.onError("Example button pressed", RuntimeException("User triggered the button"))
        // You can also throw exception, it will be caught thanks to "Chucker.registerDefaultCrashHandler"
        // throw new RuntimeException("User triggered the button");
    }
}
