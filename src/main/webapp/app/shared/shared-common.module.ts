import { NgModule } from '@angular/core';

import { RandomizePaginateElasticsearchSharedLibsModule, FindLanguageFromKeyPipe, JhiAlertComponent, JhiAlertErrorComponent } from './';

@NgModule({
    imports: [RandomizePaginateElasticsearchSharedLibsModule],
    declarations: [FindLanguageFromKeyPipe, JhiAlertComponent, JhiAlertErrorComponent],
    exports: [RandomizePaginateElasticsearchSharedLibsModule, FindLanguageFromKeyPipe, JhiAlertComponent, JhiAlertErrorComponent]
})
export class RandomizePaginateElasticsearchSharedCommonModule {}
