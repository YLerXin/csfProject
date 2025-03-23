import { Injectable } from "@angular/core";
import { Deal } from "../model/Products";
import { Client, Message, Stomp } from '@stomp/stompjs';
import { Subject } from "rxjs";

@Injectable()
export class WebSocketDealService {
  private stompClient?: Client;
  private dealUpdatesSubject = new Subject<Deal>();
  dealUpdates$ = this.dealUpdatesSubject.asObservable();

  //for development i use private brokerURL = 'ws://localhost:8080/ws';

  connect() {

    const loc = window.location;
    const wsProtocol = loc.protocol === 'https:' ? 'wss:' : 'ws:';
    const portPart = loc.port ? `:${loc.port}` : '';
    const wsUrl = `${wsProtocol}//${loc.hostname}${portPart}/ws`;


    this.stompClient = new Client({
       brokerURL: 'ws://localhost:8080/ws',
      //brokerURL: wsUrl,
        debug: (msg) => console.log('[STOMP]', msg),
      connectHeaders: {}
    });

    this.stompClient.onConnect = (frame) => {
      console.log('Connected: ' + frame);
      this.stompClient?.subscribe('/topic/deals', (message: Message) => {
        const deal: Deal = JSON.parse(message.body);
        this.dealUpdatesSubject.next(deal);
      });
    };

    this.stompClient.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };

    this.stompClient.onWebSocketError = (evt) => {
      console.error('WebSocket Error', evt);
    };

    this.stompClient.activate();
  }

  disconnect() {
    this.stompClient?.deactivate();
  }
}