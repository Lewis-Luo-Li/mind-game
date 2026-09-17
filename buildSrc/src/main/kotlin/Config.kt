object Config {
    const val COMPILE_SDK = 36
    const val MIN_SDK = 36
    const val VERSION_CODE = 1
    const val VERSION_NAME = "1.0"
    const val APPLICATION_ID = "com.idroid.stuido.mind_game"

    const val BASE_NAMESPACE = "com.idroid.stuido.mind_game"

    object Core {
        object Ui {
            const val MODULE_ID = "core.ui"
            const val PACKAGE_NAME = "$BASE_NAMESPACE.$MODULE_ID"
        }
        object Logic {
            const val MODULE_ID = "core.logic"
            const val PACKAGE_NAME = "$BASE_NAMESPACE.$MODULE_ID"
        }
    }

    object Feature {
        object Game {
            const val MODULE_ID = "feature.game"
            const val PACKAGE_NAME = "$BASE_NAMESPACE.$MODULE_ID"
        }
    }
}