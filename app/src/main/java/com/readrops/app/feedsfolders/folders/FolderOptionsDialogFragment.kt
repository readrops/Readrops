package com.readrops.app.feedsfolders.folders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.readrops.app.databinding.FolderOptionsLayoutBinding
import com.readrops.db.entities.Folder

class FolderOptionsDialogFragment : BottomSheetDialogFragment() {

    private lateinit var folder: Folder

    private var _binding: FolderOptionsLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        folder = arguments?.getParcelable(FOLDER_KEY)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FolderOptionsLayoutBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.folderOptionsTitle.text = folder.name
        binding.folderOptionsEdit.setOnClickListener { openEditFolderDialog() }
        binding.folderOptionsDelete.setOnClickListener { deleteFolder() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openEditFolderDialog() {
        dismiss()
        (parentFragment as FoldersFragment).editFolder(folder)
    }

    private fun deleteFolder() {
        dismiss()
        (parentFragment as FoldersFragment).deleteFolder(folder)
    }

    companion object {
        const val FOLDER_KEY = "FOLDER_KEY"

        fun newInstance(folder: Folder): FolderOptionsDialogFragment {
            val args = Bundle()
            args.putParcelable(FOLDER_KEY, folder)

            val fragment = FolderOptionsDialogFragment()
            fragment.arguments = args

            return fragment
        }
    }
}