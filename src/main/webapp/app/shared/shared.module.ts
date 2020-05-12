import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { NgbDateAdapter } from '@ng-bootstrap/ng-bootstrap';

import { NgbDateMomentAdapter } from './util/datepicker-adapter';
import {
    RandomizePaginateElasticsearchSharedLibsModule,
    RandomizePaginateElasticsearchSharedCommonModule,
    JhiLoginModalComponent,
    HasAnyAuthorityDirective
} from './';

@NgModule({
    imports: [RandomizePaginateElasticsearchSharedLibsModule, RandomizePaginateElasticsearchSharedCommonModule],
    declarations: [JhiLoginModalComponent, HasAnyAuthorityDirective],
    providers: [{ provide: NgbDateAdapter, useClass: NgbDateMomentAdapter }],
    entryComponents: [JhiLoginModalComponent],
    exports: [RandomizePaginateElasticsearchSharedCommonModule, JhiLoginModalComponent, HasAnyAuthorityDirective],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class RandomizePaginateElasticsearchSharedModule {
    static forRoot() {
        return {
            ngModule: RandomizePaginateElasticsearchSharedModule
        };
    }
}
