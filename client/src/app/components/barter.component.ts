import { AfterViewInit, Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';

import { selectUserId } from '../utils/auth.selectors';
import { DealService } from '../service/DealService';
import { Deal, DealMessage, Product } from '../model/Products';
import { loadStripe, Stripe, StripeCardElement } from '@stripe/stripe-js';
import { lastValueFrom } from 'rxjs';


@Component({
  selector: 'app-barter',
  templateUrl: './barter.component.html',
  standalone: false,
  styleUrls: ['./barter.component.css']
})
export class BarterComponent implements OnInit,AfterViewInit  {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private store = inject(Store);
  private dealSvc = inject(DealService);
  private fb = inject(FormBuilder);

  stripe: Stripe | null = null;
  cardElement?: StripeCardElement;
  showPaymentForm = false;


  dealId = '';
  currentUserId = '';
  deal?: Deal;
  error?: string;

  finalPriceDifference = 0; 
  messageForm!: FormGroup;
  meetingForm!: FormGroup;

  

  async ngOnInit(): Promise<void> {
    this.messageForm = this.fb.group({
      text: ['', Validators.required]
    });
    this.meetingForm = this.fb.group({
      location: ['', Validators.required],
      dateTime: ['', Validators.required] 
    });

    this.dealId = this.route.snapshot.paramMap.get('dealId') || '';

    this.store.select(selectUserId).subscribe(uid => {
      this.currentUserId = uid || '';
    });

    this.stripe = await loadStripe('pk_test_51R0vk4PijiMleOsRSQ0D0dkVRnC8QV5VvckShe0VjhSx28aVV8uHL4lmFQVv7IffKlhrLKV0iNKQ0WsVPnk4cjNa00RQq8Meaw');


    this.dealSvc.getDeal(this.dealId).subscribe({
      next: (deal) => {
        if (!deal) {
          this.error = 'No deal found.';
          return;
        }
        this.deal = deal;
        this.computeFinalDifference();
      },
      error: () => {
        this.error = 'Failed to load deal.';
      }
    });
  }

  ngAfterViewInit(): void {
    if (!this.stripe) {
      return;
    }
    const elements = this.stripe.elements();
    this.cardElement = elements.create('card');

    const cardDiv = document.getElementById('card-element');
    if (!cardDiv) {
      this.error = '#card-element not found in DOM';
      return;
    }
    this.cardElement.mount(cardDiv);
  }
  
  private computeFinalDifference() {
    if (!this.deal) {
      this.finalPriceDifference = 0;
      return;
    }
    const ownerSum = this.sumPrices(this.deal.ownerItems);
    const initiatorSum = this.sumPrices(this.deal.initiatorItems);

    let difference = ownerSum - initiatorSum;
    if (difference < 0) {
      difference = 0; 
    }
    this.finalPriceDifference = difference;
  }

  private sumPrices(items: Product[]): number {
    if (!items) return 0;
    return items.reduce((acc, p) => {
      const val = p.price ? parseFloat(p.price.toString()) : 0;
      return acc + val;
    }, 0);
  }

  refreshDeal() {
    if (!this.dealId) return;
    this.dealSvc.getDeal(this.dealId).subscribe({
      next: (updatedDeal) => {
        this.deal = updatedDeal;
        this.computeFinalDifference();
      },
      error: () => {
        this.error = 'Failed to refresh deal.';
      }
    });
  }

  sendMessage() {
    if (!this.deal || !this.deal.id) return;
    if (this.messageForm.invalid) return;

    const msgText = this.messageForm.value.text;
    const msg: DealMessage = {
      senderId: this.currentUserId,
      text: msgText
    };
    this.dealSvc.postMessage(this.deal.id, msg).subscribe({
      next: (updatedDeal) => {
        this.deal = updatedDeal;
        this.computeFinalDifference();
        this.messageForm.reset();
      },
      error: () => {
        this.error = 'Failed to send message';
      }
    });
  }

  acceptDeal() {
    if (!this.deal?.id) return;
    this.dealSvc.acceptDeal(this.deal.id, this.currentUserId).subscribe({
      next: (updatedDeal) => {
        this.deal = updatedDeal;
        this.computeFinalDifference();
        this.router.navigate(['/history']);
      },
      error: () => {
        this.error = 'Failed to accept deal';
      }
    });
  }

  rejectDeal() {
    if (!this.deal?.id) return;
    this.dealSvc.rejectDeal(this.deal.id, this.currentUserId).subscribe({
      next: (updatedDeal) => {
        this.deal = updatedDeal;
        this.computeFinalDifference();
        this.router.navigate(['/history']);

      },
      error: () => {
        this.error = 'Failed to reject deal';
      }
    });
  }

  goPickItems(role: 'owner' | 'initiator') {
    if (!this.deal?.id) {
      this.error = 'No deal found to pick items.';
      return;
    }
    this.router.navigate(['/deal', this.deal.id, 'pick-items'], {
      queryParams: { role }
    });
  }
  updateMeeting() {
    if (!this.deal?.id) return;
    if (this.meetingForm.invalid) {
      this.error = 'Please fill location and date/time.';
      return;
    }
    const { location, dateTime } = this.meetingForm.value;
    this.dealSvc.updateMeeting(this.deal.id, location, dateTime).subscribe({
      next: (updatedDeal) => {
        this.deal = updatedDeal;
      },
      error: () => {
        this.error = 'Failed to update meeting.';
      }
    });
  }

  togglePaymentForm() {
    this.showPaymentForm = !this.showPaymentForm;
    if (this.showPaymentForm) {
      this.initStripeCardElement();
    }
  }

  initStripeCardElement() {
    if (!this.stripe) {
/*       this.error = 'Stripe not initialized';
 */      return;
    }
    const elements = this.stripe.elements();
    this.cardElement = elements.create('card');
    this.cardElement.mount('#card-element');
  }

  async payDeal() {
    if (!this.stripe || !this.cardElement) {
      this.error = 'Stripe or card element not ready.';
      return;
    }
    if (!this.deal?.id) {
      this.error = 'No Deal ID found.';
      return;
    }

    try {
      const secretResp = await lastValueFrom(this.dealSvc.getPaymentIntentSecret(this.deal.id));
      const clientSecret = secretResp.clientSecret;

      const result = await this.stripe.confirmCardPayment(clientSecret, {
        payment_method: {
          card: this.cardElement
        }
      });

      if (result.error) {
        this.error = result.error.message ?? 'Payment failed';
        return;
      }

      if (result.paymentIntent && result.paymentIntent.status === 'succeeded') {
        const updatedDeal = await lastValueFrom(this.dealSvc.confirmPaid(this.deal.id));
        this.deal = updatedDeal;
        this.showPaymentForm = false;
      }

    } catch (err: any) {
      this.error = err.message || 'Error during payment.';
    }
  }

  itemsPerPage = 3; 
  ownerPageIndex = 0; 
  initPageIndex = 0;

  getOwnerPageItems(): Product[] {
    if (!this.deal) return [];
    const start = this.ownerPageIndex * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    return this.deal.ownerItems.slice(start, end);
  }

  ownerHasPrev(): boolean {
    return this.ownerPageIndex > 0;
  }
  ownerHasNext(): boolean {
    return this.deal ? ((this.ownerPageIndex + 1) * this.itemsPerPage) < this.deal.ownerItems.length : false;
  }
  ownerNextPage() {
    if (this.ownerHasNext()) {
      this.ownerPageIndex++;
    }
  }
  ownerPrevPage() {
    if (this.ownerHasPrev()) {
      this.ownerPageIndex--;
    }
  }

  getInitiatorPageItems(): Product[] {
    if (!this.deal) return [];
    const start = this.initPageIndex * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    return this.deal.initiatorItems.slice(start, end);
  }

  initHasPrev(): boolean {
    return this.initPageIndex > 0;
  }
  initHasNext(): boolean {
    return this.deal ? ((this.initPageIndex + 1) * this.itemsPerPage) < this.deal.initiatorItems.length : false;
  }
  initNextPage() {
    if (this.initHasNext()) {
      this.initPageIndex++;
    }
  }
  initPrevPage() {
    if (this.initHasPrev()) {
      this.initPageIndex--;
    }
  }
}