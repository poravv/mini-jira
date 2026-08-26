import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

import { WeatherCardComponent } from './features/weather/weather-card/weather-card.component';
import { UserSessionService } from './features/users/user-session.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, WeatherCardComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  readonly session = inject(UserSessionService);
}
