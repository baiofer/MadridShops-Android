package com.jarzasa.madridshops.domain.model

// Interface segregation

internal interface ReadAggregate<T> {
    fun count(): Int
    fun all(): List<T>
    fun get(position: Int): T
    fun get(name: String): T?
}

internal interface WriteAggregate<T> {
    fun add(element: T)
    fun delete(position: Int)
    fun delete(element: T)
}

internal interface Aggregate<T>: ReadAggregate<T>, WriteAggregate<T>
