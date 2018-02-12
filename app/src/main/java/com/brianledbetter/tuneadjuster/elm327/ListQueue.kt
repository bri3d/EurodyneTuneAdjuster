package com.brianledbetter.tuneadjuster.elm327

/**
 * Created by brian.ledbetter on 2/11/18.
 */

class ListQueue<T> {

    private val items = mutableListOf<T>()

    fun isEmpty() = this.items.isEmpty()

    fun count() = this.items.count()

    override fun toString() = this.items.toString()

    fun enqueue(element: T){
        this.items.add(element)
    }

    fun dequeue():T?{
        return if (this.isEmpty()){
            null
        } else {
            this.items.removeAt(0)
        }
    }

    fun peek():T?{
        return this.items[0]
    }

}