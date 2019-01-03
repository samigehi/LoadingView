package com.github.loading

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.github.loadingview.LoadingDialog
import com.github.loadingview.LoadingView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit


class SampleActivity() : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        findViewById<View?>(R.id.button)?.setOnClickListener {
            val dialog = LoadingDialog[this@SampleActivity].show()
            Observable.timer(java.util.Random().nextInt(9999).toLong(), TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        dialog.hide();
                    }

        }
        val loading = findViewById<LoadingView>(R.id.loadingView)
        loading?.start()
    }
}