package sklookie.bowwow.dto

import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
data class Comment (
        var pid: String?,
        var cid: String?,
        var comment: String?,
        var date: String?,
        var uid: String?,
        var uname: String?
    ) : Serializable {

    constructor() : this("", "", "", "", "", "")

//    constructor(pid: String, cid: String, comment: String, date: String?, uid: String?) : this("", "", comment, date, uid) {
//        this.comment = comment
//        this.date = date
//        this.uid = uid
//    }

    override fun toString(): String {
        return "cid: ${cid}, comment: ${comment}, date: ${date}, uid: ${uid}"
    }
}