import { CommonModule } from '@angular/common';
import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';
import { JhiLanguageService } from 'ng-jhipster';
import { JhiLanguageHelper } from 'app/core';

import { RandomizePaginateElasticsearchSharedModule } from 'app/shared';
import {
    AnimalComponent,
    AnimalDetailComponent,
    AnimalUpdateComponent,
    AnimalDeletePopupComponent,
    AnimalDeleteDialogComponent,
    animalRoute,
    animalPopupRoute
} from './';

const ENTITY_STATES = [...animalRoute, ...animalPopupRoute];

@NgModule({
    imports: [RandomizePaginateElasticsearchSharedModule, RouterModule.forChild(ENTITY_STATES), CommonModule, CommonModule],
    declarations: [AnimalComponent, AnimalDetailComponent, AnimalUpdateComponent, AnimalDeleteDialogComponent, AnimalDeletePopupComponent],
    entryComponents: [AnimalComponent, AnimalUpdateComponent, AnimalDeleteDialogComponent, AnimalDeletePopupComponent],
    providers: [{ provide: JhiLanguageService, useClass: JhiLanguageService }],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class RandomizePaginateElasticsearchAnimalModule {
    constructor(private languageService: JhiLanguageService, private languageHelper: JhiLanguageHelper) {
        this.languageHelper.language.subscribe((languageKey: string) => {
            if (languageKey !== undefined) {
                this.languageService.changeLanguage(languageKey);
            }
        });
    }
}
