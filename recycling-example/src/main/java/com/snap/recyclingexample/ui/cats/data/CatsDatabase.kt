package com.snap.recyclingexample.ui.cats.data

import android.net.Uri
import com.snap.recyclingexample.ui.cats.CHEETAH_URI
import com.snap.recyclingexample.ui.cats.HIMALAYAN_URI
import com.snap.recyclingexample.ui.cats.LEOPARD_URI
import com.snap.recyclingexample.ui.cats.LION_URI
import com.snap.recyclingexample.ui.cats.OCICAT_URI
import com.snap.recyclingexample.ui.cats.PANTHER_URI
import com.snap.recyclingexample.ui.cats.PERSIAN_URI
import com.snap.recyclingexample.ui.cats.RAGDOLL_URI
import com.snap.recyclingexample.ui.cats.SIAMESE_URI
import com.snap.recyclingexample.ui.cats.TABBY_URI
import com.snap.recyclingexample.ui.cats.TIGER_URI
import com.snap.ui.seeking.Seekable
import com.snap.ui.seeking.Seekables
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class CatsDatabase {
    fun observeTheCats(): Observable<Seekable<CatData>> {

        return Observable.fromCallable<Seekable<CatData>> {
            // Simulates a stable id
            var catId = 0L

            Seekables.copyOf(listOf(
                    CatData(
                            catId++,
                            CatType.Big,
                            TIGER_URI,
                            "Tiger"
                    ),
                    CatData(
                            catId++,
                            CatType.Big,
                            LION_URI,
                            "Lion"
                    ),
                    CatData(
                            catId++,
                            CatType.Big,
                            LEOPARD_URI,
                            "Leopard"
                    ),
                    CatData(
                            catId++,
                            CatType.Big,
                            CHEETAH_URI,
                            "Cheetah"
                    ),
                    CatData(
                            catId++,
                            CatType.Big,
                            PANTHER_URI,
                            "Panther"
                    ),
                    CatData(
                            catId++,
                            CatType.Medium,
                            RAGDOLL_URI,
                            "Ragdoll"
                    ),
                    CatData(
                            catId++,
                            CatType.Medium,
                            TABBY_URI,
                            "Tabby"
                    ),
                    CatData(
                            catId++,
                            CatType.Medium,
                            HIMALAYAN_URI,
                            "Himalayan"
                    ),
                    CatData(
                            catId++,
                            CatType.Small,
                            PERSIAN_URI,
                            "Persian"
                    ),
                    CatData(
                            catId++,
                            CatType.Small,
                            SIAMESE_URI,
                            "Siamese"
                    ),
                    CatData(
                            catId,
                            CatType.Small,
                            OCICAT_URI,
                            "Ocicat"
                    )
            ))
        }.subscribeOn(Schedulers.io())
    }
}

enum class CatType { Big, Medium, Small }

class CatData(
        val id: Long,
        val type: CatType,
        val imageUri: Uri,
        val name: String
)
