export interface Product{
    productId?: string; 
    userId: string;        
    productName: string;
    productDetails?: string;
    availability?: boolean;
    condition?: string;   
    dateAdded?: string;   
    price?: number;
    images?: string[];
    tags?: string[];
}

export interface DealMessage {
    senderId: string;
    text: string;
    timestamp?: string;
}

export interface Deal {
    id?: string;
    initiatorId: string;
    ownerId: string;
    initiatorAccepted: boolean;
    ownerAccepted: boolean;
    rejected: boolean;
    completed: boolean;
    initiatorItems: Product[];
    ownerItems: Product[];
    finalPriceDifference?: number;
    meetingLocation?: string;
    meetingDateTime?: string;
    messages: DealMessage[];
    pendingPayment?: boolean;
    paymentIntentId?: string;
}