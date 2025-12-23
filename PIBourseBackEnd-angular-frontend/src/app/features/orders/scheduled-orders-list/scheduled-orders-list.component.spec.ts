import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScheduledOrdersListComponent } from './scheduled-orders-list.component';

describe('ScheduledOrdersListComponent', () => {
  let component: ScheduledOrdersListComponent;
  let fixture: ComponentFixture<ScheduledOrdersListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ScheduledOrdersListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ScheduledOrdersListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
