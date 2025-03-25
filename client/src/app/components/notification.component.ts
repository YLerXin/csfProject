import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { WebSocketDealService } from '../service/WebSocketDealService';
import { Subscription } from 'rxjs';
import { Deal } from '../model/Products';

@Component({
  selector: 'app-notification',
  standalone: false,
  templateUrl: './notification.component.html',
  styleUrl: './notification.component.css'
})
export class NotificationComponent implements OnInit, OnDestroy {

  private wsDealService = inject(WebSocketDealService);
  private subscription!: Subscription;

  notifications: string[] = [];

  ngOnInit(): void {
    this.subscription = this.wsDealService.dealUpdates$.subscribe({
      next: (deal) => this.handleDealUpdate(deal)
    });
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  handleDealUpdate(deal: Deal) {
    let statusMsg = '';

    if (deal.lastAction === 'MESSAGE') {
      return;
    }

    if (deal.rejected) {
      if (deal.lastUpdatedBy) {
        statusMsg = `Deal ${deal.id} has been REJECTED by ${deal.lastUpdatedBy}.`;
      } else {
        statusMsg = `Deal ${deal.id} has been REJECTED.`;
      }
    } 
    else if (deal.completed) {
      statusMsg = `Deal ${deal.id} has been COMPLETED.`;
    } 
    else {
      if (deal.lastUpdatedBy) {
        statusMsg = `Deal ${deal.id} was updated by ${deal.lastUpdatedBy}.`;
      } else {
        statusMsg = `Deal ${deal.id} was updated.`;
      }
    }

    this.notifications.unshift(statusMsg);
        if (this.notifications.length > 2) {
      this.notifications.pop();
    }
  }
  removeNotification(index: number) {
    this.notifications.splice(index, 1);
  }
}