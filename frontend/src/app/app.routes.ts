import { Routes } from '@angular/router';
import { LoginComponent } from './components/login.component';
import { RegisterComponent } from './components/register.component';
import { DashboardComponent } from './components/dashboard.component';
import { ShortagesListComponent } from './components/shortages-list.component';
import { ShortageDetailComponent } from './components/shortage-detail.component';
import { CreateShortageComponent } from './components/create-shortage.component';
import { CreateTenderComponent } from './components/create-tender.component';
import { TendersListComponent } from './components/tenders-list.component';
import { NotificationsComponent } from './components/notifications.component';
import { AnalyticsComponent } from './components/analytics.component';
import { authGuard, roleGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { 
    path: '', 
    component: DashboardComponent,
    canActivate: [authGuard]
  },
  { 
    path: 'shortages', 
    component: ShortagesListComponent,
    canActivate: [authGuard]
  },
  { 
    path: 'shortages/:id', 
    component: ShortageDetailComponent,
    canActivate: [authGuard]
  },
  { 
    path: 'create-shortage', 
    component: CreateShortageComponent,
    canActivate: [authGuard, roleGuard(['ADMIN', 'COUNTRY_ADMIN'])]
  },
  { 
    path: 'create-tender/:id', 
    component: CreateTenderComponent,
    canActivate: [authGuard, roleGuard(['ADMIN', 'SUPPLIER', 'COUNTRY_ADMIN'])]
  },
  { 
    path: 'tenders', 
    component: TendersListComponent,
    canActivate: [authGuard]
  },
  { 
    path: 'notifications', 
    component: NotificationsComponent,
    canActivate: [authGuard]
  },
  { 
    path: 'analytics', 
    component: AnalyticsComponent,
    canActivate: [authGuard]
  },
  { path: '**', redirectTo: '' }
];
