import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScheduledOrderFormComponent } from './scheduled-order-form.component';

describe('ScheduledOrderFormComponent', () => {
  let component: ScheduledOrderFormComponent;
  let fixture: ComponentFixture<ScheduledOrderFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ScheduledOrderFormComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ScheduledOrderFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
