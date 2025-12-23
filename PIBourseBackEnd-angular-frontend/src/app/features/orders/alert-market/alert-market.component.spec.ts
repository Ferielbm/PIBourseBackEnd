import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AlertMarketComponent } from './alert-market.component';

describe('AlertMarketComponent', () => {
  let component: AlertMarketComponent;
  let fixture: ComponentFixture<AlertMarketComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AlertMarketComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AlertMarketComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
