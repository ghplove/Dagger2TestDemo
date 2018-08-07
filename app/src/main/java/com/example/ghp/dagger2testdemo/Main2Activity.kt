package com.example.ghp.dagger2testdemo

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

class Main2Activity : AppCompatActivity() {
    @Inject
    @JvmField
    var person2: Person2? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        ///////////依赖组件///////////
        var appComponent: AppComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()
        var activityComponent: ActivityComponent = DaggerActivityComponent.builder()
                .appComponent(appComponent)
                .activityModule(ActivityModule())
                .build()
        activityComponent.inject(this)
    }
}
