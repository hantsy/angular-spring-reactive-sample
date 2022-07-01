package com.example.demo.domain.exception

import java.util.*

class PostNotFoundException(id: UUID) : RuntimeException("Could not find post with id $id")