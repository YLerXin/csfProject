import { Component, inject, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { User } from './model/AuthRequest';
import { loginSuccess } from './utils/auth.actions';
import { WebSocketDealService } from './service/WebSocketDealService';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  standalone: false,
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit{
  title = 'project';

  private store = inject(Store);
  private wsDealService = inject(WebSocketDealService);

  ngOnInit(): void {
    const savedUser = localStorage.getItem('authUser');
    if (savedUser) {
      const userData: User = JSON.parse(savedUser);
      this.store.dispatch(loginSuccess({ user: userData }));
    }
    this.wsDealService.connect();
  }
}
