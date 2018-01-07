import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpSentEvent,
  HttpHeaderResponse,
  HttpProgressEvent,
  HttpResponse,
  HttpUserEvent,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { TokenStorage } from './token-storage';
import { Router } from '@angular/router';

const TOKEN_HEADER_KEY = 'X-AUTH-TOKEN';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private token: TokenStorage, private router: Router) { }

  intercept(req: HttpRequest<any>, next: HttpHandler):
    Observable<HttpSentEvent | HttpHeaderResponse | HttpProgressEvent | HttpResponse<any> | HttpUserEvent<any>> {

    if (this.token.get()) {
      console.log('set token in header ::' + this.token.get());
      req.headers.set(TOKEN_HEADER_KEY, this.token.get());
    }

    return next.handle(req).do(
      (event: HttpEvent<any>) => {
        if (event instanceof HttpResponse) {
          const token = event.headers.get(TOKEN_HEADER_KEY);
          if (token) {
            console.log('saving token ::' + token);
            this.token.save(token);
          }
        }
      },
      (err: any) => {
        if (err instanceof HttpErrorResponse) {
          console.log(err);
          console.log('req url :: ' + req.url);
          if (!req.url.endsWith('/auth/user') && err.status === 401) {
            this.router.navigate(['', 'auth', 'signin']);
          }
        }
      }
    );
  }

}
