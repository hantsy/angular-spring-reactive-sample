import { TestBed, inject } from '@angular/core/testing';

import { AuthInterceptor } from './auth-inteceptor';

describe('AuthIntecepterService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AuthInterceptor]
    });
  });

  it('should be created', inject([AuthInterceptor], (service: AuthInterceptor) => {
    expect(service).toBeTruthy();
  }));
});
