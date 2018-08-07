package com.example.ghp.dagger2testdemo

import android.content.Context
import android.util.Log
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Scope
import javax.inject.Singleton

/**
 * 依赖组件示例
 */

class Person2 {
    var context: Context? = null
    constructor(context: Context) {
        this.context = context
        Log.i("dagger2: ", "a person2 created with context")
    }
}

@Module
class AppModule {
    var context: Context

    constructor(context: Context){
        this.context = context;
    }

    @Provides
    fun providesContext(): Context {
        return context
    }
}

@Component(modules = [(AppModule::class)])
interface AppComponent {
    fun getContext(): Context
}

@Module
class ActivityModule {

    @ActivityScope
    @Provides
//    @Singleton
    fun providePerson2(context: Context): Person2 {
        return Person2(context)
    }
}

@ActivityScope
//@Singleton
@Component(dependencies = [(AppComponent::class)], modules = [(ActivityModule::class)])
interface ActivityComponent {
    fun inject(main2Activity: Main2Activity)
}

/////////////////@Scope 自定义生命周期//////////////////////
/**
 * 测试不能和@Singleton共用
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ActivityScope