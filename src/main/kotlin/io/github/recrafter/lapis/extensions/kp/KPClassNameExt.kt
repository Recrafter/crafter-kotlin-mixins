package io.github.recrafter.lapis.extensions.kp

import io.github.recrafter.lapis.extensions.jp.JPBoolean
import io.github.recrafter.lapis.extensions.jp.JPByte
import io.github.recrafter.lapis.extensions.jp.JPChar
import io.github.recrafter.lapis.extensions.jp.JPDouble
import io.github.recrafter.lapis.extensions.jp.JPFloat
import io.github.recrafter.lapis.extensions.jp.JPInt
import io.github.recrafter.lapis.extensions.jp.JPList
import io.github.recrafter.lapis.extensions.jp.JPLong
import io.github.recrafter.lapis.extensions.jp.JPMap
import io.github.recrafter.lapis.extensions.jp.JPObject
import io.github.recrafter.lapis.extensions.jp.JPSet
import io.github.recrafter.lapis.extensions.jp.JPShort
import io.github.recrafter.lapis.extensions.jp.JPString
import io.github.recrafter.lapis.extensions.jp.JPTypeName
import io.github.recrafter.lapis.extensions.jp.JPVoid
import io.github.recrafter.lapis.extensions.jp.boxIfPrimitive
import io.github.recrafter.lapis.kj.KJClassName

fun KPClassName.asKJClassName(): KJClassName =
    KJClassName(packageName, *simpleNames.toTypedArray())

fun KPClassName.toJavaType(shouldBox: Boolean): JPTypeName =
    when (copy(nullable = false)) {
        KPAny -> JPObject
        KPUnit -> JPVoid.boxIfPrimitive(shouldBox || isNullable)
        KPBoolean -> JPBoolean.boxIfPrimitive(shouldBox || isNullable)
        KPByte -> JPByte.boxIfPrimitive(shouldBox || isNullable)
        KPShort -> JPShort.boxIfPrimitive(shouldBox || isNullable)
        KPInt -> JPInt.boxIfPrimitive(shouldBox || isNullable)
        KPLong -> JPLong.boxIfPrimitive(shouldBox || isNullable)
        KPChar -> JPChar.boxIfPrimitive(shouldBox || isNullable)
        KPFloat -> JPFloat.boxIfPrimitive(shouldBox || isNullable)
        KPDouble -> JPDouble.boxIfPrimitive(shouldBox || isNullable)
        KPString -> JPString
        KPList -> JPList
        KPSet -> JPSet
        KPMap -> JPMap
        else -> asKJClassName().javaVersion
    }
