/* tslint:disable max-line-length */
import { ComponentFixture, TestBed, inject, fakeAsync, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, of } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';

import { RandomizePaginateElasticsearchTestModule } from '../../../test.module';
import { AnimalDeleteDialogComponent } from 'app/entities/animal/animal-delete-dialog.component';
import { AnimalService } from 'app/entities/animal/animal.service';

describe('Component Tests', () => {
    describe('Animal Management Delete Component', () => {
        let comp: AnimalDeleteDialogComponent;
        let fixture: ComponentFixture<AnimalDeleteDialogComponent>;
        let service: AnimalService;
        let mockEventManager: any;
        let mockActiveModal: any;

        beforeEach(() => {
            TestBed.configureTestingModule({
                imports: [RandomizePaginateElasticsearchTestModule],
                declarations: [AnimalDeleteDialogComponent]
            })
                .overrideTemplate(AnimalDeleteDialogComponent, '')
                .compileComponents();
            fixture = TestBed.createComponent(AnimalDeleteDialogComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(AnimalService);
            mockEventManager = fixture.debugElement.injector.get(JhiEventManager);
            mockActiveModal = fixture.debugElement.injector.get(NgbActiveModal);
        });

        describe('confirmDelete', () => {
            it('Should call delete service on confirmDelete', inject(
                [],
                fakeAsync(() => {
                    // GIVEN
                    spyOn(service, 'delete').and.returnValue(of({}));

                    // WHEN
                    comp.confirmDelete(123);
                    tick();

                    // THEN
                    expect(service.delete).toHaveBeenCalledWith(123);
                    expect(mockActiveModal.dismissSpy).toHaveBeenCalled();
                    expect(mockEventManager.broadcastSpy).toHaveBeenCalled();
                })
            ));
        });
    });
});
