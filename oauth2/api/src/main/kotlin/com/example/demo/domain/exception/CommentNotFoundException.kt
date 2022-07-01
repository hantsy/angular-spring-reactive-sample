package com.example.demo.domain.exception

import java.util.*

class CommentNotFoundException(id: UUID) : RuntimeException("Could not find comment with id $id")