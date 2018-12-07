// Http testing module and mocking controller
import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import {
  HttpClient,
  HttpClientModule,
  HttpErrorResponse
} from '@angular/common/http';
import {
  ComponentFixture,
  TestBed,
  async,
  inject,
  ComponentFixtureAutoDetect,
  tick,
  fakeAsync
} from '@angular/core/testing';
import {
  BaseRequestOptions,
  Http,
  Response,
  ResponseOptions
} from '@angular/http';
import { Observable, of, empty, from, interval, defer } from 'rxjs';
import { PostFormComponent } from './post-form.component';
import { PostService } from '../post.service';
import { Post } from '../post.model';
import { SharedModule } from '../../../shared/shared.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { DebugElement, Component } from '@angular/core';
import { By } from '@angular/platform-browser';

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
  }
] as Post[];

// class MockPostService {
//   getPosts(term?: string): Observable<any> {
//     return from(posts);
//   }
//   savePost(post: Post): Observable<any> {
//     return empty();
//   }
//   updatePost(id: string, post: Post): Observable<any> {
//     return empty();
//   }
// }

describe('Component: PostFormComponent', () => {
  let component: PostFormComponent;
  let fixture: ComponentFixture<PostFormComponent>;
  let postService: PostService;

  // Create a fake service object with spies
  const postServiceSpy = jasmine.createSpyObj('PostService', [
    'savePost',
    'updatePost'
  ]);

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [BrowserAnimationsModule, SharedModule],
      declarations: [PostFormComponent],
      // provide the component-under-test and dependent service
      providers: [
        //   { provide: ComponentFixtureAutoDetect, useValue: true },
        { provide: PostService, useValue: postServiceSpy }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    postService = TestBed.get(PostService);
    fixture = TestBed.createComponent(PostFormComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should contain "save" button', () => {
    fixture.detectChanges();
    const buttonElement: HTMLElement = fixture.nativeElement;
    console.log('text content:' + buttonElement.textContent);
    expect(buttonElement.textContent).toContain('save');
  });

  it('should have <button> with "save"', () => {
    fixture.detectChanges();
    const buttonElement: HTMLElement = fixture.nativeElement;
    const p = buttonElement.querySelector('button');
    expect(p.textContent).toContain('save');
  });

  it('should find the <button> with fixture.debugElement.nativeElement)', () => {
    fixture.detectChanges();
    const compDe: DebugElement = fixture.debugElement;
    const compEl: HTMLElement = compDe.nativeElement;
    const p = compEl.querySelector('button');
    expect(p.textContent).toContain('save');
  });

  it('should find the <button> with fixture.debugElement.query(By.css)', () => {
    fixture.detectChanges();
    const compDe: DebugElement = fixture.debugElement;
    const buttonDe = compDe.query(By.css('button'));
    const btn: HTMLElement = buttonDe.nativeElement;
    expect(btn.textContent).toContain('save');
  });

  it('should raise `saved` event when the form is submitted (triggerEventHandler)', fakeAsync(() => {
    // trigger initial data binding
    const formData = { title: 'Test title', content: 'Test content' };
    // const savePostSpy: jasmine.Spy = postServiceSpy.savePost
    //   .withArgs(formData)
    //   .and.returnValue(of({}));
    // expect(savePostSpy).toBeDefined();
    postServiceSpy.savePost.withArgs(formData).and.returnValue(of({}));
    let saved = false;
    fixture.detectChanges();

    // Make the spy return a synchronous Observable with the test data
    component.post = formData;
    component.saved.subscribe((data: boolean) => (saved = data));
    const compDe: DebugElement = fixture.debugElement;
    // compDe.triggerEventHandler('submit', null);
    component.submit();
    tick();
    fixture.detectChanges();

    expect(saved).toBeTruthy();
    expect(postServiceSpy.savePost.calls.count()).toBe(1, 'savePost called');
  }));

  // it('should run timeout callback with delay after call tick with millis', fakeAsync(() => {
  //   let called = false;
  //   setTimeout(() => {
  //     called = true;
  //   }, 100);
  //   tick(100);
  //   expect(called).toBe(true);
  // }));

  // it('should get Date diff correctly in fakeAsync', fakeAsync(() => {
  //   const start = Date.now();
  //   tick(100);
  //   const end = Date.now();
  //   expect(end - start).toBe(100);
  // }));
});
