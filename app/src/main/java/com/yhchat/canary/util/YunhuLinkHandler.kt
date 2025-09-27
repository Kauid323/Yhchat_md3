package com.yhchat.canary.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.yhchat.canary.ui.community.PostDetailActivity
import java.util.regex.Pattern

/**
 * 云湖链接处理工具类
 */
object YunhuLinkHandler {
    
    // yunhu:// 链接的正则表达式
    private val YUNHU_LINK_PATTERN = Pattern.compile("yunhu://post-detail\\?id=(\\d+)")
    
    /**
     * 解析yunhu链接并跳转到相应页面
     */
    fun handleYunhuLink(context: Context, link: String): Boolean {
        val matcher = YUNHU_LINK_PATTERN.matcher(link)
        if (matcher.find()) {
            val postId = matcher.group(1)?.toIntOrNull()
            if (postId != null) {
                // 跳转到文章详情页
                val intent = Intent(context, PostDetailActivity::class.java).apply {
                    putExtra("post_id", postId)
                    putExtra("post_title", "文章详情")
                    putExtra("token", "") // 可能需要从其他地方获取token
                }
                context.startActivity(intent)
                return true
            }
        }
        return false
    }
    
    /**
     * 在文本中识别yunhu链接并设置为可点击
     */
    fun makeLinksClickable(
        textView: TextView, 
        text: String, 
        linkColor: Int = android.graphics.Color.BLUE
    ) {
        val spannable = SpannableStringBuilder(text)
        val matcher = YUNHU_LINK_PATTERN.matcher(text)
        
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            val link = matcher.group()
            
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    handleYunhuLink(widget.context, link)
                }
            }
            
            spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(
                ForegroundColorSpan(linkColor), 
                start, 
                end, 
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        
        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance()
    }
    
    /**
     * 检查文本是否包含yunhu链接
     */
    fun containsYunhuLink(text: String): Boolean {
        return YUNHU_LINK_PATTERN.matcher(text).find()
    }
    
    /**
     * 提取文本中的所有yunhu链接
     */
    fun extractYunhuLinks(text: String): List<String> {
        val links = mutableListOf<String>()
        val matcher = YUNHU_LINK_PATTERN.matcher(text)
        
        while (matcher.find()) {
            links.add(matcher.group())
        }
        
        return links
    }
    
    /**
     * 从yunhu链接中提取文章ID
     */
    fun extractPostIdFromLink(link: String): Int? {
        val matcher = YUNHU_LINK_PATTERN.matcher(link)
        return if (matcher.find()) {
            matcher.group(1)?.toIntOrNull()
        } else {
            null
        }
    }
    
    /**
     * 生成yunhu链接
     */
    fun generateYunhuLink(postId: Int): String {
        return "yunhu://post-detail?id=$postId"
    }
    
    /**
     * 处理Intent中的yunhu链接
     */
    fun handleIntentData(context: Context, intent: Intent): Boolean {
        val data = intent.data
        if (data != null && data.scheme == "yunhu") {
            val link = data.toString()
            return handleYunhuLink(context, link)
        }
        return false
    }
}

/**
 * Compose版本的链接处理函数
 */
@Composable
fun ClickableLinkText(
    text: String,
    linkColor: Color = MaterialTheme.colorScheme.primary,
    onLinkClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    
    // 检查是否包含yunhu链接
    if (YunhuLinkHandler.containsYunhuLink(text)) {
        // 对于包含链接的文本，可以使用AndroidView来实现
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { context ->
                TextView(context).apply {
                    YunhuLinkHandler.makeLinksClickable(
                        this, 
                        text, 
                        linkColor.toArgb()
                    )
                }
            },
            update = { textView ->
                YunhuLinkHandler.makeLinksClickable(
                    textView, 
                    text, 
                    linkColor.toArgb()
                )
            }
        )
    } else {
        // 普通文本
        androidx.compose.material3.Text(text = text)
    }
}
