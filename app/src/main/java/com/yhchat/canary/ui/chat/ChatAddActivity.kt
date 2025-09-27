package com.yhchat.canary.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.yhchat.canary.data.di.RepositoryFactory
import com.yhchat.canary.data.model.ChatAddInfo
import com.yhchat.canary.data.model.ChatAddType
import com.yhchat.canary.ui.theme.YhchatCanaryTheme
import com.yhchat.canary.utils.ChatAddLinkHandler
import dagger.hilt.android.AndroidEntryPoint

/**
 * 聊天添加Activity - 处理 yunhu://chat-add 链接
 */
@AndroidEntryPoint
class ChatAddActivity : ComponentActivity() {
    
    companion object {
        const val EXTRA_LINK = "extra_link"
        
        fun start(context: Context, link: String) {
            val intent = Intent(context, ChatAddActivity::class.java).apply {
                putExtra(EXTRA_LINK, link)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        try {
            val link = intent.getStringExtra(EXTRA_LINK) ?: intent.data?.toString()
            
            if (link == null) {
                finish()
                return
            }
            
            if (!ChatAddLinkHandler.isChatAddLink(link)) {
                finish()
                return
            }
            
            val chatAddInfo = ChatAddLinkHandler.parseChatAddLink(link)
            if (chatAddInfo == null) {
                finish()
                return
            }
            
            setContent {
                YhchatCanaryTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val viewModel: ChatAddViewModel = hiltViewModel()
                        
                        ChatAddScreen(
                            chatAddInfo = chatAddInfo,
                            viewModel = viewModel,
                            onDismiss = { finish() }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // 防止崩溃，记录错误日志
            android.util.Log.e("ChatAddActivity", "Deep Link 解析失败", e)
            finish()
        }
    }
}

/**
 * 聊天添加界面
 */
@Composable
fun ChatAddScreen(
    chatAddInfo: ChatAddInfo,
    viewModel: ChatAddViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 加载数据
    LaunchedEffect(chatAddInfo) {
        viewModel.loadChatInfo(chatAddInfo)
    }
    
    // 成功后自动关闭
    LaunchedEffect(uiState.isAddSuccess) {
        if (uiState.isAddSuccess) {
            kotlinx.coroutines.delay(2000)
            onDismiss()
        }
    }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (chatAddInfo.type) {
                            ChatAddType.USER -> "用户详情"
                            ChatAddType.GROUP -> "群聊详情"
                            ChatAddType.BOT -> "机器人详情"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                when {
                    uiState.isLoading -> {
                        // 加载状态
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    
                    uiState.error != null -> {
                        // 错误状态
                        val errorMessage = uiState.error
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "加载失败",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage ?: "未知错误",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(onClick = { viewModel.loadChatInfo(chatAddInfo) }) {
                                Text("重试")
                            }
                        }
                    }
                    
                    uiState.chatInfo != null -> {
                        // 详情内容
                        val chatInfo = uiState.chatInfo!! // 解决委托属性智能转换问题
                        ChatDetailContent(
                            chatInfo = chatInfo,
                            addState = uiState,
                            onAddClick = { viewModel.addChat() }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 聊天详情内容
 */
@Composable
private fun ChatDetailContent(
    chatInfo: ChatAddInfo,
    addState: ChatAddUiState,
    onAddClick: () -> Unit
) {
    Column {
        // 头像和基本信息
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = chatInfo.avatarUrl,
                contentDescription = chatInfo.displayName,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = chatInfo.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "ID: ${chatInfo.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (chatInfo.additionalInfo.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = chatInfo.additionalInfo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 描述信息
        if (chatInfo.description.isNotEmpty()) {
            Text(
                text = when (chatInfo.type) {
                    ChatAddType.USER -> "个人简介"
                    ChatAddType.GROUP -> "群聊简介"
                    ChatAddType.BOT -> "机器人简介"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = chatInfo.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 错误提示
        addState.addError?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // 成功提示
        if (addState.isAddSuccess) {
            Text(
                text = when (chatInfo.type) {
                    ChatAddType.USER -> "好友申请已发送"
                    ChatAddType.GROUP -> "群聊申请已发送"
                    ChatAddType.BOT -> "机器人添加成功"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // 操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { /* TODO: 取消 */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("取消")
            }
            
            Button(
                onClick = onAddClick,
                modifier = Modifier.weight(1f),
                enabled = !addState.isAdding && !addState.isAddSuccess
            ) {
                if (addState.isAdding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = when {
                        addState.isAdding -> "添加中..."
                        addState.isAddSuccess -> "已添加"
                        else -> when (chatInfo.type) {
                            ChatAddType.USER -> "添加好友"
                            ChatAddType.GROUP -> "加入群聊"
                            ChatAddType.BOT -> "添加机器人"
                        }
                    }
                )
            }
        }
    }
}