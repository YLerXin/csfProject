import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Deal, DealMessage, Product } from "../model/Products";
import { Observable } from "rxjs";

@Injectable()
export class DealService {
    private http = inject(HttpClient);

  initiateDeal(deal: Deal): Observable<Deal> {
    return this.http.post<Deal>('/api/deals/initiate', deal);
  }
  getDeal(dealId: string): Observable<Deal> {
    return this.http.get<Deal>(`/api/deals/${dealId}`);
  }
  updateDealItems(
    dealId: string,
    initiatorItems: Product[],
    ownerItems: Product[]
  ): Observable<Deal> {
    return this.http.put<Deal>(`/api/deals/${dealId}/items`, {
      initiatorItems,
      ownerItems
    });
  }
  postMessage(dealId: string, message: DealMessage): Observable<Deal> {
    return this.http.put<Deal>(`/api/deals/${dealId}/message`, message);
  }
  acceptDeal(dealId: string, userId: string): Observable<Deal> {
    return this.http.patch<Deal>(`/api/deals/${dealId}/accept`, {}, {
      params: { userId }
    });
  }
  rejectDeal(dealId: string, userId: string): Observable<Deal> {
    return this.http.patch<Deal>(`/api/deals/${dealId}/reject`, {}, {
      params: { userId }
    });
  }
  updateMeeting(
    dealId: string,
    location: string,
    dateTime: string
  ): Observable<Deal> {
    return this.http.patch<Deal>(`/api/deals/${dealId}/meeting`, {
      location,
      dateTime
    });
  }
  getDealsForUser(userId: string): Observable<Deal[]> {
    return this.http.get<Deal[]>(`/api/deals/user/${userId}`);
  }

  getPaymentIntentSecret(dealId: string): Observable<{ clientSecret: string }> {
    return this.http.get<{ clientSecret: string }>(`/api/deals/${dealId}/payment-intent-secret`);
  }

  confirmPaid(dealId: string): Observable<Deal> {
    return this.http.post<Deal>(`/api/deals/${dealId}/confirm-paid`, {});
  }


}