package com.example.demo.domain.repository

import com.example.demo.domain.model.Comment
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import java.util.*

interface CommentRepository : CoroutineSortingRepository<Comment, UUID> {
    fun findByPostId(id: UUID): Flow<Comment>
}