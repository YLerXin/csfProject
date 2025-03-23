import { Component, inject, OnInit } from '@angular/core';
import { ProductService } from '../service/ProductService';
import { Store } from '@ngrx/store';
import { Deal, Product } from '../model/Products';
import { selectUserId } from '../utils/auth.selectors';
import { ActivatedRoute, Router } from '@angular/router';
import { DealService } from '../service/DealService';

@Component({
  selector: 'app-pick-item-dialog-component',
  standalone: false,
  templateUrl: './pick-item-dialog-component.component.html',
  styleUrl: './pick-item-dialog-component.component.css'
})
export class PickItemDialogComponent implements OnInit {

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private dealSvc = inject(DealService);
  private productSvc = inject(ProductService);

  dealId = '';
  role: 'owner' | 'initiator' = 'initiator'; 
  deal?: Deal;

  userItems: Product[] = [];
  selectedItems: Product[] = [];

  loading = false;
  error?: string;

  ngOnInit(): void {
    this.dealId = this.route.snapshot.paramMap.get('dealId') || '';
    const roleParam = this.route.snapshot.queryParamMap.get('role');
    if (roleParam === 'owner') {
      this.role = 'owner';
    }

    this.loading = true;
    this.dealSvc.getDeal(this.dealId).subscribe({
      next: (deal) => {
        if (!deal) {
          this.error = 'Deal not found.';
          this.loading = false;
          return;
        }
        this.deal = deal;
        this.loadUserItems();
      },
      error: () => {
        this.error = 'Failed to load deal.';
        this.loading = false;
      }
    });
  }

  loadUserItems() {
    if (!this.deal) {
      this.loading = false;
      return;
    }

    const userToLoad = (this.role === 'owner')
      ? this.deal.ownerId
      : this.deal.initiatorId;

    this.productSvc.getAllProducts().subscribe({
      next: (allProducts) => {
        let items = allProducts.filter(
          p => p.userId === userToLoad && p.availability
        );
        if (!this.deal) {
          this.loading = false;
          return;
        }
        const alreadyInDeal = (this.role === 'owner')
          ? (this.deal.ownerItems ?? [])
          : (this.deal.initiatorItems ?? []);

        items = items.filter(prod =>
          !alreadyInDeal.some(dealItem => dealItem.productId === prod.productId)
        );

        this.userItems = items;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load items.';
        this.loading = false;
      }
    });
  }

  isSelected(item: Product) {
    return this.selectedItems.some(i => i.productId === item.productId);
  }

  toggleSelection(item: Product) {
    if (this.isSelected(item)) {
      this.selectedItems = this.selectedItems.filter(
        i => i.productId !== item.productId
      );
    } else {
      this.selectedItems.push(item);
    }
  }

  confirmPick() {
    if (!this.deal?.id) return;
    if (this.role === 'owner') {
      const existingOwner = this.deal.ownerItems ?? [];
      const updatedOwnerItems = [...existingOwner, ...this.selectedItems];
      const existingInitiator = this.deal.initiatorItems ?? [];

      this.dealSvc.updateDealItems(this.deal.id, existingInitiator, updatedOwnerItems)
        .subscribe({
          next: () => {
            this.router.navigate(['/barter', this.dealId]);
          },
          error: () => {
            this.error = 'Failed to update deal with selected items.';
          }
        });
    } else {
      const existingInitiator = this.deal.initiatorItems ?? [];
      const updatedInitiatorItems = [...existingInitiator, ...this.selectedItems];
      const existingOwner = this.deal.ownerItems ?? [];

      this.dealSvc.updateDealItems(this.deal.id, updatedInitiatorItems, existingOwner)
        .subscribe({
          next: () => {
            this.router.navigate(['/barter', this.dealId]);
          },
          error: () => {
            this.error = 'Failed to update deal with selected items.';
          }
        });
    }
  }

  goBack() {
    this.router.navigate(['/barter', this.dealId]);
  }
}