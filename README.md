# Recycling Center
The Recycling Center is a framework for creating efficient RecyclerViews.
It is designed around an immutable `ViewModel` pattern, put together in logical
`Sections` of views.

The Recycling Center is as much a **pattern** as it is a library. To build
a high-performing `RecyclerView`-based screen, it is helpful to understand
certain [Android performance characteristics](docs/AndroidPerf.md).

## Data, Models, and Views
![pipeline](docs/rc_pipeline.png)

## Reference
The Recycling Center uses several primitives to create an efficient `RecyclerView`.
See the [Adding a Custom View](docs/CustomView.md) guide for how they fit together.


* [AdapterViewType](./src/main/java/com/snap/ui/recycling/AdapterViewType.java):
Each view in the RecyclerView has an enumerated type.
* [AdapterViewModel](./src/main/java/com/snap/ui/recycling/viewmodel/AdapterViewModel.java):
Contains all the data needed to render a view, ideally including any formatting.
* [ViewBinding](./src/main/java/com/snap/ui/recycling/ViewBinding.java):
Binding code that connects a `ViewModel` to its `View`.

A [ViewFactory](./src/main/java/com/snap/ui/recycling/factory/ViewFactory.java)
creates a `View` from an `AdapterViewType`. The ViewFactory supports background-inflated
views from a
[ViewPrefetcher](./src/main/java/com/snap/ui/recycling/prefetch/ViewPrefetcher.java).

The Recycling Center `RecyclerView.Adapter` adhere to the
[ViewModelAdapter](./src/main/java/com/snap/ui/recycling/adapter/ViewModelAdapter.java) interface.
There are a few different adapters for different use-cases:
* [BindingViewModelAdapter](./src/main/java/com/snap/ui/recycling/adapter/BindingViewModelAdapter.java):
Basic adapter mapping a List of ViewModels in a RecyclerView.
* [SectionedRecyclerViewAdapter](./src/main/java/com/snap/ui/recycling/adapter/SectionedRecyclerViewAdapter.java):
A sectioned ViewModelAdapter.
* [ObservableViewModelSectionAdapter](./src/main/java/com/snap/ui/recycling/adapter/ObservableViewModelSectionAdapter.java):
A sectioned adapter powered by `RxJava` Observables.

Instead of Lists of ViewModels, the Recycling Center uses a
[Seekable](./src/main/java/com/snap/ui/seeking/Seekable.java)
interface to bring content to an Adapter. This simplified interface
supports simple list-based binding via
[Seekables.copyOf(list)](./src/main/java/com/snap/ui/seeking/Seekables.java)
or  efficient, fluid binding from a `Cursor` or other seekable stream of data.

## Resources
* [RecyclerView](https://developer.android.com/guide/topics/ui/layout/recyclerview)
* [DiffUtil](https://developer.android.com/reference/androidx/recyclerview/widget/DiffUtil)