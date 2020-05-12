import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IAnimal } from 'app/shared/model/animal.model';
import { AnimalService } from './animal.service';

@Component({
    selector: 'jhi-animal-delete-dialog',
    templateUrl: './animal-delete-dialog.component.html'
})
export class AnimalDeleteDialogComponent {
    animal: IAnimal;

    constructor(protected animalService: AnimalService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(id: number) {
        this.animalService.delete(id).subscribe(response => {
            this.eventManager.broadcast({
                name: 'animalListModification',
                content: 'Deleted an animal'
            });
            this.activeModal.dismiss(true);
        });
    }
}

@Component({
    selector: 'jhi-animal-delete-popup',
    template: ''
})
export class AnimalDeletePopupComponent implements OnInit, OnDestroy {
    protected ngbModalRef: NgbModalRef;

    constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

    ngOnInit() {
        this.activatedRoute.data.subscribe(({ animal }) => {
            setTimeout(() => {
                this.ngbModalRef = this.modalService.open(AnimalDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
                this.ngbModalRef.componentInstance.animal = animal;
                this.ngbModalRef.result.then(
                    result => {
                        this.router.navigate(['/animal', { outlets: { popup: null } }]);
                        this.ngbModalRef = null;
                    },
                    reason => {
                        this.router.navigate(['/animal', { outlets: { popup: null } }]);
                        this.ngbModalRef = null;
                    }
                );
            }, 0);
        });
    }

    ngOnDestroy() {
        this.ngbModalRef = null;
    }
}
