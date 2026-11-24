import { Routes } from '@angular/router';

import { ProjectDetailComponent } from './project-detail/project-detail.component';
import { ProjectFormComponent } from './project-form/project-form.component';
import { ProjectListComponent } from './project-list/project-list.component';

/** Rutas de la feature projects, cargadas lazy bajo el prefijo /proyectos. */
export const PROJECTS_ROUTES: Routes = [
  { path: '', component: ProjectListComponent },
  { path: 'new', component: ProjectFormComponent },
  { path: ':id', component: ProjectDetailComponent }
];
