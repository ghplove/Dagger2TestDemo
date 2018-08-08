# Dagger2TestDemo
[Dagger官网](https://github.com/google/dagger)  
[Dagger Document API](http://google.github.io/dagger/)  
[Dagger2TestDemo](https://github.com/ghplove/Dagger2TestDemo)  

#### Dagger2是什么?
Dagger2是Dagger的升级版，是一个依赖注入框架,第一代由大名鼎鼎的Square公司共享出来，第二代则是由谷歌接手后推出的，现在由Google接手维护。（Dagger2是Dagger1的分支，但两个框架没有严格的继承关系，亦如Struts1 和Struts2 的关系！）

#### 那么,什么是依赖注入?
依赖注入是面向对象编程的一种设计模式，其目的是为了降低程序耦合，这个耦合就是类之间的依赖引起的.

举个栗子:
```
    public class ClassA{
        private ClassB b
        public ClassA(ClassB b){
        this.b = b    }
    }
```
这里ClassA的构造函数里传了一个参数ClassB,随着后续业务增加也许又需要传入ClassC,ClassD.试想一下如果一个工程中有5个文件使用了ClassA那是不是要改5个文件?

这既不符合开闭原则, 也太不软工了.这个时候大杀器Dagger2就该出场了.
```
  public class ClassA{
     @inject 
      private ClassB b 
      public ClassA(){
       }
    }
```
通过注解的方式将ClassB b注入到ClassA中, 可以灵活配置ClassA的属性而不影响其他文件对ClassA的使用.

#### 那就有人问了，为什么要用Dagger2？ 
回答：解耦(DI的特性)，易于测试（DI的特性），高效（不使用反射，google官方说名比Dagger快13%），易混淆（apt方式生成代码，混淆后依然正常使用）

#### 如何使用Dagger2
#### 环境配置
这里以Gradle配置为例子，实用得是AndroidStudio3.2：
当AndroidStudio升级到3.0后，同时也更新了gradle到4.1后，需要 去除 掉project的build.gradle配置（本文都是kotlin写法）
```
//classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
```
app module 的 build.gradle里的
```
//apply plugin: 'com.neenbedankt.android-apt'
apply plugin: 'kotlin-kapt'
```

打开app module 的 build.gradle ，添加
```
apply plugin: 'com.android.application'
apply plugin: 'kotlin-android'
apply plugin: 'kotlin-kapt'
//...
dependencies {
    //...
    implementation 'com.google.dagger:dagger:2.16'
    //annotationProcessor 'com.google.dagger:dagger-compiler:2.16'
    kapt 'com.google.dagger:dagger-compiler:2.16'
}
```
#### Dagger2常用的注解:
> 1. @Inject
> 2. @Module, @Provides
> 3. @Component
> 4. @Singleton
> 5. @Named, @Qualifier
> 6. Lazy, Provider
> 7. @Scope

#### 示例
下面我们来看一个示例，实用Dagger2到底是怎么依赖注入的。

现在有一个Person类，然后MainActivity中又一个成员变量person。
```
class Person {
    constructor() {
        Log.i("dagger2: ", "a person created")
    }
}
```
```
class MainActivity : AppCompatActivity() {
    var person: Person? = null
    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
            person = Person()
    }
}
```
如果不适用依赖注入，那么我们只能在MainActivity中自己new一个Person对象，然后使用。

使用依赖注入：
```
class MainActivity : AppCompatActivity() {
    @Inject
    @JvmField
    var person: Person? = null

    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
    }
}
```
kotlin写法需要添加@JvmField，或者使用关键字 lateinit 修饰，不然会报错：
```
Dagger does not support injection into private fields
    private com.example.ghp.dagger2testdemo.Person person;
```
那么问题来了，就一个@Inject 注解，系统就会自动给我创建一个对象? 当然不是，这个时候我们需要一个Person类的提供者Module
```
@Module
class MainModule {
    @Provides
    fun providesPerson(): Person {
        Log.i("dagger2: ","a person created from MainModule")
        return Person()
    }
}
```
里面两个注解，@Module 和 @Provides，Module标注的对象，你可以把它想象成一个工厂，可以向外提供一些类的对象。
同时需要引入component容器。
可以把它想成一个容器， module中产出的东西都放在里面，然后将component与我要注入的MainActivity做关联，MainActivity中需要的person就可以冲 component中去去取出来。
```
@Component(modules = [(MainModule::class)])
interface MainComponent {
    fun inject(mainActivity: MainActivity)//表示怎么和要注入的类关联
}
```
看到一个新注入 @Component 表示这个接口是一个容器，并且与 MainModule.class 关联，它生产的东西都在这里。

然后在MainActivity中将component 关联进去：
```
override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var component: MainComponent  = DaggerMainComponent.builder()
                .mainModule(MainModule()).build()
        component.inject(this)
    }
```

然后运行项目，查看log：
```
"a person created from MainModule"
"a person created"
```
说明创建了对象，并且注入到MainActivity中。
上面有一个DaggerMainComponent，是在build的过程中,APT(就是dagger-compiler)扫描到注解(@Component@Module)生成的具体的component类（命名方式是Dagger+类名）.这个过程用下面这张图表示:
![dagger build](https://upload-images.jianshu.io/upload_images/1728829-05df588a880dad3d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


#### 单例模式 @Singleton（基于Component）

上面的MainActivity代码不变，我们再在MainActivity中添加一个 @Inject @JvmField  var person: Person? = null，并打印两个 person对象，结果如下：
```
"a person created from MainModule"
"a person created"
"a person created from MainModule"
"a person created"
```
发现person会被创建两次，并且两个person对象也不同，如果我们希望只有一个 person 和 person2 都指向同一个Person对象了， 使用 @Singleton 注解

两个地方需要添加：
```
@Module
class MainModule {
    @Singleton
    @Provides
    fun providesPerson(): Person {
        Log.i("dagger2: ","a person created from MainModule")
        return Person()
    }
}

@Singleton
@Component(modules = [(MainModule::class)])
interface MainComponent {
    fun inject(mainActivity: MainActivity)
}
```
再运行，发现只创建了一次，并且两个person指向同一个对象。

> 需要非常注意的是：
单例是基于Component的，所以不仅 Provides 的地方要加 @Singleton，Component上也需要加。并且如果有另外一个OtherActivity，并且创建了一个MainComponent，也注入Person，这个时候 MainActivity和OtherActivity中的Person是不构成单例的，因为它们的Component是不同的。

#### 带有参数的依赖对象
如果构造Person类，需要一个参数Context，我们怎么注入呢？ 要知道注入的时候我们只有一个 @Inject 注解，并不能带参数。所以我们需要再 MainModule 中提供context，并且由 providesXXX 函数自己去构造。如：
```
class Person {
    var context: Context? = null
    constructor(context: Context) {
        this.context = context
        Log.i("dagger2: ", "a person created with context")
    }
}

@Module
class MainModule {
    var context: Context
    constructor(context: Context){
        this.context = context
    }

    @Provides
    fun providersContext(): Context {
        return this.context
    }
    
    @Singleton
    @Provides
    fun providesPersonWithContext(context: Context): Person {
        Log.i("dagger2: ","a person created from WithContext")
        return Person(context)
    }
}
```
> 这里需要强调的是， providesPerson(Context context)中的 context，不能直接使用 成员变量 this.context，而是要在本类中提供一个 Context providesContext() 的 @Provides 方法，这样在发现需要 context 的时候会调用 provideContext 来获取，这也是为了解耦。

#### 依赖一个组件
如果组件之间有依赖，比如 Activity 依赖 Application一样，Application中的东西，Activity要直接可以注入，怎么实现呢？

例如，现在由 AppModule 提供Context对象， ActivityModule 自己无需提供Context对象，而只需要依赖于 AppModule，然后获取Context 对象即可。
```
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
    @Provides
    @Singleton
    fun providePerson(context: Context): Person {
        return Person(context)
    }
}

@Singleton
@Component(dependencies = [(AppComponent::class)], modules = [(ActivityModule::class)])
interface ActivityComponent {
    fun inject(mainActivity: MainActivity)
}
```

通过上面例子，我们需要注意：
1. ActivityModule 也需要创建Person时的Context对象，但是本类中却没有 providesContext() 的方法，因为它通过 ActivityComponent依赖于 AppComponent，所以可以通过 AppComponent中的 providesContext() 方法获取到Context对象。
2. AppComponent中必须提供 Context getContext(); 这样返回值是 Context 对象的方法接口，否则ActivityModule中无法获取。

使用方法：
一定要在 activityComponent中注入 appComponent 这个它依赖的组件。我们可以看到，由于AppComponent没有直接和 MainActivity发生关系，所以它没有 void inject(...);这样的接口

```
    var appComponent: AppComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    var activityComponent: ActivityComponent = DaggerActivityComponent.builder()
            .appComponent(appComponent)
            .activityModule(ActivityModule())
            .build()
    activityComponent.inject(this)
```

#### 自定义标记 @Qualifier 和 ＠Named
如果Person中有两个构造方法，那么在依赖注入的时候，它怎么知道我该调用哪个构造方法呢？

修改Person类，两个不同的构造方法
```
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
```
有两种方法可以解决这个问题：

#### @Named(“…”)和@Qualifier自定义标签
使用@Named 会使用到 字符串 ，如果两边都必须写对才能成功，并且字符串总是不那么优雅的，容易出错，所以我们可以自定义标签来解决上面的问题。
下面是2者的区别使用，@Named的使用同步注释
```
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PersonWithContext

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PersonWithName
```
```
@Module
class MainModule {
    var context: Context
    constructor(context: Context){
        this.context = context
    }

    @Provides
    fun providersContext(): Context {
        return this.context
    }
//    @Named("context")
    @PersonWithContext
    @Singleton
    @Provides
    fun providesPersonWithContext(context: Context): Person {
        Log.i("dagger2: ","a person created from WithContext")
        return Person(context)
    }

//   @Named("string")
    @PersonWithName
    @Singleton
    @Provides
    fun providersPersonWithName(): Person {
        Log.i("dagger2: ","a person created from WithName")
        return Person("ghp")
    }
}
```
分别在两个提供Person的provides方法上添加 @Named标签或者自定义标签，并指定。

然后在要依赖注入的地方，同样添加 @Name 或自定义标注表示要注入时使用哪一种
```
 //   @field:Named("context")
    @field:PersonWithContext
    @Inject
    @JvmField
    var person: Person? = null

//    @field:Named("string")
    @field:PersonWithName
    @Inject
    @JvmField
    var person1: Person? = null
```
从上面代码可以看出，在使用标签时@field:，不然会报错：
```
cannot be provided without an @Inject constructor or from an @Provides- or @Produces-annotated method
```
> 变量编译为 Java 字节码的时候会对应三个目标元素，一个是变量本身、还有 getter 和 setter，Kotlin 不知道这个变量的注解应该使用到那个目标上。
要解决这个方式，需要使用 Kotlin 提供的注解目标关键字来告诉 Kotlin 所注解的目标是那个，上面示例中需要注解应用到 变量上，所以使用 field 关键字

#### 懒加载Lazy和强制重新加载Provider
在注入时分别使用 Lazy 和 Provider 修饰要注入的对象：
```
    @Inject
    var lazyPerson: Lazy<Person>? = null

     @Inject
    var providerPerson: Provider<Person>? = null
```
在使用的地方：
```
        var person: Person? = lazyPerson?.value
        var person2: Person? = providerPerson?.get()
```
>  lazyPerson 多次get 的是同一个对象，
providerPerson多次get，每次get都会尝试创建新的对象。

#### @Scope 自定义生命周期
通过前面的例子，我们遇到了 @Singleton 这个标签，它可以保证在同一个Component中，一个对象是单例对象。其实可以跟进去看代码：
Singleton.java
```
@Scope
@Documented
@Retention(RUNTIME)
public @interface Singleton {}
```
利用单例和组件间依赖的关系，我们也可以定义生命周期来满足我们的需求呢，比如Activity 这样的生命周期
```
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ActivityScope
```
除了名字，其他都和 @Singleton 是一样的。
然后用ActivityScope 修饰 ActivityModule和ActivityComponent
```
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
```

参考：  
* [Dagger2入门](https://www.jianshu.com/p/92f793e76654)  
* [深入浅出Dagger2 : 从入门到爱不释手](https://www.jianshu.com/p/626b2087e2b1)  
* [Kotlin+Dagger2开发android应用需要注意的那些问题](https://www.jianshu.com/p/59f152dbec88)  
