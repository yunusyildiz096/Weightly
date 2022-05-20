package com.yonder.weightly.ui.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.yonder.weightly.R
import com.yonder.weightly.databinding.FragmentAddWeightBinding
import com.yonder.weightly.utils.extensions.showToast
import com.yonder.weightly.utils.extensions.toFormat
import com.yonder.weightly.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.util.*

const val CURRENT_DATE_FORMAT = "dd MMM yyyy"
const val TAG_DATE_PICKER = "Tag_Date_Picker"

@AndroidEntryPoint
class AddWeightFragment : BottomSheetDialogFragment() {

    private val viewModel: AddWeightViewModel by viewModels()

    private var selectedDate = Date()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_add_weight, container, false)

    private val binding by viewBinding(FragmentAddWeightBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observe()
        viewModel.fetchDate(selectedDate)
    }

    private fun observe() {
        lifecycleScope.launchWhenStarted {
            viewModel.eventsFlow.collect { event ->
                when (event) {
                    AddWeightViewModel.Event.PopBackStack -> {
                        findNavController().popBackStack()
                    }
                    is AddWeightViewModel.Event.ShowToast -> {
                        context.showToast(event.textResId)
                    }
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect(::setUIState)
        }
    }

    private fun setUIState(uiState: AddWeightViewModel.UiState) = with(binding) {
        uiState.currentWeight.let { weight ->
            tilInputNote.setText(weight?.note.orEmpty())
            val weightValue = weight?.value
            if (weightValue != null) {
                tilInputWeight.setText(weight.value.toString())
            } else {
                tilInputWeight.setText("")
            }
        }
    }

    private fun initViews() = with(binding) {
        btnSelectDate.setOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText(getString(R.string.select_date))
                    .setSelection(selectedDate.time)
                    .build()
            datePicker.addOnPositiveButtonClickListener { timestamp ->
                selectedDate = Date(timestamp)
                btnSelectDate.text = selectedDate.toFormat(CURRENT_DATE_FORMAT)
                viewModel.fetchDate(selectedDate)
            }
            datePicker.show(parentFragmentManager, TAG_DATE_PICKER);
        }

        btnSelectDate.text = selectedDate.toFormat(CURRENT_DATE_FORMAT)

        btnSave.setOnClickListener {
            val weight = tilInputWeight.text.toString()
            val note = tilInputNote.text.toString()
            viewModel.addWeight(weight = weight, note = note, date = selectedDate)
        }
    }

}
