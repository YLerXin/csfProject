import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { ProductService } from '../service/ProductService';
import { DealService } from '../service/DealService';
import { selectUserId } from '../utils/auth.selectors';
import { Deal, Product } from '../model/Products';

@Component({
  selector: 'app-start-barter',
  standalone: false,
  templateUrl: './start-barter.component.html',
  styleUrl: './start-barter.component.css'
})
export class StartBarterComponent implements OnInit {

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private store = inject(Store);
  private productSvc = inject(ProductService);
  private dealSvc = inject(DealService);

  productId = '';
  currentUserId = '';
  error?: string;
  loading = false;

  ngOnInit(): void {
    this.productId = this.route.snapshot.paramMap.get('productId') || '';

    this.store.select(selectUserId).subscribe(uid => {
      this.currentUserId = uid || '';
    });

    if (!this.productId) {
      this.error = 'No productId given.';
      return;
    }

    this.loading = true;
    this.productSvc.getProductById(this.productId).subscribe({
      next: (product) => {
        if (!product) {
          this.error = 'Product not found.';
          this.loading = false;
          return;
        }
        this.initiateNewDeal(product);
      },
      error: () => {
        this.error = 'Failed to load product.';
        this.loading = false;
      }
    });
  }

  private initiateNewDeal(ownerProduct: Product) {
    if (!this.currentUserId) {
      this.error = 'You must be logged in to start a barter.';
      this.loading = false;
      return;
    }

     if (ownerProduct.userId === this.currentUserId) {
       this.error = 'Cannot barter with your own product.';
       this.loading = false;
       return;
     }

    const newDeal: Deal = {
      initiatorId: this.currentUserId,
      ownerId: ownerProduct.userId,
      initiatorAccepted: false,
      ownerAccepted: false,
      rejected: false,
      completed: false,
      initiatorItems: [],
      ownerItems: [ownerProduct], 
      messages: []
    };

    this.dealSvc.initiateDeal(newDeal).subscribe({
      next: (createdDeal) => {
        this.loading = false;
        this.router.navigate(['/barter', createdDeal.id]);
      },
      error: () => {
        this.error = 'Failed to initiate the deal.';
        this.loading = false;
      }
    });
  }
}