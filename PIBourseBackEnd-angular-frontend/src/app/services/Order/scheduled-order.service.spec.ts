import { TestBed } from '@angular/core/testing';

import { ScheduledOrderService } from './scheduled-order.service';

describe('ScheduledOrderService', () => {
  let service: ScheduledOrderService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ScheduledOrderService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
