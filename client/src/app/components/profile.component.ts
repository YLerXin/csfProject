import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../service/UserService';
import { ProductService } from '../service/ProductService';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { Store } from '@ngrx/store';
import { selectUserId } from '../utils/auth.selectors';

@Component({
  selector: 'app-profile',
  standalone: false,
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit{
  private route = inject(ActivatedRoute);
  private userSvc = inject(UserService);
  private productSvc = inject(ProductService);
  private router = inject(Router);


  @ViewChild(MatPaginator) paginator!: MatPaginator;

  user: any;
  error: string | null = null;
  loading = false;

  userListings: any[] = [];
  displayedListings: any[] = [];
  pageSize = 3;
  pageIndex = 0;
  pageSizeOptions = [3, 6, 9];


  private store = inject(Store);

  currentUserId: string = '';


  ngOnInit(): void {
    this.store.select(selectUserId).subscribe(uid => {
      if (uid) {
        this.currentUserId = uid;
      }
    });
    const userId = this.route.snapshot.paramMap.get('userId');
    if (!userId) {
      this.error = 'No userId provided in route.';
      return;
    }

    this.loading = true;
    this.userSvc.getPublicProfile(userId).subscribe({
      next: (data) => {
        console.log('Profile data received:', data);

        this.user = data; 
        this.loadUserListings(userId);
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to load user profile';
        this.loading = false;
      }
    });
  }

  loadUserListings(userId: string) {
    this.productSvc.getAllProducts().subscribe({
      next: listings => {
        this.userListings = listings.filter((p: any) => p.userId === userId && p.availability);
        this.updateDisplayedListings();
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load user listings';
        this.loading = false;
      }
    });
  }

  updateDisplayedListings() {
    const startIndex = this.pageIndex * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.displayedListings = this.userListings.slice(startIndex, endIndex);
  }

  onPaginateChange(event: PageEvent) {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.updateDisplayedListings();
  }
  onBarter(productId: string) {
    this.router.navigate(['/start-barter', productId]);
  }
}
