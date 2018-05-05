import { Injectable, Inject } from '@angular/core';
import { Post } from './post.model';
import { Comment } from './comment.model';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { HttpParams } from '@angular/common/http';

const apiUrl = environment.baseApiUrl + '/posts';

@Injectable()
export class PostService {

  constructor(private http: HttpClient) { }

  getPosts(term?: any): Observable<any> {
    const params: HttpParams = new HttpParams();
    Object.keys(term).map(key => {
      if (term[key]) { params.set(key, term[key]); }
    });
    return this.http.get(`${apiUrl}`, { params });
  }

  getPost(id: string): Observable<any> {
    return this.http.get(`${apiUrl}/${id}`);
  }

  savePost(data: Post) {
    console.log('saving post:' + data);
    return this.http.post(`${apiUrl}`, data);
  }

  updatePost(id: string, data: Post) {
    console.log('updating post:' + data);
    return this.http.put(`${apiUrl}/${id}`, data);
  }

  deletePost(id: string) {
    console.log('delete post by id:' + id);
    return this.http.delete(`${apiUrl}/${id}`);
  }

  saveComment(id: string, data: Comment) {
    return this.http.post(`${apiUrl}/${id}/comments`, data);
  }

  getCommentsOfPost(id: string): Observable<any> {
    return this.http.get(`${apiUrl}/${id}/comments`);
  }

}
