package wrteam.ecart.shop.helper



enum class AppEnvironment {
    SANDBOX {
        override fun merchantKey(): String {
            return Constant.MERCHANT_KEY
        }

        override fun merchantID(): String {
            return Constant.MERCHANT_ID
        }

        override fun furl(): String {
            return "https://www.payumoney.com/mobileapp/payumoney/failure.php"
        }

        override fun surl(): String {
            return "https://www.payumoney.com/mobileapp/payumoney/success.php"
        }

        override fun salt(): String {
            return Constant.MERCHANT_SALT
        }

        override fun debug(): Boolean {
            return true
        }
    },
    PRODUCTION {
        override fun merchantKey(): String {
            return Constant.MERCHANT_KEY
        }

        override fun merchantID(): String {
            return Constant.MERCHANT_ID
        }

        override fun furl(): String {
            return "https://www.payumoney.com/mobileapp/payumoney/failure.php"
        }

        override fun surl(): String {
            return "https://www.payumoney.com/mobileapp/payumoney/success.php"
        }

        override fun salt(): String {
            return Constant.MERCHANT_SALT
        }

        override fun debug(): Boolean {
            return false
        }
    };

    abstract fun merchantKey(): String
    abstract fun merchantID(): String
    abstract fun furl(): String
    abstract fun surl(): String
    abstract fun salt(): String
    abstract fun debug(): Boolean
}