import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import java.util.Date

object MockTransactionFactory {

    internal fun createTransaction(method: String): HttpTransaction {
        return HttpTransaction(
            0, Date(1300000).time, Date(1300300).time,
            1000L, "HTTP", method, "http://localhost/getUsers",
            "localhost", "/getUsers", "", "", "",
            1000L, "application/json", null,
            null, true, 200, "OK",
            null, 1000L, "application/json",
            null, """{"field": "value"}""", true, null
        )
    }

    val expectedGetHttpTransaction =
"""URL: http://localhost/getUsers
Method: GET
Protocol: HTTP
Status: Complete
Response: 200 OK
SSL: No

Request time: ${Date(1300000)}
Response time: ${Date(1300300)}
Duration: 1000 ms

Request size: 1.0 kB
Response size: 1.0 kB
Total size: 2.0 kB

---------- Request ----------



---------- Response ----------

{
  "field": "value"
}"""

    val expectedHttpPostTransaction =
"""URL: http://localhost/getUsers
Method: POST
Protocol: HTTP
Status: Complete
Response: 200 OK
SSL: No

Request time: ${Date(1300000)}
Response time: ${Date(1300300)}
Duration: 1000 ms

Request size: 1.0 kB
Response size: 1.0 kB
Total size: 2.0 kB

---------- Request ----------



---------- Response ----------

{
  "field": "value"
}"""
}
