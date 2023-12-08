package com.mobiuspace.medical

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.RecyclerView

class CommonRecyclerAdapter<T>(build: CommonRecyclerAdapter<T>.() -> Unit) :
  RecyclerView.Adapter<CommonViewHolder<T>>() {
  private var onLayout: ((viewType: Int) -> Int)? = null
  private var onCreateView: ((viewType: Int) -> View)? = null
  private lateinit var onItem: (position: Int) -> T
  private lateinit var onCount: () -> Int
  private var onItemViewType: (position: Int) -> Int = { 0 }
  private var onViewRecycle: (holder: CommonViewHolder<T>) -> Unit = { }
  private var onCreateViewHolder: ((view: View, viewType: Int) -> (CommonViewHolder<T>))? = null

  init {
    build()
  }

  fun onLayout(onLayout: (viewType: Int) -> Int) {
    this.onLayout = onLayout
  }

  fun onCreateView(onCreateView: (viewType: Int) -> View) {
    this.onCreateView = onCreateView
  }

  fun onCreateViewHolder(onCreateViewHolder: (view: View, viewType: Int) -> (CommonViewHolder<T>)) {
    this.onCreateViewHolder = onCreateViewHolder
  }

  fun onItem(onItem: (position: Int) -> T) {
    this.onItem = onItem
  }

  fun onCount(onCount: () -> Int) {
    this.onCount = onCount
  }

  fun onItemViewType(onItemViewType: (position: Int) -> Int) {
    this.onItemViewType = onItemViewType
  }

  fun onViewRecycle(onViewRecycle: (CommonViewHolder<T>) -> Unit) {
    this.onViewRecycle = onViewRecycle
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonViewHolder<T> {
    val view = when {
      onCreateView != null -> onCreateView?.invoke(viewType)
      onLayout != null -> LayoutInflater.from(parent.context).inflate(
        onLayout!!.invoke(viewType), parent, false
      )
      else -> throw IllegalArgumentException("itemView may not be null")
    } ?: throw IllegalArgumentException("itemView may not be null")
    return onCreateViewHolder?.invoke(view, viewType) ?: object :CommonViewHolder<T>(view, viewType) {
      override fun onBind(position: Int, viewType: Int, item: T) {

      }
    }
  }

  override fun onBindViewHolder(holder: CommonViewHolder<T>, position: Int) {
    holder.bind(position, onItem(position))
  }

  override fun onBindViewHolder(
    holder: CommonViewHolder<T>,
    position: Int,
    payloads: MutableList<Any>
  ) {
    super.onBindViewHolder(holder, position, payloads)
  }

  override fun getItemCount(): Int = 0

  override fun getItemViewType(position: Int): Int = onItemViewType(position)

  override fun onViewRecycled(holder: CommonViewHolder<T>) {
    super.onViewRecycled(holder)
    holder.lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    onViewRecycle.invoke(holder)
  }
}


abstract class CommonViewHolder<T>(itemView: View, val viewType: Int) : RecyclerView.ViewHolder(itemView), LifecycleOwner {
  val lifecycleRegistry = LifecycleRegistry(this)
  abstract fun onBind(position: Int, viewType: Int, item: T)

  init {
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    itemView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {

      override fun onViewAttachedToWindow(v: View) {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
      }

      override fun onViewDetachedFromWindow(v: View) {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
      }
    })
  }

  fun bind(position: Int, item: T) {
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    onBind(position, viewType, item)
  }

  override val lifecycle: Lifecycle
    get() = lifecycleRegistry
}