import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { LoginService } from '../service/login.service';
import { Router } from '@angular/router';
import { UserService } from '../service/UserService';
import { SignupRequest, SignupResponse } from '../model/AuthRequest';

  @Component({
    selector: 'app-signup',
    standalone:false,
    templateUrl: './signup.component.html',
    styleUrls: ['./signup.component.css']
  })
  export class SignupComponent implements OnInit {
  
    signupForm!: FormGroup;
    tagInput = '';

    interestTags: string[] = [];
    newTag: string = '';
  
    selectedFile?: File;
  
    message = '';
    error = false;
    usernameChecked = false;
    usernameAvailable = false;
  
    private fb = inject(FormBuilder);
    private loginSvc = inject(LoginService);
    private userSvc = inject(UserService);
    private router = inject(Router);
  
    ngOnInit(): void {
      this.signupForm = this.fb.group({
        username: ['', Validators.required],
        email: this.fb.control('',[Validators.required,Validators.email]),
        password: this.fb.control('',[Validators.required]),
      });
    }

      onSignup() {
        if (this.signupForm.invalid) {
          this.signupForm.markAllAsTouched();
          return;
        }
    
        const { username, email, password } = this.signupForm.value;
        const req: SignupRequest = { username, email, password };
    
        this.loginSvc.signupExtended(req).subscribe({
          next: (res: SignupResponse) => {
            this.error = false;
            this.message = 'Signup successful: ' + res.message;
            if (res.userId) {
              this.userSvc.createStripeAccount(res.userId).subscribe({
                next: (stripeRes) => {
                  window.location.href = stripeRes.onboardingUrl;
                },
                error: (err2) => {
                  console.error('Stripe account creation error', err2);
                  this.error = true;
                  this.message = 'User created, but failed to create Stripe account.';
                }
              });
            } else {
              this.error = true;
              this.message = 'Sign up success, but no userId returned.';
            }
          },
          error: (err) => {
            this.error = true;
            this.message = err.error?.message || 'Sign up failed';
          }
        });
      }

    checkAval() {
      const username = this.signupForm.get('username')?.value || '';
      if (!username.trim()) {
        this.error = true;
        this.message = 'Please enter a username first';
        return;
      }
  
      this.usernameChecked = true;
  
      this.userSvc.checkUsername(username).subscribe({
        next: (exists: boolean) => {
          this.error = false;
          if (exists) {
            this.usernameAvailable = false;
            this.message = `Username "${username}" is already taken.`;
          } else {
            this.usernameAvailable = true;
            this.message = `Username "${username}" is available!`;
          }
        },
        error: (err) => {
          this.error = true;
          this.usernameAvailable = false;
          this.message = `Error checking username: ${err.message || err}`;
        }
      });
    }

    
  }