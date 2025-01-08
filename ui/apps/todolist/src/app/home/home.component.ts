import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.css'],
    standalone: false
})
export class HomeComponent implements OnInit {
  constructor() {}

  ngOnInit() {}

  get message(): string {
    return 'Welcome to Angular and Spring Boot 2';
  }
}
