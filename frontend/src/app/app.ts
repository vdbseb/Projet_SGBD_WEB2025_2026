import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {WelcomePage} from './views/welcome-page/welcome-page';
import {Header} from './views/layout/header/header';
import {Footer} from './views/layout/footer/footer';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Header, Footer],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('PadelFront');
}
