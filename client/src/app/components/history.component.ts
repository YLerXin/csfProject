import { Component, inject, OnInit } from '@angular/core';
import { DealService } from '../service/DealService';
import { Store } from '@ngrx/store';
import { Deal } from '../model/Products';
import { selectUserId, selectUserName } from '../utils/auth.selectors';
import { UserService } from '../service/UserService';

@Component({
  selector: 'app-history',
  standalone: false,
  templateUrl: './history.component.html',
  styleUrl: './history.component.css'
})
export class HistoryComponent implements OnInit {

  private dealSvc = inject(DealService);
  private store = inject(Store);

  userId = '';
  error?: string;
  loading = false;

  pendingDeals: Deal[] = [];
  completedDeals: Deal[] = [];
  rejectedDeals: Deal[] = [];

  ngOnInit(): void {
    this.store.select(selectUserId).subscribe(uid => {
      this.userId = uid || '';
      if (!this.userId) {
        this.error = 'You must be logged in to see your deals.';
        return;
      }
      this.loadDeals();
    });
  }

  loadDeals() {
    this.loading = true;
    this.dealSvc.getDealsForUser(this.userId).subscribe({
      next: (allDeals) => {
        this.pendingDeals = allDeals.filter(d => !d.rejected && !d.completed);
        this.completedDeals = allDeals.filter(d => d.completed);
        this.rejectedDeals = allDeals.filter(d => d.rejected);

        const allUserIds = new Set<string>();
        allDeals.forEach(d => {
          allUserIds.add(d.ownerId);
          allUserIds.add(d.initiatorId);
        });
        allUserIds.delete(this.userId);
        const otherUserIds = Array.from(allUserIds);
        this.loadAllUsernames(otherUserIds);

        this.loading = false;


      },
      error: () => {
        this.error = 'Failed to load deals.';
        this.loading = false;
      }
    });
  }
  private userSvc = inject(UserService)
  usernameMap: Record<string, string> = {};
  loadAllUsernames(userIds: string[]) {
    userIds.forEach(uid => {
      this.userSvc.getPublicProfile(uid).subscribe({
        next: (data) => {
          this.usernameMap[uid] = data.username;
        },
        error: () => {
          this.usernameMap[uid] = '';
        }
      });
    });
  }
}