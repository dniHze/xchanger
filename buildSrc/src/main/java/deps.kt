private object Versions {
    // Language
    const val kotlin = "1.3.61"

    const val appCompat = "1.1.0"
    const val ktxCore = "1.2.0"
    const val emoji = "1.0.0"
    const val recyclerView = "1.1.0"
    const val lifecycle = "2.2.0"
    const val material = "1.1.0"
    const val insetter = "0.2.1"

    const val retrofit = "2.7.1"
    const val okHttp = "4.4.0"
    const val rxJava2 = "2.2.17"
    const val rxAndroid2 = "2.1.1"
    const val dagger2 = "2.26"
    const val moshi = "1.9.2"
    const val room = "2.2.3"
    const val rxRedux = "1.0.1"
    const val rxRelay = "2.1.1"
    const val cicerone = "5.1.0"
    // Utils
    const val timber = "4.7.1"
    const val stetho = "1.5.1"
    const val leakcanary = "2.2"

    // Test libs
    const val junit = "4.12"
    const val androixJunit = "1.1.1"
    const val espressoCore = "3.2.0"
    const val mockk = "1.9.3"
}

object Dependencies {
    // Language
    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"

    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val ktxCore = "androidx.core:core-ktx:${Versions.ktxCore}"

    const val emoji = "androidx.emoji:emoji:${Versions.emoji}"
    const val emojiCompat = "androidx.emoji:emoji-appcompat:${Versions.emoji}"

    const val material = "com.google.android.material:material:${Versions.material}"

    const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"

    const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
    const val liveData = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}"
    const val lifecycleProcess = "androidx.lifecycle:lifecycle-process:${Versions.lifecycle}"
    const val lifecycleCompiler  = "androidx.lifecycle:lifecycle-compiler:${Versions.lifecycle}"

    const val insetter = "dev.chrisbanes:insetter:${Versions.insetter}"
    const val insetterKTX = "dev.chrisbanes:insetter-ktx:${Versions.insetter}"

    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val retrofitRxJavaAdapter = "com.squareup.retrofit2:adapter-rxjava2:${Versions.retrofit}"
    const val retrofitMoshiConverter = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"

    const val okHttp = "com.squareup.okhttp3:okhttp:${Versions.okHttp}"
    const val okHttpLoggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.okHttp}"

    const val stetho = "com.facebook.stetho:stetho:${Versions.stetho}"
    const val stehoInterceptor = "com.facebook.stetho:stetho-okhttp3:${Versions.stetho}"

    const val rxJava2 = "io.reactivex.rxjava2:rxjava:${Versions.rxJava2}"
    const val rxAndroid2 = "io.reactivex.rxjava2:rxandroid:${Versions.rxAndroid2}"

    const val room = "androidx.room:room-runtime:${Versions.room}"
    const val roomCompiler = "androidx.room:room-compiler:${Versions.room}"
    const val roomRxJava2 = "androidx.room:room-rxjava2:${Versions.room}"

    const val rxRedux = "com.freeletics.rxredux:rxredux:${Versions.rxRedux}"

    const val rxRelay = "com.jakewharton.rxrelay2:rxrelay:${Versions.rxRelay}"

    const val dagger2 = "com.google.dagger:dagger:${Versions.dagger2}"
    const val dagger2Compiler = "com.google.dagger:dagger-compiler:${Versions.dagger2}"

    const val moshi = "com.squareup.moshi:moshi:${Versions.moshi}"
    const val moshiKotlin = "com.squareup.moshi:moshi-kotlin:${Versions.moshi}"
    const val moshiCodegen = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshi}"

    const val cicerone = "ru.terrakok.cicerone:cicerone:${Versions.cicerone}"

    const val leakcanary = "com.squareup.leakcanary:leakcanary-android:${Versions.leakcanary}"

    const val timber = "com.jakewharton.timber:timber:${Versions.timber}"
}

object TestDependencies {
    const val junit = "junit:junit:${Versions.junit}"
    const val androidxJunit = "androidx.test.ext:junit:${Versions.androixJunit}"
    const val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espressoCore}"
    const val mockk = "io.mockk:mockk:${Versions.mockk}"
    const val mockkAndroid = "io.mockk:mockk-android:${Versions.mockk}"
    const val room = "androidx.room:room-testing:${Versions.room}"
}