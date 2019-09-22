package com.readrops.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.readrops.app.R
import com.readrops.app.database.entities.Folder
import com.readrops.app.databinding.FolderOptionsLayoutBinding

class FolderOptionsDialogFragment : BottomSheetDialogFragment() {

    private lateinit var folder: Folder
    private lateinit var foldersOptionsLayoutBinding: FolderOptionsLayoutBinding

    companion object {
        val FOLDER_KEY = "FOLDER_KEY"

        fun newInstance(folder: Folder): FolderOptionsDialogFragment {
            val args = Bundle()
            args.putParcelable(FOLDER_KEY, folder)

            val fragment = FolderOptionsDialogFragment()
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        folder = arguments?.getParcelable(FOLDER_KEY)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        foldersOptionsLayoutBinding = DataBindingUtil.inflate(inflater, R.layout.folder_options_layout, container, false)

        return foldersOptionsLayoutBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        foldersOptionsLayoutBinding.folderOptionsTitle.text = folder.name
        foldersOptionsLayoutBinding.folderOptionsEdit.setOnClickListener { openEditFolderDialog() }
        foldersOptionsLayoutBinding.folderOptionsDelete.setOnClickListener { deleteFolder() }
    }

    private fun openEditFolderDialog() {
        dismiss()
        (parentFragment as FoldersFragment).editFolder(folder)
    }

    private fun deleteFolder() {
        dismiss()
        (parentFragment as FoldersFragment).deleteFolder(folder)
    }
}