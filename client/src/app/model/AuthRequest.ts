export interface AuthRequest {
    email: string;
    password: string;
  }

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
}

export interface SignupResponse {
  message: string;  
  userId: string; 
}

export interface User {
  userId: string;
  username: string;
  email: string;
  profilePicUrl: string;
  preferredMeetingLocation:string;
}
export interface UpdatedUserResponse {
  userId: string;
  username: string;
  email: string;
  profilePicUrl: string;
  preferredMeetingLocation:string;

}
