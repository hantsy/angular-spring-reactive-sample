import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { LoadGuard } from './core/load-guard';

const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'post', loadChildren: () => import('./post/post.module').then((post) => post.PostModule) },
  { path: 'user', loadChildren: () => import('./user/user.module').then((user) => user.UserModule) },
  {
    path: 'auth',
    // loadChildren: './auth/auth.module#AuthModule',
    loadChildren: () => import('./auth/auth.module').then((auth) => auth.AuthModule),
    canLoad: [LoadGuard],
  },
  // { path: '**', component:PageNotFoundComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
