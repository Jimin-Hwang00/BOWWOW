package sklookie.bowwow.dto

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class GoogleInfo(var uid: String, var familyName: String, var givenName: String) {
    constructor() : this("", "", "")
}