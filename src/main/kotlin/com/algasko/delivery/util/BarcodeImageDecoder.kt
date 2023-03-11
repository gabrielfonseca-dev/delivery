package com.algasko.delivery.util

import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.Reader
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.io.IOException
import java.io.InputStream
import javax.imageio.ImageIO

class BarcodeImageDecoder {
    @Throws(BarcodeDecodingException::class)
    fun decodeImage(inputStream: InputStream?): BarcodeInfo {
        return try {
            var bitmap = BinaryBitmap(
                HybridBinarizer(
                    BufferedImageLuminanceSource(ImageIO.read(inputStream))
                )
            )
            if (bitmap.width < bitmap.height) {
                if (bitmap.isRotateSupported) {
                    bitmap = bitmap.rotateCounterClockwise()
                }
            }
            decode(bitmap)
        } catch (e: IOException) {
            throw BarcodeDecodingException(e)
        }
    }

    @Throws(BarcodeDecodingException::class)
    private fun decode(bitmap: BinaryBitmap): BarcodeInfo {
        val reader: Reader = MultiFormatReader()
        return try {
            val result = reader.decode(bitmap)
            BarcodeInfo(result.text, result.barcodeFormat.toString())
        } catch (e: Exception) {
            throw BarcodeDecodingException(e)
        }
    }

    class BarcodeInfo internal constructor(
        val text: String,
        val format: String
    )

    class BarcodeDecodingException internal constructor(cause: Throwable?) : Exception(cause)
}