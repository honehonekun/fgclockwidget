package com.example.fgclockwidget

import android.R.attr.contentDescription
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import android.util.TypedValue
import androidx.annotation.FontRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.core.util.TypedValueCompat
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext


@Composable
fun GlanceText(
    text: String,
    @FontRes font: Int,
    fontSize: TextUnit,
    modifier: GlanceModifier = GlanceModifier,
    color: Color = Color.Black,
    letterSpacing: TextUnit = 0.1.sp
) {
    //画像として表示
    Image(
        modifier = modifier,
        provider = ImageProvider(
            //文字->画像　 関数呼び出し
            LocalContext.current.textAsBitmap(
                text = text,
                fontSize = fontSize,
                color = color,
                font = font,
                letterSpacing = letterSpacing.value
            )
        ),
        contentDescription = null,
    )
}

private fun Context.textAsBitmap(
    text: String,
    fontSize: TextUnit,
    color: Color = Color.Black,
    letterSpacing: Float = 0.1f,
    font: Int
    //Bitmapを返す
): Bitmap {
    //テキストペイントクラスのインスタンスを用意
    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    //文字サイズをspで設定
    paint.textSize = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        fontSize.value,
        resources.displayMetrics
    )
    //色設定　左詰め、文字間隔設定　フォント設定
    paint.color = color.toArgb()
    paint.textAlign = Paint.Align.LEFT
    paint.letterSpacing = letterSpacing
    paint.typeface = ResourcesCompat.getFont(this, font)

    // 1. 文字列を改行コードで分割する
    val lines = text.split("\n")

    // 2. 1行あたりの高さを計算する
    val fontMetrics = paint.fontMetrics
    //ascentはベースラインから上　マイナスで帰ってくるので引き算で一行の高さを求める
    val lineHeight = fontMetrics.descent - fontMetrics.ascent

    // 3. 全体の最大幅と全体の高さを計算する
    var maxWidth = 0f
    //それぞれの行の幅を計測し、最も長いものを最大として設定
    for (line in lines) {
        val w = paint.measureText(line)
        if (w > maxWidth) maxWidth = w
    }
    //トータルの高さを設定　高さ*行数
    val totalHeight = lineHeight * lines.size

    // ビットマップを作成（幅と高さに少し余裕を持たせる）
    val width = (maxWidth + 10f).toInt()
    val height = (totalHeight + 10f).toInt()

    // 幅や高さが0だとクラッシュするので防止 何も中身がない場合は一マスを返す
    if (width <= 0 || height <= 0) return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    // bitmapを作成　ARGB256段階　それぞれ8ビット
    val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    //canvasを作成
    val canvas = Canvas(image)

    // 4. 1行ずつ描画する　ベースラインを設定
    var y = -fontMetrics.ascent // 最初の行のベースライン位置
    //設定したpaintで高さはy、横は0fからlineを描画
    for (line in lines) {
        canvas.drawText(line, 0f, y, paint)
        //座標をずらす
        y += lineHeight // 次の行へY座標をずらす
    }
    //完成したimageを返す
    return image
}