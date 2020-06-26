package com.example.licenta

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import io.reactivex.processors.PublishProcessor
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun providesNavEvents(): PublishProcessor<NavEvent> = PublishProcessor.create()
}