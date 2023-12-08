package com.mobiuspace.medical

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.RecyclerView

class CommonRecyclerAdapter<T>(build: CommonRecyclerAdapter<T>.() -> Unit) :
  RecyclerView.Adapter<CommonViewHolder>() {
  private var onLayout: ((viewType: Int) -> Int)? = null
  private var onCreateView: ((viewType: Int) -> View)?= null
  private lateinit var onItem: (position: Int) -> T
  private lateinit var onCount: () -> Int
  private lateinit var onBind: View.(holder: CommonViewHolder, position: Int, viewType: Int, item: T) -> Unit
  private var onItemViewType: (position: Int) -> Int = { 0 }
  private var onViewRecycle: (holder: CommonViewHolder) -> Unit = { }
  private var onCreateViewHolder: ((view: View, viewType: Int) -> (CommonViewHolder))? = null

  init {
    build()
  }

  fun onLayout(onLayout: (viewType: Int) -> Int) {
    this.onLayout = onLayout
  }

  fun onCreateView(onCreateView: (viewType: Int) -> View) {
    this.onCreateView = onCreateView
  }

  fun onCreateViewHolder(onCreateViewHolder: (view: View, viewType: Int) -> (CommonViewHolder)) {
    this.onCreateViewHolder = onCreateViewHolder
  }

  fun onItem(onItem: (position: Int) -> T) {
    this.onItem = onItem
  }

  fun onBind(onBind: View.(holder: CommonViewHolder, position: Int, viewType: Int, item: T) -> Unit) {
    this.onBind = onBind
  }

  fun onCount(onCount: () -> Int) {
    this.onCount = onCount
  }

  fun onItemViewType(onItemViewType: (position: Int) -> Int) {
    this.onItemViewType = onItemViewType
  }

  fun onViewRecycle(onViewRecycle: (CommonViewHolder) -> Unit) {
    this.onViewRecycle = onViewRecycle
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonViewHolder {
    val view = when {
      onCreateView != null -> onCreateView?.invoke(viewType)
      onLayout != null -> LayoutInflater.from(parent.context).inflate(
        onLayout!!.invoke(viewType), parent, false
      )
      else -> throw IllegalArgumentException("itemView may not be null")
    } ?: throw IllegalArgumentException("itemView may not be null")
    return onCreateViewHolder?.invoke(view, viewType) ?: CommonViewHolder(view, viewType)
  }

  override fun onBindViewHolder(holder: CommonViewHolder, position: Int) {
    holder.bind(position, onItem(position), onBind)
  }

  override fun getItemCount(): Int = 0

  override fun getItemViewType(position: Int): Int = onItemViewType(position)

  override fun onViewRecycled(holder: CommonViewHolder) {
    super.onViewRecycled(holder)
    holder.lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    onViewRecycle.invoke(holder)
  }
}


open class CommonViewHolder(itemView: View, val viewType: Int) : RecyclerView.ViewHolder(itemView), LifecycleOwner {
  val lifecycleRegistry = LifecycleRegistry(this)

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

  inline fun <T> bind(position: Int, item: T, onBind: View.(CommonViewHolder, Int, Int, T) -> Unit) {
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    itemView.onBind(this, position, viewType, item)
  }

  override val lifecycle: Lifecycle
    get() = lifecycleRegistry
}