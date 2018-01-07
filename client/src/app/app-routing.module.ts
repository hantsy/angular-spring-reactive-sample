import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

//import { AppRoutingComponent } from './name.component';

const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'post', loadChildren: './post/post.module#PostModule' },
  { path: 'user', loadChildren: './user/user.module#UserModule' },
  { path: 'auth', loadChildren: './auth/auth.module#AuthModule' }
  // { path: '**', component:PageNotFoundComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule { }

//export const routedComponents = [AppRoutingComponent];
