# ZoomableRecyclerView 

A customizable Android RecyclerView with smooth zooming and panning capabilities, perfect for building image galleries, comic readers, or any content that requires pinch-to-zoom functionality.

##  Features

-  Smooth pinch-to-zoom and double-tap to zoom
-  Smooth panning when zoomed in
-  Fling and over-scroll support
-  Easy to integrate with existing RecyclerView setups
-  Customizable zoom levels and animation duration



##  Usage

1. Add ZoomableRecyclerView to your layout:

```xml
<io.github.tiiime.manga.widget.ZoomableRecyclerView
    android:id="@+id/zoomableRecyclerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    />
```

2. Set up your adapter as you would with a regular RecyclerView:

```kotlin
val adapter = YourAdapter()
zoomableRecyclerView.adapter = adapter
zoomableRecyclerView.layoutManager = LinearLayoutManager(context)
```

##  Preview
https://github.com/user-attachments/assets/a2a4e195-9c9e-4bb8-a2e0-647756a97956

## TODO

- bugfix
