package com.premraj.moneyboard.feature.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.premraj.moneyboard.core.dashboard.ObserveMonthlyDashboardUseCase
import com.premraj.moneyboard.core.domain.model.DashboardMode
import com.premraj.moneyboard.core.domain.model.MonthlyDashboardSnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val observeMonthlyDashboard: ObserveMonthlyDashboardUseCase,
    initialMonth: YearMonth = YearMonth.now()
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(initialMonth)
    private val selectedMode = MutableStateFlow(DashboardMode.PLAN)
    private var cachedSnapshot: MonthlyDashboardSnapshot? = null

    private val dataState: Flow<DataState> =
        selectedMonth.flatMapLatest { month ->
            observeMonthlyDashboard(month)
                .map<MonthlyDashboardSnapshot, DataState> { snapshot ->
                    cachedSnapshot = snapshot
                    DataState.Data(snapshot)
                }
                .catch { throwable ->
                    runCatching {
                        Log.e("DashboardViewModel", "Failed to load dashboard for month $month", throwable)
                    }
                    emit(DataState.Error)
                }
        }

    val uiState =
        combine(
            selectedMonth,
            selectedMode,
            dataState
        ) { month, mode, state ->
            when (state) {
                DataState.Loading ->
                    DashboardUiState(
                        selectedMonth = month,
                        mode = mode,
                        snapshot = cachedSnapshot,
                        isLoading = cachedSnapshot == null
                    )

                DataState.Error ->
                    DashboardUiState(
                        selectedMonth = month,
                        mode = mode,
                        snapshot = cachedSnapshot,
                        isLoading = false,
                        errorMessage = if (cachedSnapshot == null) "Unable to load your finance dashboard." else null
                    )

                is DataState.Data ->
                    DashboardUiState(
                        selectedMonth = month,
                        mode = mode,
                        snapshot = state.snapshot,
                        isLoading = false
                    )
            }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DashboardUiState(
                    selectedMonth = initialMonth
                )
            )

    fun previousMonth() {
        selectedMonth.value =
            selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        selectedMonth.value =
            selectedMonth.value.plusMonths(1)
    }

    fun resetToCurrentMonth() {
        selectedMonth.value = YearMonth.now()
    }

    fun selectMonth(month: YearMonth) {
        selectedMonth.value = month
    }

    fun selectMode(mode: DashboardMode) {
        selectedMode.value = mode
    }

    private sealed interface DataState {
        data object Loading : DataState
        data object Error : DataState

        data class Data(
            val snapshot: MonthlyDashboardSnapshot
        ) : DataState
    }

    companion object {
        fun factory(
            observeMonthlyDashboard: ObserveMonthlyDashboardUseCase,
            initialMonth: YearMonth = YearMonth.now()
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    DashboardViewModel(
                        observeMonthlyDashboard = observeMonthlyDashboard,
                        initialMonth = initialMonth
                    )
                }
            }
    }
}
