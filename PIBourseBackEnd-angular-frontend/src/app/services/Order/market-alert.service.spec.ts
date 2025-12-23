import { TestBed } from '@angular/core/testing';

import { MarketAlertService } from './market-alert.service';

describe('MarketAlertService', () => {
  let service: MarketAlertService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MarketAlertService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
