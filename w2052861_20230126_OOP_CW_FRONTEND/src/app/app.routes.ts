import { Routes } from '@angular/router';
import { ConfigurationFormComponent } from './Components/configuration-form/configuration-form.component';

export const routes: Routes = [
  { path: '', component: ConfigurationFormComponent }, // Default route
  { path: 'configuration', component: ConfigurationFormComponent }, // Configuration form route
];
