package com.example.ghp.dagger2testdemo

import android.os.Bundle


import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    var person: Person? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var component: MainComponent  = DaggerMainComponent.builder()
                .mainModule(MainModule()).build();
        component.inject(this)
    }
}
