import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { IAnimal } from 'app/shared/model/animal.model';
import { AnimalService } from './animal.service';

@Component({
    selector: 'jhi-animal-update',
    templateUrl: './animal-update.component.html'
})
export class AnimalUpdateComponent implements OnInit {
    animal: IAnimal;
    isSaving: boolean;

    constructor(protected animalService: AnimalService, protected activatedRoute: ActivatedRoute) {}

    ngOnInit() {
        this.isSaving = false;
        this.activatedRoute.data.subscribe(({ animal }) => {
            this.animal = animal;
        });
    }

    previousState() {
        window.history.back();
    }

    save() {
        this.isSaving = true;
        if (this.animal.id !== undefined) {
            this.subscribeToSaveResponse(this.animalService.update(this.animal));
        } else {
            this.subscribeToSaveResponse(this.animalService.create(this.animal));
        }
    }

    protected subscribeToSaveResponse(result: Observable<HttpResponse<IAnimal>>) {
        result.subscribe((res: HttpResponse<IAnimal>) => this.onSaveSuccess(), (res: HttpErrorResponse) => this.onSaveError());
    }

    protected onSaveSuccess() {
        this.isSaving = false;
        this.previousState();
    }

    protected onSaveError() {
        this.isSaving = false;
    }
}
