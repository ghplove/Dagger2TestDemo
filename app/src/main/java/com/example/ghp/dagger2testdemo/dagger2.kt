package com.example.ghp.dagger2testdemo

import android.util.Log
import dagger.Component
import dagger.Module
import dagger.Provides

class Person {
    constructor() {
        Log.i("dagger2: ", "a person created")
    }
}

@Module
class MainModule {
    @Provides
    fun providesPerson(): Person {
        Log.i("dagger2: ","a person created from MainModule")
        return Person()
    }
}

@Component(modules = [(MainModule::class)])
interface MainComponent {

    fun inject(mainActivity: MainActivity)
}