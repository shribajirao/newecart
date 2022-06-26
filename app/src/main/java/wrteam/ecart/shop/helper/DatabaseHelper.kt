@file:Suppress("FunctionName", "FunctionName", "FunctionName", "FunctionName", "FunctionName",
    "FunctionName", "FunctionName", "FunctionName", "FunctionName", "FunctionName", "FunctionName"
)

package wrteam.ecart.shop.helper

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private val tableName = "tableCart"
    private val productId = "pid"
    private val variantId = "vid"
    private val quantity = "qty"
    private val favoriteTableInfo = "$TABLE_FAVORITE_NAME($KEY_ID TEXT)"
    private val saveForLaterTableInfo =
        "$TABLE_SAVE_FOR_LATER_NAME($variantId TEXT ,$productId TEXT ,$quantity TEXT)"
    private val cartTableInfo = "$tableName($variantId TEXT ,$productId TEXT ,$quantity TEXT)"
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $favoriteTableInfo")
        db.execSQL("CREATE TABLE $cartTableInfo")
        db.execSQL("CREATE TABLE $saveForLaterTableInfo")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        replaceDataToNewTable(db, TABLE_FAVORITE_NAME, favoriteTableInfo)
        replaceDataToNewTable(db, tableName, cartTableInfo)
        replaceDataToNewTable(db, TABLE_SAVE_FOR_LATER_NAME, saveForLaterTableInfo)
        onCreate(db)
    }

    private fun replaceDataToNewTable(db: SQLiteDatabase, tableName: String, tableString: String) {
        db.execSQL("CREATE TABLE IF NOT EXISTS $tableString")
        val columns = getColumns(db, tableName)
        db.execSQL("ALTER TABLE $tableName RENAME TO temp_$tableName")
        db.execSQL("CREATE TABLE $tableString")
        columns!!.retainAll(getColumns(db, tableName)!!)
        val cols = join(columns)
        db.execSQL(
            String.format(
                "INSERT INTO %s (%s) SELECT %s from temp_%s",
                tableName, cols, cols, tableName
            )
        )
        db.execSQL("DROP TABLE temp_$tableName")
    }

    private fun getColumns(db: SQLiteDatabase, tableName: String): MutableList<String>? {
        var ar: MutableList<String>? = null
        try {
            db.rawQuery("SELECT * FROM $tableName LIMIT 1", null).use { c ->
                if (c != null) {
                    ar = ArrayList(listOf(*c.columnNames))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ar
    }

    private fun join(list: List<String>?): String {
        val buf = StringBuilder()
        val num = list!!.size
        for (i in 0 until num) {
            if (i != 0) buf.append(",")
            buf.append(list[i])
        }
        return buf.toString()
    }

    /*      FAVORITE TABLE OPERATION      */
    fun getFavoriteById(pid: String): Boolean {
        var count = false
        val db = this.writableDatabase
        val args = arrayOf(pid)
        val cursor = db.rawQuery(
            "SELECT $KEY_ID FROM $TABLE_FAVORITE_NAME WHERE $KEY_ID=? ",
            args
        )
        if (cursor.moveToFirst()) {
            count = true
        }
        cursor.close()
        db.close()
        return count
    }

    fun addOrRemoveFavorite(id: String, isAdd: Boolean) {
        val db = this.writableDatabase
        if (isAdd) {
            addFavorite(id)
        } else {
            db.execSQL("DELETE FROM  $TABLE_FAVORITE_NAME WHERE $KEY_ID = $id")
        }
        db.close()
    }

    private fun addFavorite(id: String?) {
        val fav = ContentValues()
        fav.put(KEY_ID, id)
        val db = this.writableDatabase
        db.insert(TABLE_FAVORITE_NAME, null, fav)
    }

    val favorite: ArrayList<String>
        get() {
            val ids = ArrayList<String>()
            val selectQuery = "SELECT *  FROM $TABLE_FAVORITE_NAME"
            val db = this.writableDatabase
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    ids.add(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)))
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()
            return ids
        }

    fun deleteAllFavoriteData() {
        val database = this.writableDatabase
        database.execSQL("DELETE FROM $TABLE_FAVORITE_NAME")
        database.close()
    }

    /*      CART TABLE OPERATION      */
    val cartList: ArrayList<String>
        get() {
            val ids = ArrayList<String>()
            val selectQuery = "SELECT *  FROM $tableName"
            val db = this.writableDatabase
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val count = cursor.getString(cursor.getColumnIndex(quantity).toString().toInt())
                    if (count == "0") {
                        db.execSQL(
                            "DELETE FROM $tableName WHERE $variantId = ? AND $productId = ?",
                            arrayOf(
                                cursor.getString(cursor.getColumnIndexOrThrow(variantId)),
                                cursor.getString(cursor.getColumnIndexOrThrow(productId))
                            )
                        )
                    } else ids.add(cursor.getString(cursor.getColumnIndexOrThrow(variantId)))
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()
            return ids
        }
    val cartData: HashMap<String, String>
        get() {
            val ids = HashMap<String, String>()
            val selectQuery = "SELECT *  FROM $tableName"
            val db = this.writableDatabase
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val count = cursor.getString(cursor.getColumnIndex(quantity).toString().toInt())
                    if (count == "0") {
                        db.execSQL(
                            "DELETE FROM $tableName WHERE $variantId = ? AND $productId = ?",
                            arrayOf(
                                cursor.getString(cursor.getColumnIndexOrThrow(variantId)),
                                cursor.getString(cursor.getColumnIndexOrThrow(productId))
                            )
                        )
                    } else ids[cursor.getString(cursor.getColumnIndexOrThrow(variantId))] =
                        cursor.getString(cursor.getColumnIndexOrThrow(quantity))
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()
            return ids
        }

    fun getTotalItemOfCart(activity: Activity): Int {
        val countQuery = "SELECT  * FROM $tableName"
        val db = this.readableDatabase
        val cursor = db.rawQuery(countQuery, null)
        val count = cursor.count
        cursor.close()
        Constant.TOTAL_CART_ITEM = count
        activity.invalidateOptionsMenu()
        return count
    }

    fun addToCart(vid: String?, pid: String?, qty: String) {
        try {
            if (!checkCartItemExist(vid, pid).equals("0", ignoreCase = true)) {
                updateCart(vid, pid, qty)
            } else {
                val db = this.writableDatabase
                val values = ContentValues()
                values.put(variantId, vid)
                values.put(productId, pid)
                values.put(quantity, qty)
                db.insert(tableName, null, values)
                db.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateCart(vid: String?, pid: String?, qty: String) {
        val db = this.writableDatabase
        if (qty == "0") {
            removeFromCart(vid, pid)
        } else {
            val values = ContentValues()
            values.put(quantity, qty)
            db.update(tableName, values, "$variantId = ? AND $productId = ?", arrayOf(vid, pid))
        }
        db.close()
    }

    fun removeFromCart(vid: String?, pid: String?) {
        val database = this.writableDatabase
        database.execSQL(
            "DELETE FROM $tableName WHERE $variantId = ? AND $productId = ?",
            arrayOf(vid, pid)
        )
        database.close()
    }

    fun checkCartItemExist(vid: String?, pid: String?): String {
        var count = "0"
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $tableName WHERE $variantId = ? AND $productId = ?",
            arrayOf(vid, pid)
        )
        if (cursor.moveToFirst()) {
            count = cursor.getString(cursor.getColumnIndex(quantity).toString().toInt())
            if (count == "0") {
                db.execSQL(
                    "DELETE FROM $tableName WHERE $variantId = ? AND $productId = ?",
                    arrayOf(vid, pid)
                )
            }
        }
        cursor.close()
        db.close()
        return count
    }

    fun clearCart() {
        val database = this.writableDatabase
        database.execSQL("DELETE FROM $tableName")
        database.close()
    }

    /*      SAVE FOR LATER TABLE OPERATION      */
    val saveForLaterList: ArrayList<String>
        get() {
            val ids = ArrayList<String>()
            val selectQuery = "SELECT *  FROM $TABLE_SAVE_FOR_LATER_NAME"
            val db = this.writableDatabase
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val count = cursor.getString(cursor.getColumnIndex(quantity).toString().toInt())
                    if (count == "0") {
                        db.execSQL(
                            "DELETE FROM $TABLE_SAVE_FOR_LATER_NAME WHERE $variantId = ? AND $productId = ?",
                            arrayOf(
                                cursor.getString(cursor.getColumnIndexOrThrow(variantId)),
                                cursor.getString(cursor.getColumnIndexOrThrow(productId))
                            )
                        )
                    } else ids.add(cursor.getString(cursor.getColumnIndexOrThrow(variantId)))
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()
            return ids
        }

    private fun addToSaveForLater(vid: String?, pid: String?, qty: String?) {
        try {
            val db = this.writableDatabase
            val values = ContentValues()
            values.put(variantId, vid)
            values.put(productId, pid)
            values.put(quantity, qty)
            db.insert(TABLE_SAVE_FOR_LATER_NAME, null, values)
            db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val saveForLaterData: HashMap<String, String>
        get() {
            val ids = HashMap<String, String>()
            val selectQuery = "SELECT *  FROM $TABLE_SAVE_FOR_LATER_NAME"
            val db = this.writableDatabase
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val count = cursor.getString(cursor.getColumnIndex(quantity).toString().toInt())
                    if (count == "0") {
                        db.execSQL(
                            "DELETE FROM $TABLE_SAVE_FOR_LATER_NAME WHERE $variantId = ? AND $productId = ?",
                            arrayOf(
                                cursor.getString(cursor.getColumnIndexOrThrow(variantId)),
                                cursor.getString(cursor.getColumnIndexOrThrow(productId))
                            )
                        )
                    } else ids[cursor.getString(cursor.getColumnIndexOrThrow(variantId))] =
                        cursor.getString(cursor.getColumnIndexOrThrow(quantity))
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()
            return ids
        }
    val totalItemOfSaveForLater: Int
        get() {
            val countQuery = "SELECT  * FROM $TABLE_SAVE_FOR_LATER_NAME"
            val db = this.readableDatabase
            val cursor = db.rawQuery(countQuery, null)
            val count = cursor.count
            cursor.close()
            return count
        }

    fun moveToCartOrSaveForLater(vid: String?, pid: String?, from: String, activity: Activity) {
        if (from == "cart") {
            addToSaveForLater(vid, pid, checkCartItemExist(vid, pid))
            removeFromCart(vid, pid)
        } else {
            addToCart(vid, pid, checkSaveForLaterItemExist(vid, pid))
            removeFromSaveForLater(vid, pid)
        }
        getTotalItemOfCart(activity)
    }

    fun removeFromSaveForLater(vid: String?, pid: String?) {
        val database = this.writableDatabase
        database.execSQL(
            "DELETE FROM $TABLE_SAVE_FOR_LATER_NAME WHERE $variantId = ? AND $productId = ?",
            arrayOf(vid, pid)
        )
        database.close()
    }

    private fun checkSaveForLaterItemExist(vid: String?, pid: String?): String {
        var count = "0"
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_SAVE_FOR_LATER_NAME WHERE $variantId = ? AND $productId = ?",
            arrayOf(vid, pid)
        )
        if (cursor.moveToFirst()) {
            count = cursor.getString(cursor.getColumnIndex(quantity).toString().toInt())
            if (count == "0") {
                db.execSQL(
                    "DELETE FROM $TABLE_SAVE_FOR_LATER_NAME WHERE $variantId = ? AND $productId = ?",
                    arrayOf(vid, pid)
                )
            }
        }
        cursor.close()
        db.close()
        return count
    }

    fun clearSaveForLater() {
        val database = this.writableDatabase
        database.execSQL("DELETE FROM $TABLE_SAVE_FOR_LATER_NAME")
        database.close()
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "ecart_new.db"
        const val TABLE_FAVORITE_NAME = "tableFavorite"
        const val TABLE_SAVE_FOR_LATER_NAME = "tableSaveForLater"
        const val KEY_ID = "productId"
    }
}