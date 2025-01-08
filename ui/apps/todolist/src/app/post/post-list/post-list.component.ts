import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

import { Post } from '../shared/post.model';
import { PostService } from '../shared/post.service';

@Component({
    selector: 'app-post-list',
    templateUrl: './post-list.component.html',
    styleUrls: ['./post-list.component.css'],
    standalone: false
})
export class PostListComponent implements OnInit, OnDestroy {

  q = null;
  posts: Post[];
  sub: Subscription;

  constructor(private router: Router, private postService: PostService) {
  }

  search() {
    this.sub = this.postService.getPosts({ q: this.q })
    .subscribe({
      next:data => this.posts = data,
      error: err => console.log(err)
    }
    );
  }

  searchByTerm($event) {
    console.log('search by term:' + $event);
    this.updateTerm($event);
    this.search();
  }

  updateTerm($event) {
    console.log('update term:' + $event);
    this.q = $event;
  }

  clearTerm($event) {
    console.log('clear term:' + $event);
    this.q = null;
  }

  addPost() {
    this.router.navigate(['', 'post', 'new']);
  }

  ngOnInit() {
    console.log('calling ngOnInit::PostListComponent');
    this.search();
  }

  ngOnDestroy() {
    console.log('calling ngOnDestroy::PostListComponent');
    if (this.sub) {
      this.sub.unsubscribe();
    }
  }

}
