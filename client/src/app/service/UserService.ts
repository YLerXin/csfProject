import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { UpdatedUserResponse } from "../model/AuthRequest";

@Injectable()
export class UserService {
    private http = inject(HttpClient);
  
      uploadProfilePicture(userId: string, file: File) {
        const formData = new FormData();
        formData.append('file', file);
      
        return this.http.post<UpdatedUserResponse>(`/api/user/${userId}/profile-picture`, formData);
      }

    checkUsername(username: string): Observable<boolean> {
      return this.http.get<boolean>('/api/user/check-username', {
        params: { username }
      });
    }

    setTags(userId: string, tags: string[]): Observable<any> {
      return this.http.put(`/api/user/${userId}/tags`, tags);
    }
    getTags(userId: string): Observable<string[]> {
      return this.http.get<string[]>(`/api/user/${userId}/tags`);
    }
    updateMeetingLocation(userId: string, location: string): Observable<any> {
      return this.http.patch(`/api/user/${userId}/meetingLocation`, { location });
    }
    getPublicProfile(userId: string): Observable<any> {
      return this.http.get<any>(`/api/public/user/${userId}`);
    }

    createStripeAccount(userId: string): Observable<{onboardingUrl: string}> {
      return this.http.post<{onboardingUrl: string}>(`/api/user/${userId}/create-stripe-account`, {});
    }
  }