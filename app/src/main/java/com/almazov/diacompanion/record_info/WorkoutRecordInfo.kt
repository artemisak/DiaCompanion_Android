package com.almazov.diacompanion.record_info

import android.app.AlertDialog
import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.almazov.diacompanion.R
import com.almazov.diacompanion.data.AppDatabaseViewModel
import kotlinx.android.synthetic.main.fragment_workout_record_info.*

class WorkoutRecordInfo : Fragment() {

    private val args by navArgs<WorkoutRecordInfoArgs>()
    private lateinit var appDatabaseViewModel: AppDatabaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        val animation = TransitionInflater.from(requireContext()).inflateTransition(
            android.R.transition.move
        )
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_workout_record_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        appDatabaseViewModel = ViewModelProvider(this)[AppDatabaseViewModel::class.java]
        appDatabaseViewModel.readWorkoutRecord(args.selectedRecord.id).observe(viewLifecycleOwner, Observer { record ->

            tv_duration.text = record.duration.toString()
            tv_type.text = record.type

        })

        date.text = args.selectedRecord.date
        time.text = args.selectedRecord.time

        btn_edit.setOnClickListener{
            val action = WorkoutRecordInfoDirections.actionWorkoutRecordInfoToWorkoutAddRecord(args.selectedRecord)
            findNavController().navigate(action)
        }

        btn_delete.setOnClickListener{
            val builder = AlertDialog.Builder(requireContext())
            builder.setPositiveButton(this.resources.getString(R.string.Yes)) {_, _ ->
                appDatabaseViewModel.deleteWorkoutRecord(args.selectedRecord.id)
                args.selectedRecord.let { appDatabaseViewModel.deleteRecord(it) }
                findNavController().popBackStack()
            }
            builder.setNegativeButton(this.resources.getString(R.string.No)) {_, _ ->
            }
            builder.setTitle(this.resources.getString(R.string.DeleteRecord))
            builder.setMessage(this.resources.getString(R.string.AreUSureDeleteRecord))
            builder.create().show()
        }

        super.onViewCreated(view, savedInstanceState)

    }
}