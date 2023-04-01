import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';
import { distinctUntilChanged, map } from 'rxjs/operators';

import { environment } from '../../environments/environment';
import { Credentials } from './credentials.model';
import { TokenStorage } from './token-storage';
import { User } from './user.model';

const apiUrl = environment.baseApiUrl;

interface State {
  user: User | null;
  authenticated: boolean;
}

const defaultState: State = {
  user: null,
  authenticated: false,
};

const store = new BehaviorSubject<State>(defaultState);

class Store {
  private _store = store;
  changes = this._store.asObservable().pipe(distinctUntilChanged());

  setState(state: State) {
    console.log('update user state:' + JSON.stringify(state));
    this._store.next(state);
  }

  getState(): State {
    return this._store.value;
  }

  updateState(data: State) {
    this._store.next(Object.assign({}, this.getState(), data));
  }

  purge() {
    this._store.next(defaultState);
  }
}

@Injectable()
export class AuthService {
  private store: Store = new Store();

  constructor(private http: HttpClient, private jwt: TokenStorage, private router: Router) {}

  signin(credentials: Credentials) {
    return this.http
      .post<User>(`${apiUrl}/login`, credentials, {
        observe: 'response',
        responseType: 'json',
      })
      .subscribe({
        next: (data) => {
          if (data.status === 200) {
            const token = data.headers.get('X-AUTH-TOKEN');
            console.log('login successfully, token: ' + token);
            if (token != null) this.jwt.save(token);
            this.store.setState({
              user: data.body,
              authenticated: Boolean(data),
            });
          } else {
            console.log('signin failed');
          }
        },
        error: (err) => {
          console.log(err);
        },
        complete: () => {
          console.log('complete');
        },
      });
  }

  restoreState() {
    const token = this.jwt.get();
    if (token != null) {
      const headers = new HttpHeaders({
        'X-AUTH-TOKEN': token,
      });
      this.http.get<User>(`${apiUrl}/me`, { headers: headers, observe: 'response', responseType: 'json' }).subscribe({
        next: (data) => {
          if (data.status === 200) {
            this.store.setState({
              user: data.body,
              authenticated: true,
            });
          } else {
            console.log('attemp restore state from token failed');
            this.jwt.destroy();
          }
        },
      });
    }
  }

  signout() {
    this.http.get<any>(`${apiUrl}/logout`).subscribe({
      next: (data) => {
        // reset the initial values
        this.jwt.destroy();
        this.store.purge();
      },
    });
  }

  currentUser(): Observable<User | null> {
    return this.store.changes.pipe(map((data) => data.user));
  }

  isAuthenticated(): Observable<boolean> {
    return this.store.changes.pipe(map((data) => data.authenticated));
  }
}
