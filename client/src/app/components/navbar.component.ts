import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { LoginService } from '../service/login.service';
import { selectUser, selectUserName, selectUserPic } from '../utils/auth.selectors';
import { map, Observable, take } from 'rxjs';
import { User } from '../model/AuthRequest';
import { loginSuccess, logoutSuccess } from '../utils/auth.actions';
import { FormBuilder, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-navbar',
  standalone: false,
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnInit{

  private router = inject(Router)
  private store = inject(Store)
  private loginService = inject(LoginService);
  private fb = inject(FormBuilder)

  user$ = this.store.select(selectUser);
  userName$ = this.store.select(selectUserName);
  userPic$ = this.store.select(selectUserPic);
  isLoggedIn$!: Observable<boolean>;

  email = '';
  password = '';
  searchText = '';
  searchForm!: FormGroup;


  ngOnInit(): void {

    this.searchForm = this.fb.group({
      searchText: ''
    });

    this.isLoggedIn$ = this.user$.pipe(map(user => !!user));
  }

  goHome() {
    this.router.navigate(['/home']);
  }

  onSearch() {
    const text = this.searchForm.value.searchText?.trim() || '';
    if (!text) {
      this.router.navigate(['/home']);
      return;
    }
    this.router.navigate(['/home'], { queryParams: { q: text } });
  }

  goLogin() {
    this.router.navigate(['/login']);
  }

  goAccount() {
    this.router.navigate(['/account']);
  }

  goHistory() {
    this.router.navigate(['/history']);
  }

  onLogout() {

    this.store.dispatch(logoutSuccess());
    localStorage.removeItem('authUser');
    this.router.navigate(['/home']);

  }
  goCreateListing() {
    this.router.navigate(['/create-listing']);
  }
  goMyProfile() {
    this.user$.pipe(take(1)).subscribe(user => {
      if (!user) {
        return;
      }
      const userId = user.userId;
      this.router.navigate(['/profile', userId]);
    });
  }
}