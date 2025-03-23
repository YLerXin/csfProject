import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { LoginService } from '../service/login.service';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { loginSuccess, logoutSuccess } from '../utils/auth.actions';
import { User } from '../model/AuthRequest';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit{

  message = '';   
  error = false;  
  protected loginForm!:FormGroup
  private fb = inject(FormBuilder);
  private loginSvc = inject(LoginService);
  private router = inject(Router)
  private store = inject(Store)

ngOnInit(): void {
  this.loginForm = this.fb.group({
    email: this.fb.control<string>(''),
    password: this.fb.control<string>('')
  })
}
  onLogin() {
    const { email, password } = this.loginForm.value;
    this.loginSvc.login(email, password)
      .subscribe({
        next: (res) => {
          const userData: User = {
            userId: res.user.userId,
            username: res.user.username,
            email: res.user.email,
            profilePicUrl: res.user.profilePicUrl,
            preferredMeetingLocation:res.user.preferredMeetingLocation
          };
  
          this.error = false;
          this.message = res; 

          this.store.dispatch(loginSuccess({user:userData}))
          localStorage.setItem('authUser', JSON.stringify(userData));

          this.router.navigate(['/home']);
        },
        error: (err) => {
          this.error = true;
          this.message = err.error || 'Login failed';
        }
      });
  }

  goToSignup() {
    this.router.navigate(['/signup']);
  }

  onLogout() {
    this.loginSvc.logout()
      .subscribe({
        next: (res) => {
          this.error = false;
          this.message = res; 
          this.store.dispatch(logoutSuccess())
          localStorage.removeItem('authUser');

        },
        error: (err) => {
          this.error = true;
          this.message = err.error || 'Logout failed';
        }
      });
  }
}
