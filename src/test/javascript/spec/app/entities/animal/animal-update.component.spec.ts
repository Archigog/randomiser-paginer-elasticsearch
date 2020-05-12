/* tslint:disable max-line-length */
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { Observable, of } from 'rxjs';

import { RandomizePaginateElasticsearchTestModule } from '../../../test.module';
import { AnimalUpdateComponent } from 'app/entities/animal/animal-update.component';
import { AnimalService } from 'app/entities/animal/animal.service';
import { Animal } from 'app/shared/model/animal.model';

describe('Component Tests', () => {
    describe('Animal Management Update Component', () => {
        let comp: AnimalUpdateComponent;
        let fixture: ComponentFixture<AnimalUpdateComponent>;
        let service: AnimalService;

        beforeEach(() => {
            TestBed.configureTestingModule({
                imports: [RandomizePaginateElasticsearchTestModule],
                declarations: [AnimalUpdateComponent]
            })
                .overrideTemplate(AnimalUpdateComponent, '')
                .compileComponents();

            fixture = TestBed.createComponent(AnimalUpdateComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(AnimalService);
        });

        describe('save', () => {
            it('Should call update service on save for existing entity', fakeAsync(() => {
                // GIVEN
                const entity = new Animal(123);
                spyOn(service, 'update').and.returnValue(of(new HttpResponse({ body: entity })));
                comp.animal = entity;
                // WHEN
                comp.save();
                tick(); // simulate async

                // THEN
                expect(service.update).toHaveBeenCalledWith(entity);
                expect(comp.isSaving).toEqual(false);
            }));

            it('Should call create service on save for new entity', fakeAsync(() => {
                // GIVEN
                const entity = new Animal();
                spyOn(service, 'create').and.returnValue(of(new HttpResponse({ body: entity })));
                comp.animal = entity;
                // WHEN
                comp.save();
                tick(); // simulate async

                // THEN
                expect(service.create).toHaveBeenCalledWith(entity);
                expect(comp.isSaving).toEqual(false);
            }));
        });
    });
});
