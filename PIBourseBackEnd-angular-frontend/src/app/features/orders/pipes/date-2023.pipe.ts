import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'date2023',
  standalone: true
})
export class Date2023Pipe implements PipeTransform {
  
  transform(value: string | Date | null | undefined): string {
    if (!value) {
      return '';
    }

    // Convertir en Date si c'est une string
    const date = typeof value === 'string' ? new Date(value) : value;

    // Verifier que c'est une date valide
    if (isNaN(date.getTime())) {
      return '';
    }

    // Obtenir le jour, mois, heure, minute
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    // Retourner au format dd/MM/2023 HH:mm (date forcee en 2023)
    return `${day}/${month}/2023 ${hours}:${minutes}`;
  }
}
