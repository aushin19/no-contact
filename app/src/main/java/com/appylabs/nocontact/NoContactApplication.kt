package com.appylabs.nocontact

import android.app.Application
import com.appylabs.nocontact.data.NoContactDatabase
import com.appylabs.nocontact.data.NoContactRepository

class NoContactApplication : Application() {
    val database: NoContactDatabase by lazy {
        NoContactDatabase.create(this)
    }

    val repository: NoContactRepository by lazy {
        NoContactRepository(database.breakupProfileDao())
    }
}
