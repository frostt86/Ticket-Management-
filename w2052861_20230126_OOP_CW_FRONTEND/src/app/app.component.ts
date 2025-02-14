import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {ConfigurationFormComponent} from './Components/configuration-form/configuration-form.component';
import { ChartsComponent } from './Components/charts/charts.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, ConfigurationFormComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'OOP1';
}
