## Adding a View to Recycling Center
Adding a new View to a Recycling Center involves a few steps, to set up your View, ViewModel, and ViewBinding.

### 1. Create your View
Create a standard Android Layout file in your `res/layout/` directory:

`myproject_basic.xml`:
```xml

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/item"
    android:orientation="horizontal"
    android:layout_width="match_parent"
    android:layout_height="@dimen/send_to_cell_min_height"
    android:background="@color/myproject_card_background"
    >

     <ImageView
        android:id="@+id/icon"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />

    <TextView
        android:id="@+id/label"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />
</LinearLayout>

```


### 2. Create your ViewModel
A *View Model* (of type `AdapterViewModel`) should have all the properties needed to populate your View.
Ideally, the View Model should be immutable and contain simple, pre-computed properties.

```kotlin
class MyProjectBasicViewModel(
    val label: String,
    val iconUri: Uri
)
```

An important property of the ViewModel is the numeric **modelId** field. RecyclerViews need a stable,
numeric id to represent content. This allows the RecyclerView to efficiently handle re-binding of
content when data updates, avoiding flickering and supporting animations if the model changes positions.

Ideally, the data source can emit a stable numeric id along with the data. If the source only supports
String-based identifiers, you can track a mapping from `String` to `long` for this field.

### 3. Create your ViewBinding
The *View Binding* maps your `ViewModel` onto your `View`:

```kotlin
class MyProjectBasicViewBinding : ViewBinding<MyProjectBasicViewModel> {

    companion object {
        @LayoutRes val LAYOUT = R.layout.myproject_basic
    }

    private val label: TextView;
    private val icon: ImageView;

    override fun onCreate(itemView: View) {
        label = itemView.findViewById(R.id.label);
        icon = itemView.findViewById(R.id.icon);
        itemView.setOnClickListener { /*...*/ };
    }

    override fun onBind(model: MyProjectBasicViewModel, previousModel: MyProjectBasicViewModel?) {
        label.text = model.label;
        icon.imageUri = model.iconUri);
    }
}

```

### 4. Add a View Type
The `ViewType` specifies how to bind a `View`, and creates a reference that a
`ViewModel` uses to specify its binding:

```kotlin
enum class MyItemViewType(
    override val layoutId: Int,
    override val viewBindingClass: Class<out ViewBinding<*>>? = null
) : BindingAdapterViewType {
    BASIC(MyProjectBasicViewBinding.LAYOUT, MyProjectBasicViewBinding::class.java),
    COMPLEX(MyProjectComplexViewBinding.LAYOUT, MyProjectComplexViewBinding::class.java),
    HEADER(MyProjectHeaderViewBinding.LAYOUT, MyProjectHeaderViewBinding::class.java),
}
```

### 5. Using your View
By adding your `ViewModel` to a RecyclingCenter adapter, the framework
can instantiate your view and bind your model when needed:

```java
...
myViewModels.add(new MyProjectBasicViewModel(
            data.id,
            data.label,
            Uri.fromFile(data.iconFilePath))
     );

viewModelAdapter.updateViewModels(Seekables.copyOf(myViewModels));
```
