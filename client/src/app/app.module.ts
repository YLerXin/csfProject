import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

import { AppComponent } from './app.component';
import { ReactiveFormsModule } from '@angular/forms';
import { LoginComponent } from './components/login.component';
import { provideRouter, RouterModule, Routes, withHashLocation } from '@angular/router';
import { NavbarComponent } from './components/navbar.component';
import { SignupComponent } from './components/signup.component';
import { UserService } from './service/UserService';
import { HomeComponent } from './components/home.component';
import { AccountComponent } from './components/account.component';
import { StoreModule } from '@ngrx/store';
import { authReducer } from './utils/auth.reducer';
import { MaterialModule } from './utils/material.module';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { HistoryComponent } from './components/history.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ProfileComponent } from './components/profile.component';
import { CreatelistingComponent } from './components/createlisting.component';
import { ProductService } from './service/ProductService';
import { ListingComponent } from './components/listing.component';
import { BarterComponent } from './components/barter.component';
import { DealService } from './service/DealService';
import { PickItemDialogComponent } from './components/pick-item-dialog-component.component';
import { StartBarterComponent } from './components/start-barter.component';
import { WebSocketDealService } from './service/WebSocketDealService';
import { NotificationComponent } from './components/notification.component';


const appRoutes:Routes = [
  {path:'',component:HomeComponent},
  { path: 'signup', component: SignupComponent },
  { path: 'home',component:HomeComponent},
  { path: 'account',component:AccountComponent},
  { path: 'profile/:userId',component:ProfileComponent},
  { path: 'create-listing',component:CreatelistingComponent},
  { path: 'listing/:id',component:ListingComponent},
  { path: 'start-barter/:productId', component: StartBarterComponent },
  { path: 'barter/:dealId',component:BarterComponent},
  { path: 'history',component:HistoryComponent},
  { path: 'login',component:LoginComponent},
  { path: 'deal/:dealId/pick-items', component: PickItemDialogComponent },


  { path: '**', redirectTo: '/', pathMatch: 'full' }
]

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    NavbarComponent,
    SignupComponent,
    HomeComponent,
    AccountComponent,
    HistoryComponent,
    ProfileComponent,
    CreatelistingComponent,
    ListingComponent,
    BarterComponent,
    PickItemDialogComponent,
    StartBarterComponent,
    NotificationComponent
  ],
  imports: [
    BrowserModule,
    MaterialModule,
    ReactiveFormsModule,
    RouterModule.forRoot(appRoutes),
    StoreModule.forRoot({auth: authReducer }),
    NgbModule
  ],
  providers: [provideHttpClient(withInterceptorsFromDi()),
    UserService,
    ProductService,
    DealService,
    WebSocketDealService,
    provideRouter(appRoutes, withHashLocation()),

    provideAnimationsAsync()
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
