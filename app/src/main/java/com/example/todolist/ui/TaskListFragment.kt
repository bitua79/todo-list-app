package com.example.todolist.ui

import com.example.todolist.BR
import com.example.todolist.data.model.Priority
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskListFragment : TaskListFragmentDelegate(Priority.High, true)