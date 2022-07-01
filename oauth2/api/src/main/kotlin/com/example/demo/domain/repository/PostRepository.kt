package com.example.demo.domain.repository

import com.example.demo.interfaces.dto.PostSummary
import com.example.demo.domain.model.Post
import com.example.demo.domain.model.Status
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

interface PostRepository : CoroutineCrudRepository<Post, UUID> {
    fun findByTitleContains(title: String): Flow<PostSummary>
    fun findByStatus(status: Status): Flow<Post>
}