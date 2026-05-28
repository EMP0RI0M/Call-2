package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CallEntity
import com.example.data.CallMessage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallAssistantApp(
    viewModel: CallViewModel,
    modifier: Modifier = Modifier
) {
    val callList by viewModel.callList.collectAsStateWithLifecycle()
    val selectedCall by viewModel.selectedCall.collectAsStateWithLifecycle()
    val simulatedMessages by viewModel.simulatedMessages.collectAsStateWithLifecycle()
    val isSimulating by viewModel.isSimulating.collectAsStateWithLifecycle()
    val speakerState by viewModel.speakerState.collectAsStateWithLifecycle()
    val incomingCallState by viewModel.incomingCallState.collectAsStateWithLifecycle()

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp > 600

    val primaryTeal = Color(0xFF00796B)
    val darkTealBackground = Color(0xFF0B1716)
    val mintAccent = Color(0xFF26A69A)
    val neonTealAccent = Color(0xFF00E5FF)
    val activeRed = Color(0xFFE53935)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isSimulating) activeRed else neonTealAccent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Dynamic Call Assistant",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = "AI Agent Core • Bilingual EN / हिंदी",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.clearAllHistory() },
                        modifier = Modifier.testTag("clear_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Call History",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryTeal,
                    titleContentColor = Color.White
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(darkTealBackground, Color(0xFF10211F))
                    )
                )
        ) {
            if (isWideScreen) {
                // Dual-Pane Tablet Layout
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(0.42f)
                            .fillMaxHeight()
                            .padding(12.dp)
                    ) {
                        CallMetricsSummary(callList = callList)
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Text(
                            text = "LIVE SPEECH SIMULATIONS",
                            color = mintAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        // CallSimulationController(
                        //     onSimulateEnglish = { viewModel.triggerIncomingCall(isHindi = false) },
                        //     onSimulateHindi = { viewModel.triggerIncomingCall(isHindi = true) },
                        //     isSimulating = isSimulating
                        // )
                        // Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "RECENT TRANSCRIPT LOGS (${callList.size})",
                            color = mintAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        CallLogsList(
                            callList = callList,
                            selectedCall = selectedCall,
                            onSelectCall = { viewModel.selectCall(it) },
                            onDeleteCall = { viewModel.deleteCall(it) }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Color.White.copy(alpha = 0.1f))
                    )

                    Box(
                        modifier = Modifier
                            .weight(0.58f)
                            .fillMaxHeight()
                    ) {
                        if (selectedCall != null) {
                            CallTranscriptWindow(
                                call = selectedCall!!,
                                isSimulating = isSimulating,
                                simulatedMessages = simulatedMessages,
                                speakerState = speakerState,
                                onClose = { viewModel.selectCall(null) },
                                onCloseEnabled = false
                            )
                        } else {
                            EmptyStateLanding()
                        }
                    }
                }
            } else {
                // Stacked Mobile Layout
                AnimatedContent(
                    targetState = selectedCall,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    },
                    label = "MobileNav"
                ) { targetCall ->
                    if (targetCall != null) {
                        CallTranscriptWindow(
                            call = targetCall,
                            isSimulating = isSimulating,
                            simulatedMessages = simulatedMessages,
                            speakerState = speakerState,
                            onClose = { viewModel.selectCall(null) },
                            onCloseEnabled = true
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            CallMetricsSummary(callList = callList)
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "SIMULATE INCOMING CALL AGENT",
                                color = mintAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            // CallSimulationController(
                            //     onSimulateEnglish = { viewModel.triggerIncomingCall(isHindi = false) },
                            //     onSimulateHindi = { viewModel.triggerIncomingCall(isHindi = true) },
                            //     isSimulating = isSimulating
                            // )
                            // Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "RECENT INCOMING CALLS (${callList.size})",
                                color = mintAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            CallLogsList(
                                callList = callList,
                                selectedCall = null,
                                onSelectCall = { viewModel.selectCall(it) },
                                onDeleteCall = { viewModel.deleteCall(it) }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Download Models", color = mintAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            GemmaModelDownloadCard(
                                modelName = "Gemma-4-E2B-it",
                                modelSize = "2.6 GB",
                                onDownload = { /* handle download */ }
                            )
                        }
                    }
                }
            }

            if (incomingCallState != null) {
                IncomingCallHeadsUpDisplay(
                    call = incomingCallState!!,
                    onAccept = { isHindi -> viewModel.acceptIncomingCall(isHindi) },
                    onDecline = { viewModel.declineIncomingCall() }
                )
            }
        }
    }
}

