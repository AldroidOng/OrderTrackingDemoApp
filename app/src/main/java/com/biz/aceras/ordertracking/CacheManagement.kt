package com.biz.aceras.ordertracking

import android.content.Context
import java.io.*

/**
 * Created by eesern_ong on 10/4/2019.
 */
class CacheManagement(private val context: Context,  private val cacheFileName: String) {

    // Write to internal cache file
    fun writeDataToCache(data: String) {
        val file = File(context.cacheDir, cacheFileName)
        writeDataToFile(file, data)
    }

    // Read from cache file.
    fun readDataFromCache(): String {
        try {
            val cacheFileDir = File(context.cacheDir, cacheFileName)

            val fileInputStream = FileInputStream(cacheFileDir)

            val fileData = readFromFileInputStream(fileInputStream)

            if (fileData.isNotEmpty()) {
                return fileData
            } else {
                return ""
//                return error("No Data in Cache")
            }
        } catch (ex: FileNotFoundException) {

            return ""
        }
    }

    // Delete cache file
    fun deleteCache(): String {
        try {
            val cacheFileDir = File(context.cacheDir, cacheFileName)
            cacheFileDir.delete()
            return "Successfully deleted cache file"
        } catch (ex: FileNotFoundException) {
//            return error("Cache file not found")
            return ""
        }
    }

    // This method will write data to file.
    private fun writeDataToFile(file: File, data: String) {
        try {
            val fileOutputStream = FileOutputStream(file)
            this.writeDataToFile(fileOutputStream, data)
            fileOutputStream.close()
        } catch (ex: FileNotFoundException) {

        } catch (ex: IOException) {

        }

    }

    // This method will write data to FileOutputStream.
    private fun writeDataToFile(fileOutputStream: FileOutputStream, data: String) {
        try {
            val outputStreamWriter = OutputStreamWriter(fileOutputStream)
            val bufferedWriter = BufferedWriter(outputStreamWriter)

            bufferedWriter.write(data)

            bufferedWriter.flush()
            bufferedWriter.close()
            outputStreamWriter.close()
        } catch (ex: FileNotFoundException) {

        } catch (ex: IOException) {

        }

    }

    // This method will read data from FileInputStream.
    private fun readFromFileInputStream(fileInputStream: FileInputStream?): String {
        val retBuf = StringBuffer()

        try {
            if (fileInputStream != null) {
                val inputStreamReader = InputStreamReader(fileInputStream)
                val bufferedReader = BufferedReader(inputStreamReader)

                var lineData = bufferedReader.readLine()
                while (lineData != null) {
                    retBuf.append(lineData)
                    lineData = bufferedReader.readLine()
                }
            }
        } catch (ex: IOException) {

        } finally {
            return retBuf.toString()
        }
    }
}