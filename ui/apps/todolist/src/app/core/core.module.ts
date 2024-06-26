import { NgModule, Optional, SkipSelf } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { AuthGuard } from './auth-guard';
import { AuthService } from './auth.service';
import { TokenStorage } from './token-storage';
import { RouterModule } from '@angular/router';
import { AuthInterceptor } from './auth-inteceptor';
import { LoadGuard } from './load-guard';
import { TokenInterceptor } from './token-inteceptor';

@NgModule({ declarations: [], imports: [CommonModule, RouterModule], providers: [
        AuthGuard,
        LoadGuard,
        AuthService,
        TokenStorage,
        {
            provide: HTTP_INTERCEPTORS,
            useClass: TokenInterceptor,
            multi: true,
        },
        {
            provide: HTTP_INTERCEPTORS,
            useClass: AuthInterceptor,
            multi: true,
        },
        provideHttpClient(withInterceptorsFromDi()),
    ] })
export class CoreModule {
  // Prevent reimport of the CoreModule
  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    if (parentModule) {
      throw new Error('CoreModule is already loaded. Import it in the AppModule only');
    }
  }
}
