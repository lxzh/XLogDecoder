package ledou

import java.net.URLDecoder

val String.decoded: String get() = URLDecoder.decode(this, "UTF-8")