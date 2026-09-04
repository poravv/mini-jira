import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { WeatherCardComponent } from './features/weather/weather-card/weather-card.component';
import { UserSessionService } from './features/auth/user-session.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, WeatherCardComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  private readonly router = inject(Router);
  readonly session = inject(UserSessionService);

  get isAuthRoute(): boolean {
    return this.router.url.startsWith('/auth');
  }
}
