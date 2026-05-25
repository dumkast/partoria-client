package com.partoria.client.domain.model

data class ComputerPart(
    val id: Int,
    val name: String,
    val category: String,
    val brand: String,
    val price: Double,
    val specs: String,
    val releaseYear: Int,
    val details: List<PartDetail> = emptyList()
)