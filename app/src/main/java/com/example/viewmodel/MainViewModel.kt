package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.model.Country
import com.example.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.example.model.Transaction

class MainViewModel : ViewModel() {
    private val _userState = MutableStateFlow(User(uid = "user123", deviceId = "dev_001"))
    val userState: StateFlow<User> = _userState.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun updateProfile(name: String, paymentId: String) {
        _userState.update { it.copy(displayName = name, paymentId = paymentId) }
    }

    fun fetchTransactions() {
        // Mocking a Firestore fetch response since Firebase Auth isn't fully set up yet
        val mockData = listOf(
            Transaction("1", "Math Quiz Reward", 10, true, "Completed", System.currentTimeMillis() - 1000 * 60 * 5),
            Transaction("2", "Daily Check-in", 50, true, "Completed", System.currentTimeMillis() - 1000 * 60 * 60 * 2),
            Transaction("3", "Withdrawal (UPI)", 1000, false, "Pending", System.currentTimeMillis() - 1000 * 60 * 60 * 24),
            Transaction("4", "Captcha Reward", 5, true, "Completed", System.currentTimeMillis() - 1000 * 60 * 60 * 25),
            Transaction("5", "Referral Bonus", 100, true, "Completed", System.currentTimeMillis() - 1000 * 60 * 60 * 48)
        )
        _transactions.value = mockData
    }

    fun updateCountry(country: Country) {
        _userState.update { it.copy(country = country) }
    }

    fun addCoins(amount: Int) {
        _userState.update { 
            it.copy(
                coinBalance = it.coinBalance + amount,
                lifetimeEarnings = if (amount > 0) it.lifetimeEarnings + amount else it.lifetimeEarnings
            )
        }
    }
    
    fun performDailyCheckIn() {
        val current = _userState.value
        if (current.canCheckIn) {
            _userState.update { 
                it.copy(
                    coinBalance = it.coinBalance + 50,
                    canCheckIn = false
                ) 
            }
        }
    }

    fun useMathQuizAttempt(): Boolean {
        val current = _userState.value
        if (current.dailyMathLimit > 0) {
            _userState.update { 
                it.copy(
                    coinBalance = it.coinBalance + 10,
                    dailyMathLimit = it.dailyMathLimit - 1
                ) 
            }
            return true
        }
        return false
    }

    fun useCaptchaAttempt(): Boolean {
        val current = _userState.value
        if (current.dailyCaptchaLimit > 0) {
            _userState.update { 
                it.copy(
                    coinBalance = it.coinBalance + 5,
                    dailyCaptchaLimit = it.dailyCaptchaLimit - 1
                ) 
            }
            return true
        }
        return false
    }
}
