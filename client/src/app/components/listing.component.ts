import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../service/ProductService';
import { UserService } from '../service/UserService';
import { Store } from '@ngrx/store';
import { selectUserId } from '../utils/auth.selectors';

@Component({
  selector: 'app-listing',
  standalone: false,
  templateUrl: './listing.component.html',
  styleUrl: './listing.component.css'
})
export class ListingComponent implements OnInit {

  product: any;
  productUser: any;
  error: string | null = null;
  loading = false;

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private productSvc = inject(ProductService);
  private userSvc = inject(UserService);
  private store = inject(Store);

  currentUserId?: string;
  


  ngOnInit() {
    this.store.select(selectUserId).subscribe(uid => {
      this.currentUserId = uid || '';
    });
    const productId = this.route.snapshot.paramMap.get('id');
    console.log('Listing got productId=', productId);

    if (!productId) {
      this.error = 'No productId found in route.';
      return;
    }
    this.loading = true;

    this.productSvc.getProductById(productId).subscribe({
      next: (p) => {
        this.product = p;
        this.loadProductUser(p.userId);
      },
      error: () => {
        this.error = 'Failed to load product details';
        this.loading = false;
      }
    });
  }

  loadProductUser(userId: string) {
    this.userSvc.getPublicProfile(userId).subscribe({
      next: (u) => {
        this.productUser = u;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load product owner';
        this.loading = false;
      }
    });
  }

  goToProfile(userId: string) {
    this.router.navigate(['/profile', userId]);
  }

  goToBarter(productId: string) {
    this.router.navigate(['/start-barter', productId]);
  }
}