@Composable
fun CallMetricsSummary(callList: List<CallEntity>) {
    val neonTealAccent = Color(0xFF00E5FF)
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF142422)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "CALL TRANSCRIPTION SYSTEM STATUS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${callList.size}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Total Sessions",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                
                Box(modifier = Modifier.size(1.dp, 30.dp).background(Color.White.copy(alpha = 0.1f)))

                val billingCount = callList.count { it.isBilingual }
                Column {
                    Text(
                        text = "$billingCount",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = neonTealAccent
                    )
                    Text(
                        text = "Bilingual Calls",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                Box(modifier = Modifier.size(1.dp, 30.dp).background(Color.White.copy(alpha = 0.1f)))

                Column {
                    Text(
                        text = "READY",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00E676)
                    )
                    Text(
                        text = "Local Speech AI",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}


@Composable
fun CallLogsList(
    callList: List<CallEntity>,
    selectedCall: CallEntity?,
    onSelectCall: (CallEntity) -> Unit,
    onDeleteCall: (Int) -> Unit
) {
    if (callList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "📞",
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No recorded call transcripts present.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
                Text(
                    text = "Tap Scenario A or B to trigger a simulated call!",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 12.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(callList, key = { it.id }) { call ->
                val isSelected = selectedCall?.id == call.id
                CallItemCard(
                    call = call,
                    isSelected = isSelected,
                    onClick = { onSelectCall(call) },
                    onDelete = { onDeleteCall(call.id) }
                )
            }
        }
    }
}

@Composable
fun CallItemCard(
    call: CallEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val primaryTeal = Color(0xFF00796B)
    val neonTealAccent = Color(0xFF00E5FF)
    
    val borderStroke = if (isSelected) {
        BorderStroke(1.5.dp, neonTealAccent)
    } else {
        BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    }

    val cardBg = if (isSelected) {
        Color(0xFF19322E)
    } else {
        Color(0xFF142422)
    }

    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val callTime = dateFormat.format(Date(call.timestamp))

    Card(
        shape = RoundedCornerShape(14.dp),
        border = borderStroke,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("call_item_${call.id}")
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle Badge Language Info
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (call.isBilingual) Color(0xFF3F51B5).copy(alpha = 0.2f)
                        else primaryTeal.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (call.isBilingual) "💬" else "📞",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = call.callerName,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (call.callStatus == "In Progress") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEB3B)),
                            shape = CircleShape
                        ) {
                            Text(
                                "LIVE",
                                color = Color.Black,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Text(
                    text = call.phoneNumber,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (call.isBilingual) Color(0xFFE8EAF6) else Color(0xFFE0F2F1),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = call.detectedLanguage,
                            color = if (call.isBilingual) Color(0xFF3F51B5) else primaryTeal,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "•  ${call.duration}  •  $callTime",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )
                }
            }

            IconButton(
                onClick = { onDelete() },
                modifier = Modifier.size(32.dp).testTag("delete_btn_${call.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete call log",
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun CallTranscriptWindow(
    call: CallEntity,
    isSimulating: Boolean,
    simulatedMessages: List<CallMessage>,
    speakerState: String?,
    onClose: () -> Unit,
    onCloseEnabled: Boolean
) {
    val neonTealAccent = Color(0xFF00E5FF)
    val listState = rememberLazyListState()

    val currentMessages = if (isSimulating && call.callStatus == "In Progress") {
        simulatedMessages
    } else {
        call.messages
    }

    LaunchedEffect(currentMessages.size, speakerState) {
        if (currentMessages.isNotEmpty()) {
            listState.animateScrollToItem(currentMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B19))
            .testTag("transcript_window")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF142422))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onCloseEnabled) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.padding(end = 8.dp).testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "View Call Logs",
                        tint = Color.White
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TRANSCRIPT WITH ${call.callerName.uppercase()}",
                    color = neonTealAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = call.phoneNumber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Status: Secured Bilingual Pipeline Active",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DURATION", fontSize = 8.sp, color = Color.White.copy(alpha = 0.5f))
                        Text(call.duration, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MODE", fontSize = 8.sp, color = Color.White.copy(alpha = 0.5f))
                        Text(
                            if (call.isBilingual) "Bilingual" else "Monolingual",
                            fontSize = 12.sp,
                            color = neonTealAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (isSimulating && call.callStatus == "In Progress") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE53935).copy(alpha = 0.15f))
                    .padding(vertical = 4.dp, horizontal = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                    )
                    Text(
                        text = "Awaiting live response transcription feed...",
                        color = Color(0xFFFF8A80),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            if (currentMessages.isEmpty() && !isSimulating) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Call started. Connecting AI line...",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 14.dp, bottom = 24.dp)
                ) {
                    items(currentMessages) { message ->
                        ChatSpeechBubble(message = message)
                    }

                    if (isSimulating && speakerState != null) {
                        item {
                            TypingIndicatorBubble(sender = speakerState)
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF142422))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Agent Target Format: G.711 PCMU 8kHz",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFF00E5FF).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("ACTIVE COMPACT", color = Color(0xFF00E5FF), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun ChatSpeechBubble(message: CallMessage) {
    val isAssistant = message.sender == "Assistant"
    val bubbleAlign = if (isAssistant) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleShape = if (isAssistant) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    val bubbleBg = if (isAssistant) Color(0xFF00796B) else Color(0xFF223E3B)
    val labelText = if (isAssistant) "AI ASSISTANT" else "CALLER"
    val labelColor = if (isAssistant) Color(0xFF80CBC4) else Color(0xFF4DB6AC)

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = bubbleAlign
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = if (isAssistant) Arrangement.End else Arrangement.Start
        ) {
            Column(
                horizontalAlignment = if (isAssistant) Alignment.End else Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = labelText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = labelColor,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = message.timestampOffset,
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Box(
                    modifier = Modifier
                        .clip(bubbleShape)
                        .background(bubbleBg)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column {
                        Text(
                            text = message.text,
                            color = Color.White,
                            fontSize = 14.sp,
                            lineHeight = 19.sp
                        )

                        if (!message.translation.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(vertical = 5.dp, horizontal = 8.dp)
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("🌐", fontSize = 10.sp)
                                        Text(
                                            text = "DYNAMIC TRANSLATION ASSIST",
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00E5FF),
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = message.translation,
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicatorBubble(sender: String?) {
    val isAssistant = sender == "Assistant"
    val bubbleAlign = if (isAssistant) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleShape = if (isAssistant) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }
    val labelText = if (isAssistant) "AI ASSISTANT TYPING..." else "CALLER SPEAKING..."

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("typing_indicator"),
        contentAlignment = bubbleAlign
    ) {
        Column(horizontalAlignment = if (isAssistant) Alignment.End else Alignment.Start) {
            Text(
                text = labelText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAssistant) Color(0xFF00E5FF) else Color(0xFFFFB300),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(Color(0xFF142422))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "typing_dots")
                    val dotAlpha1 by infiniteTransition.animateFloat(
                        initialValue = 0.2f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = keyframes {
                                durationMillis = 1000
                                0.2f at 0
                                1f at 200
                                0.2f at 500
                            },
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "dot1"
                    )
                    val dotAlpha2 by infiniteTransition.animateFloat(
                        initialValue = 0.2f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = keyframes {
                                durationMillis = 1000
                                0.2f at 150
                                1f at 350
                                0.2f at 650
                            },
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "dot2"
                    )
                    val dotAlpha3 by infiniteTransition.animateFloat(
                        initialValue = 0.2f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = keyframes {
                                durationMillis = 1000
                                0.2f at 300
                                1f at 500
                                0.2f at 800
                            },
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "dot3"
                    )

                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF00E5FF).copy(alpha = dotAlpha1)))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF00E5FF).copy(alpha = dotAlpha2)))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF00E5FF).copy(alpha = dotAlpha3)))
                }
            }
        }
    }
}

