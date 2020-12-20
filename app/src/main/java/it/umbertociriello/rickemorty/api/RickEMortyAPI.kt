package it.umbertociriello.rickemorty.api

import android.util.Log
import com.google.gson.Gson
import com.squareup.okhttp.*
import it.umbertociriello.rickemorty.models.CharacterResponse
import org.json.JSONException
import java.io.IOException

object RickEMortyAPI {
    private val TAG = "CharacterAPI"

    private fun getCharacterUrl(id: String): HttpUrl {
        return HttpUrl.Builder()
            .scheme("https")
            .host("rickandmortyapi.com")
            .addPathSegment("api")
            .addPathSegment("character")
            .addPathSegment(id)
            .build()
    }

    private fun getCharacterRequest(id: String): Request {
        return Request.Builder()
            .url(getCharacterUrl(id))
            .get()
            .build()

    }

    fun getCharacter(id: String, characterInterface: CharacterInterface) {
        val client = OkHttpClient()
        client.newCall(getCharacterRequest(id)).enqueue(object : Callback {
            override fun onFailure(request: Request?, e: IOException?) {
                characterInterface.onResponseError(request, e)
            }

            @Throws(IOException::class)
            override fun onResponse(response: Response?) {
                response?.body()?.let {
                    when (response.code()) {
                        200 -> {
                            try {
                                val responseString = it.string()
                                characterInterface.onResponseSuccessful(
                                    Gson().fromJson(
                                        responseString,
                                        CharacterResponse::class.java
                                    )
                                )
                                Log.d(TAG, "Chiamata API corretta: $responseString")
                            } catch (e: JSONException) {
                                Log.e(TAG, "Parse JSON fallito!")
                                characterInterface.onResponseError(
                                    null,
                                    Exception("Parse JSON fallito")
                                )
                            } catch (e2: Exception) {
                                Log.e(TAG, e2.toString())
                                characterInterface.onResponseError(
                                    null,
                                    Exception("Errore nel parse risposta")
                                )
                            }
                        }
                        else -> {
                            characterInterface.onResponseError(
                                null,
                                IOException("Errore nella risposta")
                            )
                        }
                    }
                }
            }

        })
    }

    interface CharacterInterface {
        fun onResponseSuccessful(character: CharacterResponse)
        fun onResponseError(request: Request?, exception: Exception?)
    }
}