package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class CallViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CallRepository

    // Exposed Flows
    val callList: StateFlow<List<CallEntity>>
    private val _selectedCall = MutableStateFlow<CallEntity?>(null)
    val selectedCall: StateFlow<CallEntity?> = _selectedCall.asStateFlow()

    // Simulation Live States
    private val _simulatedMessages = MutableStateFlow<List<CallMessage>>(emptyList())
    val simulatedMessages: StateFlow<List<CallMessage>> = _simulatedMessages.asStateFlow()

    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    private val _speakerState = MutableStateFlow<String?>(null) // "Caller", "Assistant" or null
    val speakerState: StateFlow<String?> = _speakerState.asStateFlow()

    private val _incomingCallState = MutableStateFlow<CallEntity?>(null) // To show incoming call popup overlay
    val incomingCallState: StateFlow<CallEntity?> = _incomingCallState.asStateFlow()

    private var simulationJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CallRepository(database.callDao())
        
        callList = repository.allCalls.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun selectCall(call: CallEntity?) {
        _selectedCall.value = call
        if (call == null) {
            cancelSimulation()
        } else {
            // Cancel any current simulation when viewing static calls
            cancelSimulation()
        }
    }

    // Trigger an incoming call flow
    fun triggerIncomingCall(isHindi: Boolean) {
        viewModelScope.launch {
            val phone = if (isHindi) "+91 95432-10987" else "+1 (650) 555-0133"
            val name = if (isHindi) "Karan Johar (Inquiry)" else "David Smith (Support)"
            val newCallPlaceholder = CallEntity(
                phoneNumber = phone,
                callerName = name,
                timestamp = System.currentTimeMillis(),
                duration = "Active",
                detectedLanguage = if (isHindi) "Hindi / हिंदी" else "English",
                callStatus = "In Progress",
                isBilingual = isHindi,
                messages = emptyList()
            )
            // Show incoming call overlay HUD
            _incomingCallState.value = newCallPlaceholder
        }
    }

    // Accept incoming call and start active dynamic assistant simulation
    fun acceptIncomingCall(isHindi: Boolean) {
        val incoming = _incomingCallState.value ?: return
        _incomingCallState.value = null
        
        // Save to database as active call
        viewModelScope.launch {
            val id = repository.insertCall(incoming)
            val savedCall = incoming.copy(id = id.toInt())
            _selectedCall.value = savedCall
            startLiveSimulation(savedCall, isHindi)
        }
    }

    fun declineIncomingCall() {
        _incomingCallState.value = null
    }

    // Start playback simulation of a call step-by-step
    fun startLiveSimulation(call: CallEntity, isHindi: Boolean) {
        cancelSimulation()
        _isSimulating.value = true
        _simulatedMessages.value = emptyList()

        val fullScript = if (isHindi) {
            listOf(
                CallMessage(
                    id = UUID.randomUUID().toString(),
                    sender = "Caller",
                    text = "नमस्ते, क्या मुझे कार इंश्योरेंस के रिन्यूअल चार्जेस पता चल सकते हैं?",
                    translation = "Hello, can I find out the renewal charges for my car insurance?",
                    timestampOffset = "00:04",
                    language = "hi"
                ),
                CallMessage(
                    id = UUID.randomUUID().toString(),
                    sender = "Assistant",
                    text = "नमस्ते! मैं आपकी पूरी सहायता करूँगा। सुरक्षा मोटर इंश्योरेंस में आपका स्वागत है। क्या आप अपना पॉलिसी नंबर बता सकते हैं?",
                    translation = "Namaste! I will fully assist you. Welcome to Suraksha Motor Insurance. Can you tell me your policy number?",
                    timestampOffset = "00:18",
                    language = "hi"
                ),
                CallMessage(
                    id = UUID.randomUUID().toString(),
                    sender = "Caller",
                    text = "हाँ, पॉलिसी नंबर है MH-12-3004।",
                    translation = "Yes, the policy number is MH-12-3004.",
                    timestampOffset = "00:35",
                    language = "hi"
                ),
                CallMessage(
                    id = UUID.randomUUID().toString(),
                    sender = "Assistant",
                    text = "विवरण देखने के लिए धन्यवाद। आपके रिन्यूअल शुल्क ₹8,450 हैं, जिसमें जीएसटी भी शामिल है। क्या आप इसे रिन्यू करना चाहते हैं?",
                    translation = "Thank you for the details. Your renewal charges are ₹8,450, including GST. Do you want to renew it?",
                    timestampOffset = "00:55",
                    language = "hi"
                ),
                CallMessage(
                    id = UUID.randomUUID().toString(),
                    sender = "Caller",
                    text = "हाँ, इसकी रिन्यूअल डेट कब तक की है?",
                    translation = "Yes, what is the deadline for this renewal?",
                    timestampOffset = "01:12",
                    language = "hi"
                ),
                CallMessage(
                    id = UUID.randomUUID().toString(),
                    sender = "Assistant",
                    text = "यह पॉलिसी 31 मई को समाप्त हो जाएगी। मैं भुगतान के लिए आपके नंबर पर एक रिन्यूअल पेमेंट लिंक भेज रहा हूँ।",
                    translation = "This policy will expire on May 31st. I am sending a payment link to your number.",
                    timestampOffset = "01:30",
                    language = "hi"
                )
            )
        } else {
            listOf(
                CallMessage(
                    id = UUID.randomUUID().toString(),
                    sender = "Caller",
                    text = "Hi, can I confirm if my order has shipped? It was placed yesterday.",
                    timestampOffset = "00:05",
                    language = "en"
                ),
                CallMessage(
                    id = UUID.randomUUID().toString(),
                    sender = "Assistant",
                    text = "Hi! I will check that for you. Can you please provide your 6-digit order ID?",
                    timestampOffset = "00:15",
                    language = "en"
                ),
                CallMessage(
                    id = UUID.randomUUID().toString(),
                    sender = "Caller",
                    text = "Yes, the order ID is 998243.",
                    timestampOffset = "00:28",
                    language = "en"
                ),
                CallMessage(
                    id = UUID.randomUUID().toString(),
                    sender = "Assistant",
                    text = "Got it! Your order 998243 has indeed shipped from our Chicago warehouse. It's on its way and scheduled for delivery tomorrow by 3 PM.",
                    timestampOffset = "00:46",
                    language = "en"
                ),
                CallMessage(
                    id = UUID.randomUUID().toString(),
                    sender = "Caller",
                    text = "Awesome, that's incredibly fast! Thank you so much for the quick help.",
                    timestampOffset = "01:02",
                    language = "en"
                ),
                CallMessage(
                    id = UUID.randomUUID().toString(),
                    sender = "Assistant",
                    text = "You are very welcome! It was my pleasure. Let us know if you need anything else. Have a great day!",
                    timestampOffset = "01:15",
                    language = "en"
                )
            )
        }

        simulationJob = viewModelScope.launch {
            val accumulatedList = mutableListOf<CallMessage>()
            for (message in fullScript) {
                // Show speaker typing state
                _speakerState.value = message.sender
                delay(1800) // Simulate cognitive delay / transcription typing

                accumulatedList.add(message)
                _simulatedMessages.value = accumulatedList.toList()
                _speakerState.value = null
                delay(1200) // Pause between speech turns
            }

            // At the end, update the Call status in Room to "Completed" and save the full message list
            val finalCall = call.copy(
                callStatus = "Completed",
                duration = if (isHindi) "01:38" else "01:21",
                messages = accumulatedList
            )
            repository.updateCall(finalCall)
            _selectedCall.value = finalCall
            _isSimulating.value = false
        }
    }

    fun cancelSimulation() {
        simulationJob?.cancel()
        _isSimulating.value = false
        _speakerState.value = null
        _simulatedMessages.value = emptyList()
    }

    fun deleteCall(id: Int) {
        viewModelScope.launch {
            repository.deleteCallById(id)
            if (_selectedCall.value?.id == id) {
                _selectedCall.value = null
                cancelSimulation()
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
            _selectedCall.value = null
            cancelSimulation()
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelSimulation()
    }
}
