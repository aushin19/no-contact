# Views UI Samples — Reference

**Docs**: [developer.android.com/develop/ui/views](https://developer.android.com/develop/ui/views)

---

## Patterns Covered

1. RecyclerView + ListAdapter
2. View Binding
3. Fragment + ViewModel
4. MotionLayout animation
5. Custom View (attrs + drawing)

---

## 1. RecyclerView + ListAdapter (DiffUtil)

```kotlin
// Adapter with DiffUtil (auto-animates changes)
class TaskAdapter(
    private val onDelete: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.ViewHolder>(TaskDiffCallback()) {

    inner class ViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.tvTitle.text = task.title
            binding.cbCompleted.isChecked = task.isCompleted
            binding.btnDelete.setOnClickListener { onDelete(task) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(old: Task, new: Task) = old.id == new.id
    override fun areContentsTheSame(old: Task, new: Task) = old == new
}

// In Fragment
adapter = TaskAdapter(onDelete = { viewModel.delete(it) })
binding.recyclerView.adapter = adapter
viewLifecycleOwner.lifecycleScope.launch {
    viewModel.tasks.collect { adapter.submitList(it) }
}
```

---

## 2. View Binding

```kotlin
// Activity
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnAction.setOnClickListener { /* ... */ }
    }
}

// Fragment (avoid memory leaks with _binding pattern)
class TaskListFragment : Fragment(R.layout.fragment_task_list) {
    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTaskListBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // CRITICAL: avoid memory leak
    }
}
```

build.gradle.kts:
```kotlin
android { buildFeatures { viewBinding = true } }
```

---

## 3. Fragment + ViewModel (shared)

```kotlin
// Shared ViewModel between fragments
class SharedViewModel : ViewModel() {
    private val _selectedTask = MutableLiveData<Task?>()
    val selectedTask: LiveData<Task?> = _selectedTask
    fun selectTask(task: Task) { _selectedTask.value = task }
}

// In Fragment A
private val viewModel: SharedViewModel by activityViewModels()

// In Fragment B
private val viewModel: SharedViewModel by activityViewModels()
viewModel.selectedTask.observe(viewLifecycleOwner) { task -> showDetail(task) }
```

---

## 4. MotionLayout

```xml
<!-- res/xml/motion_scene.xml -->
<MotionScene>
    <Transition
        app:constraintSetStart="@id/start"
        app:constraintSetEnd="@id/end"
        app:duration="300">
        <OnSwipe
            app:touchAnchorId="@id/card"
            app:dragDirection="dragUp"/>
    </Transition>

    <ConstraintSet android:id="@+id/start">
        <Constraint android:id="@+id/card"
            android:layout_width="match_parent"
            android:layout_height="100dp"
            app:layout_constraintBottom_toBottomOf="parent"/>
    </ConstraintSet>

    <ConstraintSet android:id="@+id/end">
        <Constraint android:id="@+id/card"
            android:layout_width="match_parent"
            android:layout_height="400dp"
            app:layout_constraintBottom_toBottomOf="parent"/>
    </ConstraintSet>
</MotionScene>
```

```kotlin
// Trigger programmatically
binding.motionLayout.transitionToEnd()
binding.motionLayout.transitionToStart()
```

---

## Key Notes

- `ListAdapter` + `DiffUtil` = efficient incremental updates vs `notifyDataSetChanged()`
- Always null `_binding` in `onDestroyView()` — fragments outlive their views
- Prefer View Binding over `findViewById()` and kotlin-synthetics (deprecated)
- `by activityViewModels()` shares ViewModel across all fragments in same Activity
- MotionLayout requires `constraintlayout:2.0+`; great for complex gesture-driven animations
