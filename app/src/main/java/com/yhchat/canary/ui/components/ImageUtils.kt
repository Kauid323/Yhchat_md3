package com.yhchat.canary.ui.components

import android.content.Context
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.request.ImageRequest

/**
 * 图片加载工具类
 * 支持多种图片格式包括 GIF、AVIF、WEBP、SVG 等
 */
object ImageUtils {
    
    /**
     * 创建支持多格式的ImageLoader
     */
    fun createImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                // GIF支持
                add(GifDecoder.Factory())
                // Android 9+ 支持 AVIF, WEBP 等新格式
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                }
                // SVG支持
                add(SvgDecoder.Factory())
            }
            .build()
    }
    
    /**
     * 创建表情图片请求
     * 包含必要的referer头和格式支持
     */
    fun createStickerImageRequest(
        context: Context,
        url: String,
        enableHardware: Boolean = true
    ): ImageRequest {
        return ImageRequest.Builder(context)
            .data(url)
            .setHeader("Referer", "https://myapp.jwznb.com")
            .setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36")
            .allowHardware(enableHardware)
            .crossfade(true)
            .build()
    }
    
    /**
     * 创建普通图片请求
     * 支持多种图片格式：jpg, jpeg, png, gif, webp, avif, svg
     */
    fun createImageRequest(
        context: Context,
        url: String,
        enableHardware: Boolean = true
    ): ImageRequest {
        val builder = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(enableHardware)
            .crossfade(true)
            
        // 为相关域名添加Referer头
        if (url.contains("chat-img.jwznb.com") || url.contains("jwznb.com") || url.contains("myapp.jwznb.com")) {
            builder.setHeader("Referer", "https://myapp.jwznb.com")
            builder.setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36")
        }
        
        return builder.build()
    }
    
    /**
     * 创建头像图片请求
     * 支持多种图片格式，包含referer头
     */
    fun createAvatarImageRequest(
        context: Context,
        url: String,
        enableHardware: Boolean = true
    ): ImageRequest {
        val builder = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(enableHardware)
            .crossfade(true)
            
        // 为相关域名添加Referer头
        if (url.contains("chat-img.jwznb.com") || url.contains("jwznb.com") || url.contains("myapp.jwznb.com")) {
            builder.setHeader("Referer", "https://myapp.jwznb.com")
            builder.setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36")
        }
        
        return builder.build()
    }
    
    /**
     * 创建机器人图片请求
     * 专门为机器人相关图片添加必要的referer头
     */
    fun createBotImageRequest(
        context: Context,
        url: String,
        enableHardware: Boolean = true
    ): ImageRequest {
        return ImageRequest.Builder(context)
            .data(url)
            .setHeader("Referer", "https://myapp.jwznb.com")
            .setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36")
            .allowHardware(enableHardware)
            .crossfade(true)
            .build()
    }
    
    /**
     * 创建分区相关图片请求
     * 支持.webp, .avif等格式，包含必要头信息
     */
    fun createBoardImageRequest(
        context: Context,
        url: String,
        enableHardware: Boolean = true
    ): ImageRequest {
        return ImageRequest.Builder(context)
            .data(url)
            .setHeader("Referer", "https://myapp.jwznb.com")
            .setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36")
            .allowHardware(enableHardware)
            .crossfade(true)
            .build()
    }
}