@Composable
fun EmptyStateLanding() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "🧠",
                fontSize = 54.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Assistant Desk Dashboard",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select any call transaction from the left sidebar panel to analyze the bilingual speech transcription timeline, or run a live simulation to observe active translation workflows in real-time.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
                modifier = Modifier.widthIn(max = 380.dp)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FeatureIndicator(emoji = "🌐", text = "Dynamic EN/HI")
                FeatureIndicator(emoji = "⚡", text = "Live Translating")
                FeatureIndicator(emoji = "🛡️", text = "Local & Secure")
            }
        }
    }
}

@Composable
fun FeatureIndicator(emoji: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(emoji, fontSize = 14.sp)
        Text(text, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun IncomingCallHeadsUpDisplay(
    call: CallEntity,
    onAccept: (Boolean) -> Unit,
    onDecline: () -> Unit
) {
    val isHindi = call.detectedLanguage.contains("Hindi")
    
    Dialog(
        onDismissRequest = { onDecline() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF102522)),
                border = BorderStroke(2.dp, Color(0xFF00E5FF)),
                modifier = Modifier
                    .widthIn(max = 340.dp)
                    .padding(20.dp)
                    .testTag("incoming_call_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val ringAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.1f,
                        targetValue = 0.5f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulser"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .drawBehind {
                                drawCircle(
                                    color = Color(0xFF00E5FF),
                                    radius = (40.dp + (15.dp * ringAlpha)).toPx(),
                                    alpha = 0.4f - (0.3f * ringAlpha)
                                )
                            }
                            .clip(CircleShape)
                            .background(Color(0xFF00796B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔔", fontSize = 32.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "INCOMING AI ASSIST CALL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF),
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = call.callerName,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = call.phoneNumber,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🧠", fontSize = 12.sp)
                            Text(
                                text = "Detected: ${call.detectedLanguage.uppercase()}",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = onDecline,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("decline_call_btn")
                        ) {
                            Text("Decline", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { onAccept(isHindi) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("accept_call_btn")
                        ) {
                            Text("AI Answer", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

fun Modifier.size(size: androidx.compose.ui.unit.Dp) = this.size(size, size)
