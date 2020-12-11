package android.example.threadsverificationtask

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST

private val retrofit = Retrofit.Builder()
    .baseUrl("https://onet.pl")
    .build()

interface ApiService {

    @POST("/posts")
    suspend fun postData(
        @Body requestBody: RequestBody
    ): Response<ResponseBody>
}

object Api {
    val retrofitService: ApiService by lazy { retrofit.create(ApiService::class.java) }
}