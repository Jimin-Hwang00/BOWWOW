package sklookie.bowwow.dto

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable


@IgnoreExtraProperties
data class Post (
    var pid: String?,
    var title: String?,
    var content: String?,
    var date: String?,
    var uid: String?,
    var views: String?,
    var image: String?
    ) : Serializable {

    constructor() : this("", "", "", "", "", "0", "")

    constructor(title: String, content: String?, date: String?, uid: String?) : this("", title, content, date, uid, "0", "") {
        this.title = title
        this.content = content
        this.date = date
        this.uid = uid
    }

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "pid" to pid,
            "title" to title,
            "content" to content,
            "date" to date,
            "uid" to uid
        )
    }

    override fun toString(): String {
        return "pid: ${pid}, title: ${title}, content: ${content}, date: ${date}, uid: ${uid}, views: ${views}, image: ${image}"
    }
}