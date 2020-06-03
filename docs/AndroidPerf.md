## RecyclerView: A Performance Primer

The *Recycling Center* framework is designed to help make high performance
RecyclerView-based screens. The framework is informed by a few performance
characteristics of Android.

### Views Are Expensive

The main purpose of a RecyclerView is to support view reuse for repeated UI elements
that have content beyond what can fit on screen. A key reason view reuse is important
is because **views are expensive to create**. The `View` base class itself
has [over 100 fields](http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/5.1.1_r1/android/view/View.java/),
(supported by over 20,000 lines of code!) resulting in a very hefty object on the heap.
A subclass like [TextView](http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/5.1.1_r1/android/widget/TextView.java/)
can add several dozen more properties for an even beefier allocation.

On the other hand, consider the complete set of data used to populate a
complex `View` within a `RecyclerView`. Often, a small handful of properties is enough to
create each entry-- perhaps a name, timestamp, thumbnail `Uri`, etc.
The complete model is very often at least an order of magnitude smaller
than the `View` itself.

It is relatively inexpensive to generate a list of models, as compared
to even a medium-sized list of `View`s. With a concise data model, creating
hundreds or even a few thousand models can be cheaper than creating a dozen
complex views.

To maintain this property, it is important to **keep ViewModels concise**.
Try to only include properties that will be used directly in the `View`,
and also avoid putting large objects like `Bitmap`s in the Model, instead
prefer to handle them by reference such as a `Uri`.


### Main Thread Matters
Most Android developers know the Golden Rule of Android performance:
Don't run excessive work on the Main Thread.
In Snapchat, this guideline is particularly important.

First, our app
is more than a simple screen or two. Snapchat is composed of more than
a half-dozen highly complex features, each of which could easily be an
app in itself. Running excessive work can impact the experience not just
of the feature it is related to, but of any other screen or feature.

Second, Snapchat is beloved for its highly swipable experience. A smooth
swiping experience requires the main thread be unblocked, so it can capture
input events and re-render the UI within 16ms (the length of a frame at 60fps)

Even if the work being run is to immediately support an on-screen feature,
it is important to do as much of that work off the main thread.

In the Recycling Center, we use a few patterns to keep the main thread clear:

- Integrated view preloader supports background-inflated views (since `View`s are expensive!)
- `ViewModel`s should hold any data that needs to be manipulated or parsed. This also helps
- Keep data generation off the main thread
- Reuse/recycle objects whenever possible, first and foremost those expensive Views but models and other data too.
- Litho is another excellent framework for moving work off the main thread. We are exploring how we may combine Litho with the Recycling Center.
- Systrace, systrace, systrace to know where you are spending time.
Don't be afraid to use `TraceCompat` directly to mark key moments worth monitoring.

### Keeping Updates Efficient
Most `RecyclerView`s accept updates of one kind or another, whether it be observable
updates pushed to the ui; A pull-to-refresh reload pattern; or user interactions that
cause views to update.

[DiffUtils](https://developer.android.com/reference/android/support/v7/util/DiffUtil.html)
is a convenient, general-purpose tool for handling RecyclerView updates. It is based on two important
principals of the underlying data models: (1) A notion of *identity*, eg a model that represents
the same entity; and (2) a notion of *sameness*, eg whether that model has changed between updates.

While DiffUtils is not the only way to handle updates, these two properties are always important.

To handle identity, a RecyclerView requires a numeric [Stable ID](https://developer.android.com/reference/android/support/v7/widget/RecyclerView.Adapter.html#setHasStableIds(boolean)).
While it is sometimes annoying to use a `Long` as the identifier (where we may have a `String`-based key, for example),
it is an important performance choice. Consider the performance of comparing equality
of two Longs vs two Strings-- `Long.equals()` is naturally `O(1)`, but String equality
is `O(str_len)`-- obtaining its worst performance when the Strings are in fact equal!

Since DiffUtil's runtime is worse than O(n), having a speedy equals method can help
considerably.

For this reason, Recycling Center requires a `long` identifier for each ViewModel.
