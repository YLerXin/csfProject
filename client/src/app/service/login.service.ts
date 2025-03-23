import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { AuthRequest, SignupRequest, SignupResponse } from '../model/AuthRequest';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  private http = inject(HttpClient);

  login(email: string, password: string): Observable<any> {
    const body = { email, password };
    return this.http.post('/api/login', body);
  }

  signupExtended(req: SignupRequest): Observable<SignupResponse> {
    return this.http.post<SignupResponse>('/api/signup', req);
  }

  logout(): Observable<any> {
    return this.http.get('/api/logout', { responseType: 'text' });
  }
}