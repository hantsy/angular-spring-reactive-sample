/* tslint:disable:no-unused-variable */
// Http testing module and mocking controller
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpClient, HttpClientModule, HttpErrorResponse } from '@angular/common/http';
import { TestBed, async, inject } from '@angular/core/testing';
import { BaseRequestOptions, Http, Response, ResponseOptions } from '@angular/http';
import { Observable} from 'rxjs';

import { PostService } from './post.service';
import { Post } from './post.model';

const posts = [
  {
    id: '1',
    title: 'Getting started with REST',
    content: 'Content of Getting started with REST',
    createdDate: '9/22/16 4:15 PM'
  },
  {
    id: '2',
    title: 'Getting started with AngularJS 1.x',
    content: 'Content of Getting started with AngularJS 1.x',
    createdDate: '9/22/16 4:15 PM'
  },
  {
    id: '3',
    title: 'Getting started with Angular 2',
    content: 'Content of Getting started with Angular2',
    createdDate: '9/22/16 4:15 PM'
  },
] as Post[];

describe('Service: Post', () => {
  let postService: PostService;

  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      // Import the HttpClient mocking services
      imports: [ HttpClientTestingModule ],
      // Provide the service-under-test and its dependencies
      providers: [
        PostService
        // HttpErrorHandler,
        // MessageService
      ]
    });

    // Inject the http, test controller, and service-under-test
    // as they will be referenced by each test.
    httpClient = TestBed.get(HttpClient);
    httpTestingController = TestBed.get(HttpTestingController);
    postService = TestBed.get(PostService);
  });

  afterEach(() => {
    // After every test, assert that there are no more pending requests.
    httpTestingController.verify();
  });


  it('should not be null...', () => {
    expect(postService).toBeTruthy();
  });

  describe('#getPosts', () => {
    let expectedPosts: Post[];

    beforeEach(() => {
       expectedPosts = posts;
       postService = TestBed.get(PostService);
    });

    it('should get posts...', () => {
      postService.getPosts()
        .subscribe(
          res => expect(res).toEqual(expectedPosts, 'should return exptected posts'),
          fail
        );

      // PostService should have made one request to GET heroes from expected URL
      const req = httpTestingController.expectOne(postService.apiUrl);
      expect(req.request.method).toEqual('GET');

      // Respond with the mock posts
      req.flush(expectedPosts);

    });

    it('should be OK returning no posts', () => {

      postService.getPosts()
      .subscribe(
        res => expect(res.length).toEqual(0, 'should have empty posts array'),
        fail
      );

      const req = httpTestingController.expectOne(postService.apiUrl);
      req.flush([]); // Respond with no posts
    });
  });

});
