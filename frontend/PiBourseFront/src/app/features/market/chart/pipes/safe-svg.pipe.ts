import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Pipe({
  name: 'safeSvg',
  standalone: true
})
export class SafeSvgPipe implements PipeTransform {
  constructor(private readonly sanitizer: DomSanitizer) {}

  transform(svg: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(svg);
  }
}
