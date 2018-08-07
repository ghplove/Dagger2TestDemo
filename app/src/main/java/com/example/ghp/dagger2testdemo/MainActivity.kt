package com.example.ghp.dagger2testdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider

class MainActivity : AppCompatActivity() {

    /**
     * @Inject
     * 这个注解是用来说明该注解下方的属性或方法需要依赖注入
     */
    @field:Named("context")
//    @field:PersonWithContext
    @Inject
    @JvmField
    var person: Person? = null

    /**
     * 变量这行代码编译为 Java 字节码的时候会对应三个目标元素，一个是变量本身、还有 getter 和 setter，
     * Kotlin 不知道这个变量的注解应该使用到那个目标上。
     * 要解决这个方式，需要使用 Kotlin 提供的注解目标关键字来告诉 Kotlin 所注解的目标是那个，
     * 示例中需要注解应用到 变量上，所以使用 field 关键字：
     */
    @field:Named("string")
//    @field:PersonWithName
    @Inject
    @JvmField
    var person1: Person? = null

    /**
     * 懒加载Lazy
     * 多次get 的是同一个对象
     */
//    @Inject
//    var lazyPerson: Lazy<Person>? = null

    /**
     * 强制重新加载Provider
     * 多次get，每次get都会尝试创建新的对象。
     */
//    @Inject
//    var providerPerson: Provider<Person>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var component: MainComponent  = DaggerMainComponent.builder()
                .mainModule(MainModule(this)).build()
        component.inject(this)


    }
}
