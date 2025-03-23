
import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { ProductService } from '../service/ProductService';
import { UserService } from '../service/UserService';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: false,
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  private productSvc = inject(ProductService);
  private userSvc = inject(UserService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  listings: any[] = [];
  displayedListings: any[] = [];
  pageSize = 8;
  pageIndex = 0;
  pageSizeOptions = [8, 16, 24];
  loading = false;
  error: string | null = null;
  userMap: Record<string, any> = {};

  ngOnInit(): void {
    console.log('HomeComponent ngOnInit called');

    this.route.queryParamMap.subscribe(params => {
      const searchText = params.get('q')?.trim() || '';
      console.log('Detected search param:', searchText);

      this.loading = true;
      if (searchText) {
        this.searchAvailable(searchText);
      } else {
        this.getAllProducts();
      }
    });
  }

  async loadUserInfo(userId: string) {
    return new Promise<void>((resolve) => {
      this.userSvc.getPublicProfile(userId).subscribe({
        next: (u) => {
          this.userMap[userId] = u;
          resolve();
        },
        error: () => {
          this.userMap[userId] = {};
          resolve();
        }
      });
    });
  }

  updateDisplayedListings() {
    const startIndex = this.pageIndex * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.displayedListings = this.listings.slice(startIndex, endIndex);
  }

  onPaginateChange(event: PageEvent) {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.updateDisplayedListings();
  }

  goToProfile(userId: string) {
    this.router.navigate(['/profile', userId]);
  }

  goToListing(productId: string) {
    this.router.navigate(['/listing', productId]);
  }

  getAllProducts() {
    console.log('Calling getAllProducts');
    this.productSvc.getAllProducts().subscribe({
      next: async (products) => {
        const filtered = products.filter(p => p.availability);

        const uniqueUserIds = Array.from(new Set(filtered.map(p => p.userId)));
        for (const uid of uniqueUserIds) {
          await this.loadUserInfo(uid);
        }
        this.listings = filtered.map(p => ({
          ...p,
          username: this.userMap[p.userId]?.username
        }));
        this.pageIndex = 0;
        this.updateDisplayedListings();
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load listings';
        this.loading = false;
      }
    });
  }

  searchAvailable(searchText: string) {
    console.log('Calling /api/products/searchAvail with q=', searchText);
    this.productSvc.searchAvailable(searchText).subscribe({
      next: async (found) => {
        console.log('searchAvail returned:', found);
        const uniqueUserIds = Array.from(new Set(found.map(p => p.userId)));
        for (const uid of uniqueUserIds) {
          await this.loadUserInfo(uid);
        }
        this.listings = found.map(p => ({
          ...p,
          username: this.userMap[p.userId]?.username
        }));
        this.pageIndex = 0;
        this.updateDisplayedListings();
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to search with query: ' + searchText;
        this.loading = false;
      }
    });
  }
}
