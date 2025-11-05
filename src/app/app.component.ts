import { Component } from '@angular/core';
import { ShellComponent } from './layout/shell/shell.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ShellComponent],
  template: `<app-shell />`,
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'PiBourseFront';
}
