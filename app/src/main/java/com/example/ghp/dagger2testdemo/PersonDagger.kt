package com.example.ghp.dagger2testdemo

import android.content.Context
import android.util.Log
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * 自定义标签
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PersonWithContext

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PersonWithName


class Person {
    var context: Context? = null
    var name: String? = null
    constructor(context: Context) {
        this.context = context
        Log.i("dagger2: ", "a person created with context")
    }

    constructor(name: String) {
        this.name = name
        Log.i("dagger2: ", "a person created with name")
    }
}

@Module
class MainModule {
    var context: Context
    constructor(context: Context){
        this.context = context
    }

    /**
     * 在@Module注解的类中，使用@Provider注解，说明提供依赖注入的具体对象
     */
    @Provides
    fun providersContext(): Context {
        return this.context
    }
    /**
     * person 和 person2 都指向同一个Person对象了？ 使用 @Singleton 注解
     * Provides和Component都需要添加@Singleton，因为单例是基于Component的
     * @Named标签 标注表示要注入时使用哪一种
     * @PersinWithContext 自定义的标签
     */
    @Named("context")
//    @PersonWithContext
    @Singleton
    @Provides
    fun providesPersonWithContext(context: Context): Person {
        Log.i("dagger2: ","a person created from WithContext")
        return Person(context)
    }

    @Named("string")
//    @PersonWithName
    @Singleton
    @Provides
    fun providersPersonWithName(): Person {
        Log.i("dagger2: ","a person created from WithName")
        return Person("ghp")
    }
}

/**
 * @Component 可以通过Component访问到Module中提供的依赖注入对象
 * @Sigleton 被注解的对象，在App中是单例存在的！
 */
@Singleton
@Component(modules = [(MainModule::class)])
interface MainComponent {
    fun inject(mainActivity: MainActivity)
}