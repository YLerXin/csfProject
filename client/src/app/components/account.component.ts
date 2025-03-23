import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService } from '../service/UserService';
import { Store } from '@ngrx/store';
import { selectUser, selectUserId } from '../utils/auth.selectors';
import { take } from 'rxjs';
import { MatChipInputEvent } from '@angular/material/chips';
import { loginSuccess } from '../utils/auth.actions';


@Component({
  selector: 'app-account',
  standalone: false,
  templateUrl: './account.component.html',
  styleUrl: './account.component.css'
})
export class AccountComponent implements OnInit{
protected form!:FormGroup;
dataUri!:string

private router = inject(Router)
private fb = inject(FormBuilder)
private fileupload=inject(UserService)
private store = inject(Store)

//observables to use store
user$ = this.store.select(selectUser);
userId$ = this.store.select(selectUserId)

selectedFile?:File;
tagsControl = new FormControl<string[]>([], {nonNullable: true});

meetingLocationControl = new FormControl<string>('', {nonNullable: true});

ngOnInit(): void {
  this.createForm();
  this.loadUserTags();
  this.user$.pipe(take(1)).subscribe(user => {
    if (user?.preferredMeetingLocation) {
      this.meetingLocationControl.setValue(user.preferredMeetingLocation);
    }
  });
}
createForm(){
this.form=this.fb.group({

});
}

onFileChange(event:any){
  const fileList:FileList=event.target.files;
  if(fileList&& fileList.length>0){
    this.selectedFile=fileList[0];
    const reader = new FileReader();
    reader.onload=()=>{
      this.dataUri=reader.result as string
    };
    reader.readAsDataURL(this.selectedFile);
  }
}
  uploadProfilePic() {
    if (!this.selectedFile) {
      console.error("No File Selected");
      return;
    }
  
    this.userId$.pipe(take(1)).subscribe((userId) => {
      if (!userId) {
        console.error('No userId found in store');
        return;
      }
  
      this.fileupload.uploadProfilePicture(userId, this.selectedFile!)
        .subscribe({
          next: (res) => {
            console.log('Success', res.userId);
  
            const updatedUser = {
              userId: res.userId,
              username: res.username,
              email: res.email,
              profilePicUrl: res.profilePicUrl,
              preferredMeetingLocation:res.preferredMeetingLocation

            };
  
            this.store.dispatch(loginSuccess({ user: updatedUser }));
            localStorage.setItem('authUser', JSON.stringify(updatedUser));
          },
          error: (err) => {
            console.error('Upload error:', err);
          }
        });
    });
  }
  

addTag(event: MatChipInputEvent) {
  const inputValue = (event.value || '').trim();
  if (inputValue) {
    const current = this.tagsControl.value;
    this.tagsControl.setValue([...current, inputValue]);
  }
  event.chipInput!.clear();
}

removeTag(tag: string) {
  const updated = this.tagsControl.value.filter(t => t !== tag);
  this.tagsControl.setValue(updated);
}

saveTags() {
  this.userId$.pipe(take(1)).subscribe(userId => {
    if (!userId) {
      console.error('No userId found in store');
      return;
    }
    const tags = this.tagsControl.value;
    this.fileupload.setTags(userId, tags).subscribe({
      next: (res) => {
        console.log('Tags saved:', res);
        this.loadUserTags();
      },
      error: (err) => {
        console.error('Error saving tags:', err);
      }
    });
  });
}
loadUserTags() {
  this.userId$.pipe(take(1)).subscribe(userId => {
    if (!userId) {
      console.error('No userId found in store');
      return;
    }
    this.fileupload.getTags(userId).subscribe({
      next: (tagsFromDB) => {
        this.tagsControl.setValue(tagsFromDB || []);
        console.log('Loaded tags from DB:', tagsFromDB);
      },
      error: (err) => {
        console.error('Error loading tags:', err);
      }
    });
  });
}
savePreferredLocation() {
  this.userId$.pipe(take(1)).subscribe((userId) => {
    if (!userId) {
      console.error('No userId found in store');
      return;
    }
    const newLoc = this.meetingLocationControl.value;
    
    this.fileupload.updateMeetingLocation(userId, newLoc)
      .subscribe({
        next: (res) => {
          console.log('Meeting location updated:', res);
          this.store.select(selectUser).pipe(take(1)).subscribe((currentUser) => {
            if (!currentUser) {
              return;
            }
            const updatedUser = {
              ...currentUser,
              preferredMeetingLocation: newLoc
            };
            this.store.dispatch(loginSuccess({ user: updatedUser }));
  
            localStorage.setItem('authUser', JSON.stringify(updatedUser));
          });
        },
        error: (err) => {
          console.error('Error updating location:', err);
        }
      });
  });
}
}
