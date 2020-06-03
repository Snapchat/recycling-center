## Adding a View to Recycling Center
Adding a new View to a Recycling Center project is not hard, but involves
many pieces.

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

```java
class MyProjectBasicViewModel {

    private final String mLabel;
    private final Uri mIconUri;

    public MyProjectBasicViewModel(long modelId, String label, Uri iconUri) {
        super(MyProjectViewType.BASIC, modelId);
        mLabel = label;
        mIconUri = iconUri;
    }

    public String getLabel() {
        return mLabel;
    }

    public Ui getIconUri() {
        return mIconUri;
    }
}
```

An important property of the ViewModel is the numeric **modelId** field. RecyclerViews need a stable,
numeric id to represent content. This allows the RecyclerView to efficiently handle re-binding of
content when data updates, avoiding flickering and supporting animations if the model changes positions.

Ideally, the data source can emit a stable numeric id along with the data. If the source only supports
String-based identifiers, you can track a mapping from `String` to `long` for this field.

### 3. Create your ViewBinding
The *View Binding* maps your `ViewModel` onto your `View`:

```java
public class MyProjectBasicViewBinding extends ViewBinding<MyProjectBasicViewModel> {

    public static final @LayoutRes int LAYOUT = R.layout.myproject_basic;

    private TextView mLabel;
    private ImageView mIcon;

    @Override
    protected void onCreate(View itemView) {
        mName = itemView.findViewById(R.id.label);
        mIcon = itemView.findViewById(R.id.icon);
        itemView.setOnClickListener(mOnClickListener);
    }

    @Override
    protected void onBind(MyProjectBasicViewModel model,
                          MyProjectBasicViewModel previousModel) {

        mLabel.setText(model.getLabel());
        mIcon.setImageUri(model.getIconUri());
    }

    private final View.OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO
        }
    };
}

```

### 4. Add a View Type
The `ViewType` specifies how to bind a `View`, and creates a reference that a
`ViewModel` uses to specify its binding:

```java
public enum MyProjectViewType implements BindingAdapterViewType {

    BASIC(MyProjectBasicViewBinding.class, MyProjectBasicViewBinding.LAYOUT),
    COMPLEX(MyProjectComplexViewBinding.class, MyProjectComplexViewBinding.LAYOUT),
    HEADER(MyProjectHeaderViewBinding.class, MyProjectHeaderViewBinding.LAYOUT),
    ;

    private final @LayoutRes int mLayoutId;
    private final Class<? extends ViewBinding> mBindingClass;

    SendToViewType(Class<? extends ViewBinding> binding, @LayoutRes int layoutId) {
        mBindingClass = binding;
        mLayoutId = layoutId;
    }

    @Override
    @LayoutRes public int getLayoutId() {
        return mLayoutId;
    }

    @Override
    public Class<? extends ViewBinding> getViewBindingClass() {
        return mBindingClass;
    }
}
```

### 5. Using your View
By adding your `ViewModel` to a RecyclingCenter adapter, the framework
can instantiate your view and bind your model when needed:

```java
...
myViewModels.add(new MyProjectBasicViewModel(
            data.getId(),
            data.getLabel(),
            Uri.fromFile(data.getIconFilePath()))
     );

mViewModelAdapter.updateViewModels(Seekables.copyOf(myViewModels));
```